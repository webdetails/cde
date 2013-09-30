/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlAdhocComponentTypeReader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.packager.input.PluginRepositoryOrigin;
import pt.webdetails.cdf.dd.packager.input.StaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Loads XML model files,
 * component types and property types,
 * from the file system,
 * of a Pentaho CDE plugin instalation.
 *
 * @author dcleao
 */
public final class XmlFsPluginModelReader {

  public static final String RESOURCES_DIR    = "resources";

  public static final String DEF_BASE_TYPE    = PrimitiveComponentType.class.getSimpleName();
  
  public static final String BASE_DIR         = Utils.joinPath(RESOURCES_DIR, "base");
  public static final String BASE_PROPS_DIR   = Utils.joinPath(BASE_DIR, "properties");
  public static final String BASE_COMPS_DIR   = Utils.joinPath(BASE_DIR, "components");

  public static final String DEF_CUSTOM_TYPE  = CustomComponentType.class.getSimpleName();
  public static final String CUSTOM_DIR       = Utils.joinPath(RESOURCES_DIR, "custom");
  public static final String CUSTOM_PROPS_DIR = Utils.joinPath(CUSTOM_DIR, "properties");
  
  public static final String DEF_WIDGET_STUB_TYPE  = WidgetComponentType.class.getSimpleName();
  public static final String WIDGETS_DIR      = "widgets";

  public static final String CUSTOM_PROPS_FILENAME  = "property";
  public static final String COMPONENT_FILENAME     = "component";
  // extension for component properties definitions
  public static final String DEFINITION_FILE_EXT  = "xml";

  protected static final Log logger = LogFactory.getLog(XmlFsPluginModelReader.class);

  // -----------
  
  private final Boolean continueOnError;

  private IContentAccessFactory contentAccessFactory;

  public XmlFsPluginModelReader(boolean continueOnError) //the real ctor, only usage calls with false
  {
    this.continueOnError = continueOnError;
  }

  public XmlFsPluginModelReader(IContentAccessFactory caf, boolean continueOnError) {
    this(false);
    contentAccessFactory = caf;
  }

  /**
   * Reads properties, components and widgets
   * @return model with component and property types
   * @throws ThingReadException
   */
  public MetaModel.Builder read(XmlFsPluginThingReaderFactory factory) throws ThingReadException {
    MetaModel.Builder model = new MetaModel.Builder();

    // Read Properties
    this.readBaseProperties(model, factory);
    this.readCustomProperties(model, factory);

    // Read Components
    logger.info(String.format("Loading BASE components from: %s", BASE_COMPS_DIR));
    this.readBaseComponents(model, factory);
    this.readCustomComponents(model, factory);
    this.readWidgetStubComponents(model, factory);
  
    return model;
  }
  
  private void readBaseComponents(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory) throws ThingReadException {
    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(BASE_COMPS_DIR).listFiles(null, 
        new GenericBasicFileFilter(null, DEFINITION_FILE_EXT), IReadAccess.DEPTH_ALL);
    PathOrigin origin = new StaticSystemOrigin(BASE_COMPS_DIR);
    for (IBasicFile file : filesList) {
      this.readComponentsFile(model, factory, file, DEF_BASE_TYPE, origin);
    }
  }

  private void readBaseProperties(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory) throws ThingReadException {
    
    logger.info(String.format("Loading BASE properties from: %s", BASE_PROPS_DIR));
      
    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(BASE_PROPS_DIR).listFiles(null, 
            new GenericBasicFileFilter(null, DEFINITION_FILE_EXT), IReadAccess.DEPTH_ALL);

    if(filesList != null) {
        
      for (IBasicFile file : filesList) {
        this.readPropertiesFile(model, factory, file);
      }
    }
  }

