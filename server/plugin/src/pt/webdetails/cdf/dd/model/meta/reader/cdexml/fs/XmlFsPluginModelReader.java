
package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlPluginModelReadContext;
import pt.webdetails.cdf.dd.FsPluginResourceLocations;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

/**
 * Loads XML model files,
 * component types and property types,
 * from the file system,
 * of a Pentaho CDE plugin instalation.
 *
 * @author dcleao
 */
public final class XmlFsPluginModelReader implements IThingReader
{
  public static final String SOLUTION_PATH = PentahoRepositoryAccess.getPentahoSolutionPath("");
  
  // Paths relative to getSolutionPath
  public static final String PLUGIN_DIR       = Utils.joinPath("system", DashboardDesignerContentGenerator.PLUGIN_NAME);
  public static final String RESOURCES_DIR    = Utils.joinPath(PLUGIN_DIR, "resources");

  public static final String DEF_BASE_TYPE    = PrimitiveComponentType.class.getSimpleName();
  
  public static final String BASE_DIR         = Utils.joinPath(RESOURCES_DIR, "base");
  public static final String BASE_PROPS_DIR   = Utils.joinPath(BASE_DIR, "properties");
  public static final String BASE_COMPS_DIR   = Utils.joinPath(BASE_DIR, "components");

  public static final String DEF_CUSTOM_TYPE  = CustomComponentType.class.getSimpleName();
  public static final String CUSTOM_DIR       = Utils.joinPath(RESOURCES_DIR, "custom");
  public static final String CUSTOM_PROPS_DIR = Utils.joinPath(CUSTOM_DIR, "properties");
  
  public static final String DEF_WIDGET_STUB_TYPE  = WidgetComponentType.class.getSimpleName();
  public static final String WIDGETS_DIR      = Utils.joinPath("cde", "widgets");

  public static final String CUSTOM_PROPS_FILENAME  = "property.xml";
  public static final String COMPONENT_FILENAME     = "component.xml";

  protected static final Log _logger = LogFactory.getLog(XmlFsPluginModelReader.class);

  // -----------
  
  private final String  _basePath;
  private final Boolean _continueOnError;

  public XmlFsPluginModelReader()
  {
    this(SOLUTION_PATH);
  }

  public XmlFsPluginModelReader(String path)
  {
    this(path, false);
  }

  public XmlFsPluginModelReader(boolean continueOnError)
  {
    this(SOLUTION_PATH, continueOnError);
  }

  public XmlFsPluginModelReader(String path, boolean continueOnError)
  {
    if(StringUtils.isEmpty(path)) { throw new IllegalArgumentException("path"); }

    this._continueOnError = continueOnError;
    this._basePath = path;
  }

  // As this is a top-level reader,
  // the source and sourcePath arguments are not meaningful and are thus ignored.
  public void read(
          Thing.Builder builder,
          IThingReadContext context,
          java.lang.Object source,
          String sourcePath)
          throws ThingReadException
  {
    MetaModel.Builder model = (MetaModel.Builder) builder;

    this.read(model, context);
  }

  // idem
  public MetaModel.Builder read(
          IThingReadContext context,
          java.lang.Object source,
          String sourcePath)
          throws ThingReadException
  {
    MetaModel.Builder builder = new MetaModel.Builder();

    this.read(builder, context);

    return builder;
  }

  public MetaModel.Builder read(IThingReadContext context)
          throws ThingReadException
  {
    MetaModel.Builder model = new MetaModel.Builder();

    this.read(model, context);

    return model;
  }

  public void read(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    assert model != null;

    XmlPluginModelReadContext fsContext = new
            XmlPluginModelReadContext(context, this._basePath);

    // Read Properties
    this.readBaseProperties  (model, fsContext);
    this.readCustomProperties(model, fsContext);

    // Read Components
    this.readBaseComponents(model, fsContext);
    this.readCustomComponents(model, fsContext);
    this.readWidgetStubComponents(model, fsContext);
  }
  
  private void readBaseProperties(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    File baseDir = new File(this._basePath, BASE_PROPS_DIR);
    FilenameFilter xmlFilesFilter = new FilenameFilter()
    {
      public boolean accept(File adir, String name)
      {
        return !name.startsWith(".") && name.endsWith(".xml");
      }
    };
    
    _logger.info(String.format("Loading BASE properties from: %s", BASE_PROPS_DIR));
    
    String baseDirPath = baseDir.getPath();
    String[] fileNames = baseDir.list(xmlFilesFilter);
    if(fileNames != null)
    {
      for (String fileName : fileNames)
      {
        String filePath = Utils.joinPath(baseDirPath, fileName);
        this.readPropertiesFile(model, context, filePath);
      }
    }
  }

