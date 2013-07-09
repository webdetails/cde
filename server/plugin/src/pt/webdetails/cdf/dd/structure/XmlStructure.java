/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.structure;

import java.io.*;
import java.util.HashMap;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.SyncronizeCdfStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.MetaModelManager;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cggrunjs.CggRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cggrunjs.CggRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.IPropertyTypeSource;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.model.meta.writer.xml.XmlThingWriterFactory;
import pt.webdetails.cdf.dd.render.CdaRenderer;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.SaveFileStatus;
import pt.webdetails.cpf.repository.IRepositoryAccess;

public class XmlStructure implements IStructure
{
  private static final String ENCODING = "UTF-8";
  
  private static Log logger = LogFactory.getLog(XmlStructure.class);
  
  private final IPentahoSession userSession;
  
  public XmlStructure(IPentahoSession userSession)
  {
    this.userSession = userSession;
  }

  public void delete(HashMap<String, Object> parameters) throws StructureException
  {
    // 1. Delete File
    String filePath = (String)parameters.get("file");
    
    logger.info("Deleting File:" + filePath);
    
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    if(!repository.removeFile(filePath))
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_007_DELETE_FILE_EXCEPTION"));
    }
  }
  
  public JSON load(HashMap<String, Object> parameters) throws Exception
  {
    JSONObject result = null;

    InputStream file = null;
    InputStream wcdfFile = null;
    try
    {
      String cdeFilePath = (String)parameters.get("file");
      
      logger.info("Loading File:" + cdeFilePath);
    
      // 1. Read .CDFDE file
      IRepositoryAccess solutionRepository = PentahoRepositoryAccess.getRepository(userSession);
      if(solutionRepository.resourceExists(cdeFilePath))
      {
        file = solutionRepository.getResourceInputStream(cdeFilePath);
      }
      else
      {
        file = new FileInputStream(new File(SyncronizeCdfStructure.EMPTY_STRUCTURE_FILE_PATH));
      }

      JSON cdeData = JsonUtils.readJsonFromInputStream(file);
      
      // 3. Read .WCDF
      String wcdfFilePath = cdeFilePath.replace(".cdfde", ".wcdf");
      
      JSONObject wcdfData = loadWcdfDescriptor(wcdfFilePath).toJSON();
      
      result = new JSONObject();
      result.put("wcdf", wcdfData);
      result.put("data", cdeData);
    }
    catch (FileNotFoundException e)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_001_LOAD_FILE_NOT_FOUND_EXCEPTION"));
    }
    catch (IOException e)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_003_LOAD_READING_FILE_EXCEPTION"));
    }
    finally
    {
      IOUtils.closeQuietly(file);
      IOUtils.closeQuietly(wcdfFile);
    }

    return result;
  }

  public WcdfDescriptor loadWcdfDescriptor(String wcdfFilePath) throws IOException
  {
    WcdfDescriptor wcdf  = WcdfDescriptor.load(wcdfFilePath, userSession);
    
    return wcdf != null ? wcdf : new WcdfDescriptor();
  }
  
  public WcdfDescriptor loadWcdfDescriptor(Document wcdfDoc)
  {
    return WcdfDescriptor.fromXml(wcdfDoc);
  }

  public HashMap<String, String> save(HashMap<String, Object> parameters) throws Exception
  {
    final HashMap<String, String> result = new HashMap<String, String>();
    
    // 1. Get CDE file parameters
    String cdeRelFilePath = (String)parameters.get("file");
    String cdeFileDir  = FilenameUtils.getFullPath(cdeRelFilePath);
    String cdeFileName = FilenameUtils.getName(cdeRelFilePath);
    
    logger.info("Saving File:" + cdeRelFilePath);

    // 2. If not the CDE temp file, delete the temp file, if one exists
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    if(cdeRelFilePath.indexOf("_tmp.cdfde") == -1)
    {
      String cdeTempFilePath = cdeFileDir + cdeFileName.replace(".cdfde", "_tmp.cdfde");
      repository.removeFileIfExists(cdeTempFilePath);
    }
    
    // 3. CDE
    String cdfdeJsText = (String)parameters.get("cdfstructure");
    SaveFileStatus status = repository.publishFile(cdeFileDir, cdeFileName, safeGetEncodedBytes(cdfdeJsText), true);
    if (status != SaveFileStatus.OK)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
    }

    // 3. CDA
    CdaRenderer cdaRenderer = new CdaRenderer(cdfdeJsText);
    
    String cdaFileName = cdeFileName.replace(".cdfde", ".cda");
    
    // Any data sources?
    if(cdaRenderer.isEmpty())
    {
      repository.removeFileIfExists(Utils.joinPath(cdeFileDir, cdaFileName));
    }
    else
    {
      // throws Exception ????
      String cdaText = cdaRenderer.render();

      status = repository.publishFile(cdeFileDir, cdaFileName, safeGetEncodedBytes(cdaText), true);
      if(status != SaveFileStatus.OK)
      {
        throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }
    }

    // 4. CGG
    String wcdfFilePath = cdeRelFilePath.replace(".cdfde", ".wcdf");
    this.saveCgg(repository, wcdfFilePath);
    
    // TODO: Is this used?
    result.put("cdfde", "true");
    result.put("cda",   "true");
    result.put("cgg",   "true");

    return result;
  }
  
  private void saveCgg(IRepositoryAccess repository, String cdeRelFilePath) 
          throws ThingReadException, UnsupportedThingException, ThingWriteException
  {
    String wcdfFilePath = cdeRelFilePath.replace(".cdfde", ".wcdf");
    
    // Ontain an UPDATED dashboard object
    DashboardManager dashMgr = DashboardManager.getInstance();
    Dashboard dash = dashMgr.getDashboard(wcdfFilePath, userSession, /*bypassCacheRead*/false);
    
    CggRunJsThingWriterFactory cggWriteFactory = new CggRunJsThingWriterFactory();
    IThingWriter cggDashWriter = cggWriteFactory.getWriter(dash);
    CggRunJsDashboardWriteContext cggDashContext = new CggRunJsDashboardWriteContext(cggWriteFactory, dash, userSession);
    cggDashWriter.write(repository, cggDashContext, dash);
  }
  
  public void saveas(HashMap<String, Object> parameters) throws Exception
  {
    // 1. Read empty wcdf file
    File wcdfFile = new File(SyncronizeCdfStructure.EMPTY_WCDF_FILE_PATH);
    
    String wcdfContentAsString = FileUtils.readFileToString(wcdfFile, ENCODING);

    // 2. Fill-in wcdf file title and description
    String title = StringUtils.defaultIfEmpty((String)parameters.get("title"), "Dashboard");
    String description = StringUtils.defaultIfEmpty((String)parameters.get("description"), "");

    wcdfContentAsString = wcdfContentAsString
            .replaceFirst("@DASBOARD_TITLE@",       title)
            .replaceFirst("@DASBOARD_DESCRIPTION@", description);

    String filePath = (String)parameters.get("file");

    // 3. Publish new wcdf file
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    
    SaveFileStatus status = repository.publishFile(filePath, wcdfContentAsString.getBytes(ENCODING), true);
    if(status != SaveFileStatus.OK)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }
    
    // 4. Save cdf structure
    parameters.put("file", filePath.replace(".wcdf", ".cdfde"));
    
    save(parameters);
  }

  public void newfile(HashMap<String, Object> parameters) throws Exception
  {
    // 1. Read Empty Structure
    InputStream cdfstructure = null;
    try
    {
      cdfstructure = new FileInputStream(new File(SyncronizeCdfStructure.EMPTY_STRUCTURE_FILE_PATH));

      // 2. Save file
      parameters.put("cdfstructure", JsonUtils.readJsonFromInputStream(cdfstructure).toString());
      
      saveas(parameters);
    }
    finally
    {
      IOUtils.closeQuietly(cdfstructure);
    }
  }

  // .WCDF file
  // (called using reflection by SyncronizeCdfStructure)
  public void savesettings(HashMap<String, Object> parameters) throws StructureException
  {
    String wcdfFilePath = (String)parameters.get("file");
    logger.info("Saving settings file:" + wcdfFilePath);

    WcdfDescriptor wcdf = null;
    try
    {
      wcdf = WcdfDescriptor.load(wcdfFilePath, userSession);
    }
    catch(IOException ex)
    {
      // Access?
      throw new StructureException(Messages.getString("XmlStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION"));
    }

    if(wcdf == null)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION"));
    }

    // Update with client info
    wcdf.update(parameters);
    
    // Save to repository
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    String wcdfText = wcdf.toXml().asXML();
    SaveFileStatus status = repository.publishFile(wcdfFilePath, safeGetEncodedBytes(wcdfText), true);
    if(status != SaveFileStatus.OK)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_010_SAVE_SETTINGS_FAIL_EXCEPTION"));
    }
    
    // Save widget component.xml file?
    if(wcdf.isWidget())
    {
      publishWidgetComponentXml(wcdf);
    }
  }

  private void publishWidgetComponentXml(WcdfDescriptor wcdf)
  {
    String widgetPath = wcdf.getPath().replaceAll(".wcdf$", ".component.xml");
    
    logger.info("Saving widget component file:" + widgetPath);
    
    Document doc = createAndWriteWidgetComponentTypeXml(wcdf);
    if(doc == null)
    {
      // Failed
      return;
    }

    PentahoRepositoryAccess repository =
       (PentahoRepositoryAccess)PentahoRepositoryAccess.getRepository(userSession);

    repository.publishFile(widgetPath, safeGetEncodedBytes(doc.asXML()), true);
    try
    {
      DashboardDesignerContentGenerator.refresh(null);
    }
    catch(Exception ex)
    {
        logger.error("Error while refreshing the meta data cache", ex);
    }
  }

  private static Document createAndWriteWidgetComponentTypeXml(WcdfDescriptor wcdf)
  {
    WidgetComponentType widget  = createWidgetComponentType(wcdf);
    if(widget == null)
    {
      return null;
    }
    
    IThingWriterFactory factory = new XmlThingWriterFactory();
    IThingWriteContext context  = new DefaultThingWriteContext(factory, true);

    IThingWriter writer;
    try
    {
      writer = factory.getWriter(widget);
    }
    catch(UnsupportedThingException ex)
    {
      logger.error("No writer to write widget component type to XML", ex);
      return null;
    }

    Document doc = DocumentHelper.createDocument();
    try
    {
      writer.write(doc, context, widget);
    }
    catch (ThingWriteException ex)
    {
      logger.error("Failed writing widget component type to XML", ex);
      return null;
    }

    return doc;
  }
  
  private static WidgetComponentType createWidgetComponentType(WcdfDescriptor wcdf)
  {
    WidgetComponentType.Builder builder = new WidgetComponentType.Builder();

    String name = wcdf.getWidgetName();
    builder
      .setName(name)
      .setLabel(name + " Widget")
      .setCategory("WIDGETS")
      .setCategoryLabel("Widgets")
      .addAttribute("widget", "true")
      .addAttribute("wcdf", wcdf.getPath());

    builder.useProperty(null, "htmlObject");

    for(String paramName : wcdf.getWidgetParameters())
    {
      // Create an own property
      PropertyType.Builder prop = new PropertyType.Builder();

      prop
        .setName(paramName)
        .setLabel("Parameter " + paramName)
        .setTooltip("What dashboard parameter should map to widget parameter '" + paramName + "'?");

      prop.setInputType("Parameter");

      builder.addProperty(prop);

      // And use it
      builder.useProperty(null, paramName);
    }

    // Use the global model to build the component in.
    MetaModel model = MetaModelManager.getInstance().getModel();
    IPropertyTypeSource propSource = model.getPropertyTypeSource();
    try
    {
      return (WidgetComponentType)builder.build(propSource);
    }
    catch (ValidationException ex)
    {
      logger.error(ex);
      return null;
    }
  }

  private static byte[] safeGetEncodedBytes(String text)
  {
    try
    {
      return text.getBytes(ENCODING);
    }
    catch(UnsupportedEncodingException ex)
    {
      // Never happens
      return null;
    }
  }
}
