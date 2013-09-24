/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs;

import pt.webdetails.cdf.dd.model.meta.reader.cdexml.XmlPluginModelReadContext;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
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
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Loads XML model files,
 * component types and property types,
 * from the file system,
 * of a Pentaho CDE plugin instalation.
 *
 * @author dcleao
 */
public final class XmlFsPluginModelReader implements IThingReader {

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
  public static final String CUSTOM_PROPS_FILE_EXTENSION  = "xml";

  protected static final Log logger = LogFactory.getLog(XmlFsPluginModelReader.class);

  // -----------
  
  private final Boolean continueOnError;

  public XmlFsPluginModelReader(){
	  continueOnError = false; /* default */
  }

  public XmlFsPluginModelReader(boolean continueOnError)
  {
    this.continueOnError = continueOnError;
  }

  public XmlFsPluginModelReader(String path, boolean continueOnError)
  {
    if(StringUtils.isEmpty(path)) { throw new IllegalArgumentException("path"); }

    this.continueOnError = continueOnError;
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

    XmlPluginModelReadContext fsContext = new XmlPluginModelReadContext(context);

    // Read Properties
    this.readBaseProperties  (model, fsContext);
    this.readCustomProperties(model, fsContext);

    // Read Components
    this.readBaseComponents(model, fsContext);
    this.readCustomComponents(model, fsContext);
    this.readWidgetStubComponents(model, fsContext);
  }
  
  private void readBaseProperties(MetaModel.Builder model, IThingReadContext context) throws ThingReadException {
    
	logger.info(String.format("Loading BASE properties from: %s", BASE_PROPS_DIR));
	  
    List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(BASE_PROPS_DIR).listFiles(null, 
    		new GenericBasicFileFilter(null, CUSTOM_PROPS_FILE_EXTENSION), IReadAccess.DEPTH_ALL);

    if(filesList != null) {
    	
      for (IBasicFile file : filesList) {
        this.readPropertiesFile(model, context, file);
      }
    }
  }

  private void readCustomProperties(MetaModel.Builder model, IThingReadContext context)  throws ThingReadException {
    
	 logger.info(String.format("Loading CUSTOM properties from: %s", CUSTOM_PROPS_DIR));
	 
	 List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(CUSTOM_PROPS_DIR).listFiles(null, 
			 new GenericBasicFileFilter(CUSTOM_PROPS_FILENAME,CUSTOM_PROPS_FILE_EXTENSION), IReadAccess.DEPTH_ALL);

    if(filesList != null) {
      for(IBasicFile file : filesList) {
        this.readPropertiesFile(model, context, file);
      }
    }
  }

  private void readBaseComponents(MetaModel.Builder model, IThingReadContext context) throws ThingReadException  {
    
	  logger.info(String.format("Loading BASE components from: %s", BASE_COMPS_DIR));
	  
	  List<IBasicFile> filesList = CdeEnvironment.getPluginSystemReader(BASE_COMPS_DIR).listFiles(null, 
	    		new GenericBasicFileFilter(null, CUSTOM_PROPS_FILE_EXTENSION), IReadAccess.DEPTH_ALL);

    if(filesList != null && filesList.size() > 0) {
      
    	logger.debug(String.format("%s BASE component files found", filesList.size()));

      for (IBasicFile file : filesList) {
        this.readComponentsFile(model, context, file, DEF_BASE_TYPE);
      }
    }
  }

  private void readCustomComponents(MetaModel.Builder model, IThingReadContext context) throws ThingReadException {
	  
    for(IReadAccess access : CdeEnvironment.getPluginResourceLocationManager().getAllCustomComponentsResourceLocations()) {
      readCustomComponentsLocation(model, context, access);
    }
  }

  private void readCustomComponentsLocation(MetaModel.Builder model, IThingReadContext context, IReadAccess access) throws ThingReadException {
    	  
	 GenericBasicFileFilter filter = new GenericBasicFileFilter(COMPONENT_FILENAME, CUSTOM_PROPS_FILE_EXTENSION);
	  
	 List<IBasicFile> filesList = access.listFiles(null, filter, IReadAccess.DEPTH_ALL);

    if (filesList != null) {
    	
      logger.debug(String.format("%s sub-folders found", filesList.size()));
      
      IBasicFile[] filesArray = filesList.toArray(new IBasicFile[]{});
      
      Arrays.sort(filesArray, 
    		  new Comparator<IBasicFile>(){

				@Override
				public int compare(IBasicFile file1, IBasicFile file2) {
					if (file1 == null && file2 == null){
						return 0;
					}else{
						return file1.getFullPath().toLowerCase().compareTo(file2.getFullPath().toLowerCase());
					}
				}	
      		});
      
      for (IBasicFile file : filesList) {
        this.readComponentsFile(model, context, file, DEF_CUSTOM_TYPE);
      }
    }
  }