  private void readCustomProperties(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    File baseDir = new File(this._basePath, CUSTOM_PROPS_DIR);

    FilenameFilter subFoldersWithPropsFilter = new FilenameFilter()
    {
      public boolean accept(File systemFolder, String name)
      {
        File propFile = new File(Utils.joinPath(systemFolder.getPath(), name, CUSTOM_PROPS_FILENAME));
        return propFile.isFile() && propFile.canRead();
      }
    };
    
    _logger.info(String.format("Loading CUSTOM properties from: %s", CUSTOM_PROPS_DIR));
    
    String baseDirPath = baseDir.getPath();
    String[] folderNames = baseDir.list(subFoldersWithPropsFilter);
    if(folderNames != null)
    {
      for(String folderName : folderNames)
      {
        String filePath = Utils.joinPath(baseDirPath, folderName, CUSTOM_PROPS_FILENAME);
        this.readPropertiesFile(model, context, filePath);
      }
    }
  }

  private void readBaseComponents(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    File baseDir = new File(this._basePath, BASE_COMPS_DIR);
    FilenameFilter xmlFilesFilter = new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return !name.startsWith(".") && name.endsWith(".xml");
      }
    };

    _logger.info(String.format("Loading BASE components from: %s", BASE_COMPS_DIR));

    List<File> files = Utils.listAllFiles(baseDir, xmlFilesFilter);
    if(files.size() > 0) 
    {
      _logger.debug(String.format("%s BASE component files found", files.size()));

      for (File file : files)
      {
        String relFilePath = Utils.getRelativePath(file.getPath(), _basePath);
        this.readComponentsFile(model, context, relFilePath, DEF_BASE_TYPE, BASE_COMPS_DIR);
      }
    }
  }

  private void readCustomComponents(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    for(String relDir : FsPluginResourceLocations.getCustomComponentsRelDirs())
    {
      readCustomComponentsLocation(model, context, relDir);
    }
  }

  private void readCustomComponentsLocation(MetaModel.Builder model, IThingReadContext context, String relDir)
          throws ThingReadException
  {
    String compAbsDirPath = Utils.joinPath(this._basePath, relDir);
    
    File compAbsDir = new File(compAbsDirPath);
    
    _logger.info(String.format("Loading CUSTOM components from: %s", relDir.toString()));

    FilenameFilter subFoldersFilter = new FilenameFilter()
    {
      public boolean accept(File folder, String name)
      {
        File plugin = new File(Utils.joinPath(folder.getPath(), name, COMPONENT_FILENAME));
        return plugin.isFile() && plugin.canRead();
      }
    };

    String[] folderNames = compAbsDir.list(subFoldersFilter);
    if (folderNames != null)
    {
      _logger.debug(String.format("%s sub-folders found", folderNames.length));

      Arrays.sort(folderNames, String.CASE_INSENSITIVE_ORDER);
      
      String relDirText = relDir.toString();
      
      for (String folderName : folderNames)
      {
        String relFilePath = Utils.joinPath(relDir, folderName, COMPONENT_FILENAME);
        this.readComponentsFile(model, context, relFilePath, DEF_CUSTOM_TYPE, relDirText);
      }
    }
  }

  private void readWidgetStubComponents(MetaModel.Builder model, IThingReadContext context)
          throws ThingReadException
  {
    File widgetsDir = new File(this._basePath, WIDGETS_DIR);
    FilenameFilter widgetComponentFilter = new FilenameFilter()
    {
      public boolean accept(File folder, String name)
      {
        return !name.startsWith(".") && name.endsWith("." + COMPONENT_FILENAME);
      }
    };

    _logger.info(String.format("Loading WIDGET components from: %s", widgetsDir.toString()));
    
    String[] fileNames = widgetsDir.list(widgetComponentFilter);
    if(fileNames != null)
    {
      _logger.debug(String.format("%s widget components found", fileNames.length));
      
      Arrays.sort(fileNames, String.CASE_INSENSITIVE_ORDER);

      for (String fileName : fileNames)
      {
        String fileRelPath = Utils.joinPath(WIDGETS_DIR, fileName);
        this.readComponentsFile(model, context, fileRelPath, DEF_WIDGET_STUB_TYPE, WIDGETS_DIR);
      }
    }
  }

  // ------------------

  private void readPropertiesFile(MetaModel.Builder model, IThingReadContext context, String filePath)
          throws ThingReadException
  {
    Document doc;
    try
    {
      doc = XmlDom4JHelper.getDocFromFile(filePath, null);
    }
    catch (Exception ex)
    {
      ThingReadException ex2 = new ThingReadException("Cannot read properties file '" + filePath + "'.", ex);
      if(!this._continueOnError) { throw ex2; }

      // log and move on
      _logger.fatal(null, ex2);
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings("unchecked")
    List<Element> propertieElems = doc.selectNodes("//DesignerProperty");
    for (Element propertyElem : propertieElems)
    {
      readProperty(model, context, propertyElem, filePath);
    }
  }

  private void readProperty(MetaModel.Builder model, IThingReadContext context, Element propertyElem, String sourcePath)
          throws ThingReadException
  {
    String className = null;
    try
    {
      className = XmlDom4JHelper.getNodeText("Header/Override", propertyElem);
      
      if(StringUtils.isEmpty(className)) { className = "PropertyType"; }
      
      String name = XmlDom4JHelper.getNodeText("Header/Name", propertyElem);
        
      IThingReader reader = context.getFactory().getReader(KnownThingKind.PropertyType, className, name);

      PropertyType.Builder prop = (PropertyType.Builder)reader.read(context, propertyElem, sourcePath);

      model.addProperty(prop);
    }
    catch(UnsupportedThingException ex)
    {
      ThingReadException ex2 = new ThingReadException("Failed to read property of class name '" +  className + "'.", ex);
      if(!this._continueOnError) { throw ex2; }

      // Just log and move on
      _logger.fatal(null, ex2);
    }
  }

  // ------------------
  private void readComponentsFile(MetaModel.Builder model, IThingReadContext context, String relFilePath, String defaultClassName, String baseRelDir)
          throws ThingReadException
  {
    Document doc;
    try
    {
      try
      {
        doc = XmlDom4JHelper.getDocFromFile(Utils.joinPath(_basePath, relFilePath), null);
      } 
      catch(Exception ex1) 
      {
        if(relFilePath.endsWith(COMPONENT_FILENAME)) { throw ex1; }

        // TODO: confirm this is really needed
        // Second that is relevant for CUSTOM and WIDGET components reading...
        relFilePath = Utils.joinPath(relFilePath, COMPONENT_FILENAME);

        doc = XmlDom4JHelper.getDocFromFile(relFilePath, null);
      }
    }
    catch (Exception ex)
    {
      ThingReadException ex2 = new ThingReadException("Cannot read components file '" + relFilePath + "'.", ex);
      if(!this._continueOnError) { throw ex2; }

      // log and move on
      _logger.fatal(null, ex2);
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings("unchecked")
    List<Element> componentElems = doc.selectNodes("//DesignerComponent");

    if (_logger.isDebugEnabled() && componentElems.size() > 0)
    {
      _logger.debug(String.format("\t%s [%s]", Utils.getRelativePath(relFilePath, baseRelDir), componentElems.size()));
    }

    for (Element componentElem : componentElems)
    {
      readComponent(model, context, componentElem, relFilePath, defaultClassName);
    }
  }

  private void readComponent(
          MetaModel.Builder model,
          IThingReadContext context,
          Element componentElem,
          String sourcePath,
          String defaultClassName) throws ThingReadException
  {
    String className = null;

    if(StringUtils.isEmpty(defaultClassName)) { defaultClassName = "ComponentType"; }

    try
    {
      className = XmlDom4JHelper.getNodeText("Header/Override", componentElem);
      
      if(StringUtils.isEmpty(className)) { className = defaultClassName; }
      
      String name = XmlDom4JHelper.getNodeText("Header/Name", componentElem);
      
      IThingReader reader = context.getFactory().getReader(KnownThingKind.ComponentType, className, name);

      ComponentType.Builder comp = (ComponentType.Builder)reader.read(context, componentElem, sourcePath);

      model.addComponent(comp);
    }
    catch(UnsupportedThingException ex)
    {
      ThingReadException ex2 = new ThingReadException("Failed to read component of class name '" +  className + "'.", ex);
      if(!this._continueOnError) { throw ex2; }

      // Just log and move on
      _logger.fatal(null, ex2);
    }
  }
}