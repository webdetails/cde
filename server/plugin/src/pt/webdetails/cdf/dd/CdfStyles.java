package pt.webdetails.cdf.dd;

import net.sf.json.JSONArray;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;

import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class CdfStyles {

  private static Log logger = LogFactory.getLog(CdfStyles.class);

  private static CdfStyles instance;
  
  private static String RESOURCE_STYLES_DIR = "resources/styles";
  private static String RESOURCE_STYLES_DIR_SOLUTION = "styles";
  
  public static final String DEFAULTSTYLE = "Clean";


  public CdfStyles() {

  }


  public void syncronize(IPentahoSession userSession, OutputStream out, IParameterProvider requestParams) throws Exception {

    //Read parameters
    Iterator<String> keys = requestParams.getParameterNames();
    HashMap<String, String> parameters = new HashMap<String, String>();
    while (keys.hasNext()) {
      String key = keys.next();
      parameters.put(key, requestParams.getStringParameter(key, null));
    }

    String operation = requestParams.getStringParameter("operation", "").toLowerCase();

    //Call sync method
    try {

      Class[] params = new Class[1];
      params[0] = HashMap.class;
      Method mthd = this.getClass().getMethod(operation, params);
      Object result = mthd.invoke(this, parameters);
      if (result != null) {
        JsonUtils.buildJsonResult(out, true, result);
      }

      JsonUtils.buildJsonResult(out, true, null);

    }
    catch (NoSuchMethodException e) {
      throw new Exception(Messages.getString("CdfTemplates.ERROR_001_INVALID_SYNCRONIZE_METHOD_EXCEPTION"));
    }

  }


  public Object liststyles(HashMap parameters) throws DashboardDesignerException {

    JSONArray result = new JSONArray();


    final File defaultStylesDir = new File(ResourceManager.PLUGIN_DIR + RESOURCE_STYLES_DIR);
    final File customStylesDir = new File(ResourceManager.SOLUTION_DIR + RESOURCE_STYLES_DIR_SOLUTION);
    
    final FilenameFilter htmlFilter = new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return name.endsWith(".html");
      }
    };

    //get bundled styles
    File[] styleFiles = defaultStylesDir.listFiles(htmlFilter);

    if (styleFiles == null) {
      logger.error("No styles directory found in resources");
      styleFiles = new File[0];
    }

    Arrays.sort(styleFiles);

    for (File styleFile : styleFiles) {
      final String name = styleFile.getName();
      result.add(name.substring(0,name.lastIndexOf('.')));
    }
   
    //append custom styles
    if(customStylesDir.isDirectory()){
      File[] customStyleFiles = customStylesDir.listFiles(htmlFilter);
      Arrays.sort(customStyleFiles);
      for (File styleFile : customStyleFiles) 
      {
        final String name = styleFile.getName();
        String styleEntry = name.substring(0,name.lastIndexOf('.'));
        if(!ArrayUtils.contains(styleFiles, styleEntry))
        {
          result.add(styleEntry);
        }
        else logger.error("Custom style " + styleEntry + " has same name as another style in resources.");
      } 
    }

    return result;
  }


  public static synchronized CdfStyles getInstance() {

    if (instance == null) {
      instance = new CdfStyles();
    }

    return instance;

  }

  public String getResourceLocation(String style) {
    File styleFile = new File(ResourceManager.PLUGIN_DIR + RESOURCE_STYLES_DIR + "/" + style + ".html");
    if(styleFile.exists()) return RESOURCE_STYLES_DIR + "/" + style + ".html";
    else return RESOURCE_STYLES_DIR_SOLUTION + "/" + style + ".html";
  }
}