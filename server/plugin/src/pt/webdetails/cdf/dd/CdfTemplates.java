package pt.webdetails.cdf.dd;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.structure.StructureException;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import pt.webdetails.cdf.dd.util.Utils;

@SuppressWarnings("unchecked")
public class CdfTemplates {

  private IPentahoSession userSession = null;
  
  private static String SOLUTION_PATH = PentahoSystem.getApplicationContext().getSolutionPath("");
  private static String CDF_DD_TEMPLATES = "system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/resources/templates";
  private static String CDF_DD_TEMPLATES_CUSTOM = DashboardDesignerContentGenerator.SOLUTION_DIR + "/templates";
  
  public static String DEFAULT_TEMPLATE_DIR = PentahoSystem.getApplicationContext().getSolutionPath( CDF_DD_TEMPLATES);
  public static String CUSTOM_TEMPLATE_DIR = PentahoSystem.getApplicationContext().getSolutionPath( CDF_DD_TEMPLATES_CUSTOM);
  public static String RESOURCE_TEMPLATE_DIR = Utils.getBaseUrl() + "content/pentaho-cdf-dd/getResource?resource=/resources/templates/";
  public static String UNKNOWN_IMAGE = RESOURCE_TEMPLATE_DIR + "unknown.png";



  public CdfTemplates(IPentahoSession userSession) {

    this.userSession = userSession;

  }

  /**
   * Invokes operation through reflection
   * @param out
   * @param requestParams
   * @throws Exception
   */
  public void syncronize(final OutputStream out, final IParameterProvider requestParams) throws Exception {

    //Read parameters
    final Iterator<String> keys = requestParams.getParameterNames();
    final HashMap<String, String> parameters = new HashMap<String, String>();
    while (keys.hasNext()) {
      final String key = keys.next();
      parameters.put(key, requestParams.getStringParameter(key, null));
    }

    final String operation = requestParams.getStringParameter("operation", "").toLowerCase();

    //Call sync method
    try {

      final Class[] params = new Class[1];
      params[0] = HashMap.class;
      final Method mthd = this.getClass().getMethod(operation, params);
      final Object result = mthd.invoke(this, parameters);
      if (result != null) {
        JsonUtils.buildJsonResult(out, true, result);
      }

      JsonUtils.buildJsonResult(out, true, null);

    }
    catch (NoSuchMethodException e) {
      throw new Exception(Messages.getString("CdfTemplates.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    }

  }

  public void save(final HashMap parameters) throws Exception {

    final String fileName = (String) parameters.get("file");
    System.out.println("Saving File:" + fileName);

    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    String cdfStructure = (String) parameters.get(DashboardDesignerContentGenerator.PathParams.CDF_STRUCTURE);
    byte[] fileData = cdfStructure.getBytes("UTF-8");
    final int status = solutionRepository.publish(SOLUTION_PATH, CDF_DD_TEMPLATES_CUSTOM, fileName, fileData, true);

    if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      throw new StructureException(Messages.getString("XmlStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION"));
    }
  }

  public Object load(final HashMap parameters) {

    Object result = new JSONArray();

    try {

      final File defaultTemplatesDir = new File(DEFAULT_TEMPLATE_DIR);

      final FilenameFilter jsonFilter = new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
          return name.endsWith(".cdfde");
        }
      };

      File[] jsonFiles = defaultTemplatesDir.listFiles(jsonFilter);
      if (jsonFiles != null) {
        loadFiles(jsonFiles, (JSONArray) result, "default");
      } else
        result = Messages.getString("CdfTemplates.ERROR_002_LOADING_TEMPLATES_EXCEPTION");

      final File customTemplates = new File(CUSTOM_TEMPLATE_DIR);
      if (customTemplates.exists()) {
        jsonFiles = customTemplates.listFiles(jsonFilter);
        if (jsonFiles != null) {
          loadFiles(jsonFiles, (JSONArray) result, "custom");
        }
      }

    } catch (FileNotFoundException e) {
      result = Messages.getString("CdfTemplates.ERROR_002_LOADING_EXCEPTION");
    } catch (IOException e) {
      result = Messages.getString("CdfTemplates.ERROR_002_LOADING_EXCEPTION");
    }

    return result;

  }

  private void loadFiles(final File[] jsonFiles, final JSONArray result, final String type) throws FileNotFoundException, IOException {

    Arrays.sort(jsonFiles, new Comparator<File>() {
      private Collator c = Collator.getInstance();

      public int compare(final File f1, final File f2) {
        return c.compare(f1.getName(), f2.getName());
      }
    });

    for (int i = 0; i < jsonFiles.length; i++) {
      final JSONObject template = new JSONObject();
      final File image = new File(jsonFiles[i].getAbsolutePath().replace(".cdfde", ".png"));
      final String imgResourcePath = image.exists() ? RESOURCE_TEMPLATE_DIR + image.getName() : UNKNOWN_IMAGE;
      template.put("img", imgResourcePath);
      template.put("type", type);
      FileInputStream cdfdeFile = null;
      try{
        cdfdeFile = new FileInputStream(jsonFiles[i]);
        template.put("structure", JsonUtils.readJsonFromInputStream(cdfdeFile));
        result.add(template);
      }
      finally {
        IOUtils.closeQuietly(cdfdeFile);
      }

    }
  }

}
