package pt.webdetails.cdf.dd.structure;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.CdfStyles;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.SyncronizeCdfStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.*;
import java.util.HashMap;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.render.CdaRenderer;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.cdw.CdwRenderer;
import pt.webdetails.cdf.dd.render.components.ComponentDefinition;
import pt.webdetails.cdf.dd.render.components.ComponentManager;
import pt.webdetails.cdf.dd.render.properties.PropertyDefinition;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.SaveFileStatus;

public class XmlStructure implements IStructure
{

  private static final String ENCODING = "UTF-8";
  private IPentahoSession userSession = null;
  private static Log logger = LogFactory.getLog(XmlStructure.class);

  public XmlStructure(IPentahoSession userSession)
  {
    this.userSession = userSession;

  }

  public void delete(HashMap<String, Object> parameters) throws StructureException
  {

    logger.info("Deleting File:" + (String) parameters.get("file"));

    //1. Delete File

    PentahoRepositoryAccess solutionRepository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);
    if (!solutionRepository.removeFile((String) parameters.get("file")))
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_007_DELETE_FILE_EXCEPTION"));
    }

  }

  public JSON load(HashMap<String, Object> parameters) throws Exception
  {


    String filePath = (String) parameters.get("file");
    logger.info("Loading File:" + filePath);

    JSONObject result = null;

    InputStream file = null;
    InputStream wcdfFile = null;
    try
    {
      //1. Get file
      PentahoRepositoryAccess solutionRepository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);
      if (solutionRepository.resourceExists(filePath))
      {
        file = solutionRepository.getResourceInputStream(filePath);
      }
      else
      {
        file = new FileInputStream(new File(SyncronizeCdfStructure.EMPTY_STRUCTURE_FILE));
      }

      //2. Read cdfStructure
      JSON data = JsonUtils.readJsonFromInputStream(file);
      result = new JSONObject();

      //3. Read wcdf file
      String wcdfFilePath = filePath.replace(".cdfde", ".wcdf");

      JSONObject wcdf = loadWcdfDescriptor(wcdfFilePath).toJSON();

      result.put("data", data);
      result.put("wcdf", wcdf);
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

  public WcdfDescriptor loadWcdfDescriptor(final String wcdfFilePath) throws IOException
  {
    PentahoRepositoryAccess solutionRepository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);

    if (solutionRepository.resourceExists(wcdfFilePath))
    {
      Document wcdfDoc = solutionRepository.getResourceAsDocument(wcdfFilePath);
      WcdfDescriptor wcdf = loadWcdfDescriptor(wcdfDoc);
      wcdf.setWcdfPath(wcdfFilePath);
      return wcdf;

    }
    else
    {
      return new WcdfDescriptor();
    }

  }

  public WcdfDescriptor loadWcdfDescriptor(final Document wcdfDoc) throws IOException
  {
    WcdfDescriptor wcdf = new WcdfDescriptor();
    wcdf.setTitle(XmlDom4JHelper.getNodeText("/cdf/title", wcdfDoc, ""));
    wcdf.setDescription(XmlDom4JHelper.getNodeText("/cdf/description", wcdfDoc, ""));
    wcdf.setWidget(XmlDom4JHelper.getNodeText("/cdf/widget", wcdfDoc, "false").equals("true"));
    wcdf.setWidgetName(XmlDom4JHelper.getNodeText("/cdf/widgetName", wcdfDoc, ""));
    wcdf.setAuthor(XmlDom4JHelper.getNodeText("/cdf/author", wcdfDoc, ""));
    wcdf.setStyle(XmlDom4JHelper.getNodeText("/cdf/style", wcdfDoc, CdfStyles.DEFAULTSTYLE));
    wcdf.setRendererType(XmlDom4JHelper.getNodeText("/cdf/rendererType", wcdfDoc, "blueprint"));
    wcdf.setWidgetParameters(XmlDom4JHelper.getNodeText("/cdf/widgetParameters", wcdfDoc, "").split(","));
    return wcdf;
  }

  public HashMap<String, String> save(HashMap<String, Object> parameters) throws Exception
  {

    boolean cdfdeResult = true;
    boolean cdaResult = true;
    boolean cggResult = true;

    final HashMap<String, String> result = new HashMap<String, String>();
    PentahoRepositoryAccess.SaveFileStatus status = SaveFileStatus.OK;

    String filePath = (String) parameters.get("file");
    logger.info("Saving File:" + filePath);

    try
    {

      //1. Build file parameters

      String path = FilenameUtils.getFullPath(filePath);
      String cdeFileName = FilenameUtils.getName(filePath);

      //2. Publish file to pentaho repository

      PentahoRepositoryAccess repository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);
      if (filePath.indexOf("_tmp.cdfde") == -1 && repository.resourceExists(path + cdeFileName.replace(".cdfde", "_tmp.cdfde")))
      {
        parameters.put("file", path + cdeFileName.replace(".cdfde", "_tmp.cdfde"));
        delete(parameters);
      }
      byte[] fileContents = ((String) parameters.get("cdfstructure")).getBytes(ENCODING);
      switch (repository.publishFile(path, cdeFileName, fileContents, true))
      {
        case FAIL:
            cdfdeResult = false;
            throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }

      status = SaveFileStatus.OK;

      //3. Write CDA File
      CdaRenderer cdaRenderer = CdaRenderer.getInstance();
      cdaRenderer.setContext((String) parameters.get("cdfstructure"));
      String cdaFileName = cdeFileName.replace(".cdfde", ".cda");//TODO: replace these with a proper extension-replacing func
      if (cdaRenderer.isEmpty())
      {
        deleteFileIfExists(repository, path, cdaFileName);
      }
      else
      {
        status = repository.publishFile(path, cdaFileName, cdaRenderer.render().getBytes(ENCODING), true);
      }

      if (status != SaveFileStatus.OK)
      {
        cdaResult = false;
        throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }

      //4. Write only JS file, because CDW is no longer used by CGG
      String wcdfFilePath = filePath.replace(".cdfde", ".wcdf");
      CdwRenderer cdwRenderer = new CdwRenderer((String) parameters.get("cdfstructure"), loadWcdfDescriptor(wcdfFilePath));
      cdwRenderer.render(path, cdeFileName);

      //5. Check publish result again.
      if (status != SaveFileStatus.OK)
      {
        cggResult = false;
        throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }

    }
    catch (PentahoAccessControlException e)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }

    result.put("cdfde", new Boolean(cdfdeResult).toString());
    result.put("cda", new Boolean(cdaResult).toString());
    result.put("cgg", new Boolean(cggResult).toString());

    return result;
  }

  private void deleteFileIfExists(PentahoRepositoryAccess solutionRepository, String path, String fileName)
  {
    String fullName = path + fileName;
    fullName = fullName.replaceAll("//+", "/");
    solutionRepository.removeFileIfExists(fullName);
  }

  public void saveas(HashMap<String, Object> parameters) throws Exception
  {

    PentahoRepositoryAccess repository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);

    //1. Read empty wcdf file
    File wcdfFile = new File(SyncronizeCdfStructure.EMPTY_WCDF_FILE);
    String wcdfContentAsString = FileUtils.readFileToString(wcdfFile, ENCODING);

    //2. Replace wcdf file title and description
    String title = (String) parameters.get("title");
    String description = (String) parameters.get("description");
    wcdfContentAsString = wcdfContentAsString.replaceFirst("@DASBOARD_TITLE@", title.length() > 0 ? title : "Dashboard");
    wcdfContentAsString = wcdfContentAsString.replaceFirst("@DASBOARD_DESCRIPTION@", description.length() > 0 ? description : "");

    //final String filePath = URLDecoder.decode((String) parameters.get("file"), "ISO-8859-1"); // jquery takes care of the encoding for us
    final String filePath = (String) parameters.get("file");

    //3. Publish new wcdf file
    switch (repository.publishFile(filePath, wcdfContentAsString.getBytes(ENCODING), true))
    {
      case OK:
        //4. Save cdf structure
        parameters.put("file", filePath.replace(".wcdf", ".cdfde"));
        save(parameters);
        break;
      case FAIL:
        throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }
  }

  public void newfile(HashMap<String, Object> parameters) throws Exception
  {

    //1. Read Empty Structure
    InputStream cdfstructure = null;

    try
    {
      cdfstructure = new FileInputStream(new File(SyncronizeCdfStructure.EMPTY_STRUCTURE_FILE));

      //2. Save file
      parameters.put("cdfstructure", JsonUtils.readJsonFromInputStream(cdfstructure).toString());
      saveas(parameters);
    }
    finally
    {
      IOUtils.closeQuietly(cdfstructure);
    }

  }

  public void savesettings(HashMap<String, Object> parameters) throws Exception
  {

    String filePath = (String) parameters.get("file");
    String titleStr = (String) parameters.get("title");
    String authorStr = (String) parameters.get("author");
    String descriptionStr = (String) parameters.get("description");
    String styleStr = (String) parameters.get("style");
    String widgetNameStr = (String) parameters.get("widgetName");
    Boolean isWidget = "true".equals(parameters.get("widget"));
    String rendererType = (String) parameters.get("rendererType");
    Object widgetParams = parameters.get("widgetParameters");
    String widgetParameters[] = null;
    if (widgetParams instanceof String[])
    {
      widgetParameters = (String[]) widgetParams;
    }
    else if (widgetParams != null)
    {
      widgetParameters = new String[1];
      widgetParameters[0] = widgetParams.toString();
    }

    logger.info("Saving settings file:" + filePath);

    try
    {
      PentahoRepositoryAccess repository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);

      if (repository.resourceExists(filePath))
      {

        Document wcdfDoc = repository.getResourceAsDocument(filePath);
        Node cdfNode = wcdfDoc.selectSingleNode("/cdf");

        //only override explicitly set elements, leave others as they are (initStyles will only set style)
        if (parameters.containsKey("title"))
        {
          setNodeValue(cdfNode, "title", titleStr);
        }
        if (parameters.containsKey("author"))
        {
          setNodeValue(cdfNode, "author", authorStr);
        }
        if (parameters.containsKey("description"))
        {
          setNodeValue(cdfNode, "description", descriptionStr);
        }
        if (parameters.containsKey("style"))
        {
          setNodeValue(cdfNode, "style", styleStr);
        }
        if (parameters.containsKey("rendererType"))
        {
          setNodeValue(cdfNode, "rendererType", rendererType);
        }
        if (parameters.containsKey("widget"))
        {
          setNodeValue(cdfNode, "widget", isWidget ? "true" : "false");
        }
        if (parameters.containsKey("widgetName"))
        {
          setNodeValue(cdfNode, "widgetName", widgetNameStr);
        }
        if (parameters.containsKey("widgetParameters"))
        {
          StringBuilder sb = new StringBuilder();
          for (String s : widgetParameters)
          {
            sb.append(s);
            sb.append(",");
          }
          setNodeValue(cdfNode, "widgetParameters", sb.toString().replaceAll(",$", ""));
        }


        if (isWidget)
        {
          WcdfDescriptor wcdf = loadWcdfDescriptor(wcdfDoc);
          wcdf.setWcdfPath(filePath);
          generateComponentXml(wcdf);
        }
        //Save
        switch (repository.publishFile(filePath, wcdfDoc.asXML().getBytes(ENCODING), true))
        {
          case FAIL:
            throw new StructureException(Messages.getString("XmlStructure.ERROR_010_SAVE_SETTINGS_FAIL_EXCEPTION"));
        }

      }
      else
      {
        throw new StructureException(Messages.getString("XmlStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION"));
      }

    }
    catch (Exception e)
    {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }

  }

  private void setNodeValue(Node cdfNode, String elementName, String value)
  {

    Node node = cdfNode.selectSingleNode(elementName);

    if (node == null)
    {
      node = ((Element) cdfNode).addElement(elementName);
    }

    node.setText(value == null ? "" : value);

  }

  private void generateComponentXml(WcdfDescriptor wcdf)
  {
    if (!wcdf.isWidget())
    {
      return;
    }
    ComponentDefinition cd = new ComponentDefinition();
    cd.setName( wcdf.getWidgetName() );
    cd.setIName("widget" + wcdf.getWidgetName());
    cd.setDescription(wcdf.getWidgetName() + " Widget");
    cd.setCatDescription("Widgets");
    cd.setCategory("WIDGETS");
    cd.addMetadata("widget", "true");
    cd.addMetadata("wcdf", wcdf.getWcdfPath());
    cd.addProperty("htmlObject");
    for (String s : wcdf.getWidgetParameters())
    {
      cd.addProperty(s, generateParameterProperty(s));
    }
    Document doc = DocumentHelper.createDocument();
    cd.toXML(doc);
    PentahoRepositoryAccess repository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository(userSession);
    String path = wcdf.getWcdfPath().replaceAll(".wcdf$", ".component.xml");
    try
    {
      repository.publishFile(path, doc.asXML().getBytes(ENCODING), true);
      DashboardDesignerContentGenerator.refresh(null);
    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }

  private PropertyDefinition generateParameterProperty(String name)
  {
    PropertyDefinition pd = new PropertyDefinition();
    pd.setName(name);
    pd.setDescription("Parameter " + name);
    pd.setTooltip("What dashboard parameter should map to widget parameter " + name + "?");
    pd.setInputType("Parameter");
    return pd;
  }
}