  private void readWidgetStubComponents(MetaModel.Builder model, IThingReadContext context) throws ThingReadException {
    
	logger.info(String.format("Loading WIDGET components from: %s", WIDGETS_DIR));
	  
	List<IBasicFile> filesList = CdeEnvironment.getPluginRepositoryReader(WIDGETS_DIR).listFiles(null, 
			  new GenericBasicFileFilter(COMPONENT_FILENAME,CUSTOM_PROPS_FILE_EXTENSION), IReadAccess.DEPTH_ALL);
	  
    
    if(filesList != null) {
      
    	logger.debug(String.format("%s widget components found", filesList.size()));
      
    	IBasicFile[] filesArray = filesList.toArray(new IBasicFile[]{});
        
        Arrays.sort(filesArray, 
      		  new Comparator<IBasicFile>(){

  				@Override
  				public int compare(IBasicFile file1, IBasicFile file2) {
  					if (file1 == null && file2 == null){
  						return 0;
  					}else{
  						return file1.getFullPath().toLowerCase().compareTo(file2.getFullPath().toLowerCase());
  					}
  				}	
        });

      for (IBasicFile file : filesList) {
        this.readComponentsFile(model, context, file, DEF_WIDGET_STUB_TYPE);
      }
    }
  }

  // ------------------

  private void readPropertiesFile(MetaModel.Builder model, IThingReadContext context, IBasicFile file) throws ThingReadException {
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
      readProperty(model, context, propertyElem, file);
    }
  }

  private void readProperty(MetaModel.Builder model, IThingReadContext context, Element propertyElem, IBasicFile file) throws ThingReadException {
	  
    String className = null;
    try {
      className = Utils.getNodeText("Header/Override", propertyElem);
      
      if(StringUtils.isEmpty(className)) { className = "PropertyType"; }
      
      String name = Utils.getNodeText("Header/Name", propertyElem);
        
      IThingReader reader = context.getFactory().getReader(KnownThingKind.PropertyType, className, name);

      PropertyType.Builder prop = (PropertyType.Builder)reader.read(context, propertyElem, file.getPath());

      model.addProperty(prop);
    
    } catch(UnsupportedThingException ex) {
      ThingReadException ex2 = new ThingReadException("Failed to read property of class name '" +  className + "'.", ex);
      if(!this.continueOnError) { throw ex2; }

      // Just log and move on
      logger.fatal(null, ex2);
    }
  }

  // ------------------
  private void readComponentsFile(MetaModel.Builder model, IThingReadContext context, IBasicFile file, String defaultClassName)
          throws ThingReadException
  {
    Document doc;
    try {
      
      try {
        doc = Utils.getDocFromFile(file, null);
      } catch(Exception ex1) {
    	  throw ex1;
      }
    } catch (Exception ex) {
      ThingReadException ex2 = new ThingReadException("Cannot read components file '" + file.getFullPath() + "'.", ex);
      if(!this.continueOnError) { throw ex2; }

      // log and move on
      logger.fatal(null, ex2);
      return;
    }

    // One file can contain multiple definitions.
    @SuppressWarnings("unchecked")
    List<Element> componentElems = doc.selectNodes("//DesignerComponent");

    if (logger.isDebugEnabled() && componentElems.size() > 0) {
      logger.debug(String.format("\t%s [%s]", file.getPath(), componentElems.size()));
    }

    for (Element componentElem : componentElems) {
      readComponent(model, context, componentElem, file.getPath(), defaultClassName);
    }
  }

  private void readComponent(
          MetaModel.Builder model,
          IThingReadContext context,
          Element componentElem,
          String sourcePath,
          String defaultClassName) throws ThingReadException {
    String className = null;

    if(StringUtils.isEmpty(defaultClassName)) { defaultClassName = "ComponentType"; }

    try
    {
      className = Utils.getNodeText("Header/Override", componentElem);
      
      if(StringUtils.isEmpty(className)) { className = defaultClassName; }
      
      String name = Utils.getNodeText("Header/Name", componentElem);
      
      IThingReader reader = context.getFactory().getReader(KnownThingKind.ComponentType, className, name);

      ComponentType.Builder comp = (ComponentType.Builder)reader.read(context, componentElem, sourcePath);

      model.addComponent(comp);
    }
    catch(UnsupportedThingException ex)
    {
      ThingReadException ex2 = new ThingReadException("Failed to read component of class name '" +  className + "'.", ex);
      if(!this.continueOnError) { throw ex2; }

      // Just log and move on
      logger.fatal(null, ex2);
    }
  }
}