  private void readCustomProperties(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory)  throws ThingReadException {
    
    logger.info(String.format("Loading CUSTOM properties from: %s", CUSTOM_PROPS_DIR));
    
    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(CUSTOM_PROPS_DIR).listFiles(null, 
            new GenericBasicFileFilter(CUSTOM_PROPS_FILENAME,DEFINITION_FILE_EXT), IReadAccess.DEPTH_ALL);

   if(filesList != null) {
     for(IBasicFile file : filesList) {
       this.readPropertiesFile(model, factory, file);
     }
   }
 }
  private void readPropertiesFile(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, IBasicFile file) throws ThingReadException {
    Document doc;
    try {
      doc = Utils.getDocFromFile(file, null);
    }
    catch (Exception ex) {
      ThingReadException ex2 = new ThingReadException("Cannot read properties file '" + file + "'.", ex);
      if(!this.continueOnError) { throw ex2; }

      // log and move on
      logger.fatal(null, ex2);
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings("unchecked")
    List<Element> propertieElems = doc.selectNodes("//DesignerProperty");
    for (Element propertyElem : propertieElems) {
      readProperty(model, factory, propertyElem, file);
    }
  }

  private void readProperty(
      MetaModel.Builder modelBuilder,
      XmlFsPluginThingReaderFactory factory,
      Element propertyElem,
      IBasicFile file)
  {
    String className = null;
    try {
      className = Utils.getNodeText("Header/Override", propertyElem);
      
      if(StringUtils.isEmpty(className)) { className = "PropertyType"; }
      
      //  String name = Utils.getNodeText("Header/Name", propertyElem); //TODO: do anything with this?
      
      PropertyType.Builder prop = factory.getPropertyTypeReader().read(propertyElem, file.getPath());

      modelBuilder.addProperty(prop);
    
    } catch(IllegalArgumentException ex) {
      if(!this.continueOnError) { throw ex; }

      // Just log and move on
      logger.fatal("Failed to read property ", ex);
    }
  }

  private void readCustomComponents(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory) throws ThingReadException {
    for(PathOrigin origin : CdeEnvironment.getPluginResourceLocationManager().getCustomComponentsLocations()) {
      readCustomComponentsLocation(model, factory, origin);
    }
  }
  
  private void readCustomComponentsLocation(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, PathOrigin origin) throws ThingReadException {
      GenericBasicFileFilter filter = new GenericBasicFileFilter(COMPONENT_FILENAME, DEFINITION_FILE_EXT);
      IReadAccess access = origin.getReader(contentAccessFactory);
      List<IBasicFile> filesList = access.listFiles (null, filter, IReadAccess.DEPTH_ALL);
  
      if (filesList != null) {
        logger.debug(String.format("%s sub-folders found", filesList.size()));
        
        IBasicFile[] filesArray = filesList.toArray(new IBasicFile[]{});
        
        Arrays.sort(filesArray, getComponentFileComparator());
  
        for (IBasicFile file : filesList) {
          this.readComponentsFile(model, factory, file, DEF_CUSTOM_TYPE, origin);
        }
      }
  }
  
  private void readWidgetStubComponents(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory) throws ThingReadException {
    
    logger.info(String.format("Loading WIDGET components from: %s", WIDGETS_DIR));
      
    List<IBasicFile> filesList = CdeEnvironment.getPluginRepositoryReader(WIDGETS_DIR).listFiles(null, 
              new GenericBasicFileFilter(COMPONENT_FILENAME,DEFINITION_FILE_EXT), IReadAccess.DEPTH_ALL);
    PathOrigin widgetsOrigin = new PluginRepositoryOrigin(WIDGETS_DIR);
    
    if(filesList != null) {

      logger.debug(String.format("%s widget components found", filesList.size()));

      IBasicFile[] filesArray = filesList.toArray(new IBasicFile[]{});

      Arrays.sort(filesArray, getComponentFileComparator());

      for (IBasicFile file : filesList) {
        this.readComponentsFile(model, factory, file, DEF_WIDGET_STUB_TYPE, widgetsOrigin);
      }
    }
  }


  private Comparator<IBasicFile> getComponentFileComparator() {
    return new Comparator<IBasicFile>() { //TODO: why sort?
      @Override
      public int compare(IBasicFile file1, IBasicFile file2) {
//        if (file1 == null && file2 == null) { //TODO if this makes sense so does one of them being null
//          return 0;
//        } else {
          return file1.getFullPath().toLowerCase().compareTo(file2.getFullPath().toLowerCase());
//        }
      }
    };
  }

 
  private void readComponentsFile(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, IBasicFile file, String defaultClassName, PathOrigin origin)
    throws ThingReadException
  {
    Document doc;
    try {
      doc = Utils.getDocFromFile(file, null);
    } catch (Exception ex) {
      String msg = "Cannot read components file '" + file.getFullPath() + "'.";

      if(!this.continueOnError) {
        throw new ThingReadException(msg, ex);
      }
      // log and move on
      logger.fatal(msg, ex);
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings("unchecked")
    List<Element> componentElems = doc.selectNodes("//DesignerComponent");

    if (logger.isDebugEnabled() && componentElems.size() > 0) {
      logger.debug(String.format("\t%s [%s]", file.getPath(), componentElems.size()));
    }

    for (Element componentElem : componentElems) {
      readComponent(model, factory, componentElem, file.getPath(), defaultClassName, origin);
    }
  }


  private void readComponent(MetaModel.Builder model, XmlFsPluginThingReaderFactory factory, Element componentElem, String sourcePath, String defaultClassName, PathOrigin origin)
    throws ThingReadException
  {
    String className = Utils.getNodeText("Header/Override", componentElem);

    if(StringUtils.isEmpty(className)) { className = defaultClassName; }
    
    String name = Utils.getNodeText("Header/Name", componentElem);
    
    XmlAdhocComponentTypeReader<? extends ComponentType.Builder> reader = factory.getComponentTypeReader(className);
    if (reader == null) {
      String msg = "Failed to read component of class '" +  className + "' and name " + name;
      if (!this.continueOnError) {
        throw new ThingReadException(msg);
      }
      // Just log and move on
      logger.fatal(msg);
    }

    ComponentType.Builder comp = reader.read(componentElem, origin, sourcePath);
    comp.setOrigin(origin);//TODO: in reader?
    model.addComponent(comp);
  }

}