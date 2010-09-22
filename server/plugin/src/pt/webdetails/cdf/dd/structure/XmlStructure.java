package pt.webdetails.cdf.dd.structure;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import pt.webdetails.cdf.dd.CdfStyles;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.SyncronizeCdfStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import pt.webdetails.cdf.dd.render.CdaRenderer;

@SuppressWarnings("unchecked")
public class XmlStructure implements IStructure {

  private IPentahoSession userSession = null;
  public static final String BASE_URL = "/" + PentahoSystem.getApplicationContext().getBaseUrl().split("[/]+")[2];
  public static final String SOLUTION_PATH = PentahoSystem.getApplicationContext().getSolutionPath("");

  public XmlStructure(IPentahoSession userSession) {
    this.userSession = userSession;

  }

  public void delete(HashMap parameters) throws Exception {

    System.out.println("deleting File:" + (String) parameters.get("file"));

    //1. Delete File
    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    if (!solutionRepository.removeSolutionFile((String) parameters.get("file"))) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_007_DELETE_FILE_EXCEPTION"));
    }

  }

  public JSON load(HashMap parameters) throws Exception {

    String filePath = (String) parameters.get("file");
    System.out.println("Loading File:" + filePath);

    JSONObject result = null;

    try {

      InputStream file = null;
      InputStream wcdfFile = null;
      //1. Get file
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
      if (solutionRepository.resourceExists(filePath)) {
        file = solutionRepository.getResourceInputStream(filePath, true);
      } else {
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
      file.close();


    } catch (FileNotFoundException e) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_001_LOAD_FILE_NOT_FOUND_EXCEPTION"));
    } catch (IOException e) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_003_LOAD_READING_FILE_EXCEPTION"));
    }

    return result;
  }


  public WcdfDescriptor loadWcdfDescriptor(final String wcdfFilePath) throws IOException {


    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    WcdfDescriptor wcdf = new WcdfDescriptor();

    if (solutionRepository.resourceExists(wcdfFilePath)) {
      Document wcdfDoc = solutionRepository.getResourceAsDocument(wcdfFilePath);

      wcdf.setTitle(XmlDom4JHelper.getNodeText("/cdf/title", wcdfDoc, ""));
      wcdf.setDescription(XmlDom4JHelper.getNodeText("/cdf/description", wcdfDoc, ""));
      wcdf.setAuthor(XmlDom4JHelper.getNodeText("/cdf/author", wcdfDoc, ""));
      wcdf.setStyle(XmlDom4JHelper.getNodeText("/cdf/style", wcdfDoc, CdfStyles.DEFAULTSTYLE));
    }

    return wcdf;

  }


  public void save(HashMap parameters) throws Exception {

    String filePath = (String) parameters.get("file");
    System.out.println("Saving File:" + filePath);

    try {

      //1. Build file parameters
      String[] file = buildFileParameters(filePath);

      //2. Publish file to pentaho repository
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

      if (filePath.indexOf("_tmp.cdfde") == -1 && solutionRepository.resourceExists(file[0] + file[1].replace(".cdfde", "_tmp.cdfde"))) {
        parameters.put("file", file[0] + file[1].replace(".cdfde", "_tmp.cdfde"));
        delete(parameters);
      }

      //int status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1], json.toString(2).getBytes("UTF-8"), true);
      int status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1], ((String) parameters.get("cdfstructure")).getBytes("UTF-8"), true);

      //3. Check publish result
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }

      //4. Write CDA File
      CdaRenderer renderer = CdaRenderer.getInstance();
      renderer.setContext((String) parameters.get("cdfstructure"));
      status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1].replace("cdfde","cda"), renderer.render().getBytes("UTF-8"), true);

      //5. Check publish result again.
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
      }
    } catch (PentahoAccessControlException e) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }

  }

  public void saveas(HashMap parameters) throws Exception {

    ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    //1. Read empty wcdf file
    File wcdfFile = new File(SyncronizeCdfStructure.EMPTY_WCDF_FILE);
    InputStream wcdfFileStream = new FileInputStream(wcdfFile);
    byte wcdfContent[] = new byte[(int) wcdfFile.length()];
    wcdfFileStream.read(wcdfContent);

    //2. Replace wcdf file title and description
    String wcdfContentAsString = new String(wcdfContent, "UTF-8");
    String title = (String) parameters.get("title");
    String description = (String) parameters.get("description");
    wcdfContentAsString = wcdfContentAsString.replaceFirst("@DASBOARD_TITLE@", title.length() > 0 ? title : "Dashboard");
    wcdfContentAsString = wcdfContentAsString.replaceFirst("@DASBOARD_DESCRIPTION@", description.length() > 0 ? description : "");

    //final String filePath = URLDecoder.decode((String) parameters.get("file"), "ISO-8859-1"); // jquery takes care of the encoding for us
    final String filePath = (String) parameters.get("file");
    final String[] file = buildFileParameters(filePath);

    //3. Publish new wcdf file
    int status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1], wcdfContentAsString.getBytes("UTF-8"), true);
    if (status == ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      //4. Save cdf structure
      parameters.put("file", filePath.replace(".wcdf", ".cdfde"));
      save(parameters);
    } else
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
  }

  public void newfile(HashMap parameters) throws Exception {

    //1. Read Empty Structure
    InputStream cdfstructure = new FileInputStream(new File(SyncronizeCdfStructure.EMPTY_STRUCTURE_FILE));

    //2. Save file
    parameters.put("cdfstructure", JsonUtils.readJsonFromInputStream(cdfstructure).toString());
    saveas(parameters);
  }

  public void savesettings(HashMap parameters) throws Exception {

    String filePath = (String) parameters.get("file");
    String titleStr = (String) parameters.get("title");
    String authorStr = (String) parameters.get("author");
    String descriptionStr = (String) parameters.get("description");
    String styleStr = (String) parameters.get("style");

    System.out.println("Saving settings file:" + filePath);

    try {

      //1. Build file parameters
      String[] file = buildFileParameters(filePath);

      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

      if (solutionRepository.resourceExists(filePath)) {

        Document wcdfDoc = solutionRepository.getResourceAsDocument(filePath);
        Node cdfNode = wcdfDoc.selectSingleNode("/cdf");

        setNodeValue(cdfNode, "title", titleStr);
        setNodeValue(cdfNode, "author", authorStr);
        setNodeValue(cdfNode, "description", descriptionStr);
        setNodeValue(cdfNode, "style", styleStr);

        int status = solutionRepository.publish(SOLUTION_PATH, file[0], file[1], wcdfDoc.asXML().getBytes("UTF-8"), true);

        if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
          throw new StructureException(Messages.getString("XmlStructure.ERROR_010_SAVE_SETTINGS_FAIL_EXCEPTION"));
        }
      } else
        throw new StructureException(Messages.getString("XmlStructure.ERROR_009_SAVE_SETTINGS_FILENOTFOUND_EXCEPTION"));


    } catch (Exception e) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_005_SAVE_PUBLISH_FILE_EXCEPTION"));
    }

  }

  private void setNodeValue(Node cdfNode, String elementName, String value) {

    Node node = cdfNode.selectSingleNode(elementName);

    if (node == null) {
      node = ((Element) cdfNode).addElement(elementName);
    }

    node.setText(value == null ? "" : value);

  }

  private String[] buildFileParameters(String filePath) {
    String[] result = {"", ""};
    String[] file = filePath.split("/");
    String fileName = file[file.length - 1];
    String path = filePath.substring(0, filePath.indexOf(fileName));
    result[0] = path;
    result[1] = fileName;
    return result;
  }
}

