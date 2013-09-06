package pt.webdetails.cdf.dd;

import pt.webdetails.cdf.dd.util.Utils;

public class CdeConstants {
	
	public static final String PLUGIN_NAME = "pentaho-cdf-dd";
	
	/**
	 * solution folder for custom components, styles and templates
	 */
	public static final String SOLUTION_DIR = "cde";
	
	public static final String DEFAULT_STYLE = "Clean";

  public static final String DESIGNER_HEADER_TAG = "@HEADER@";

  public static final String DESIGNER_CDF_TAG = "@CDF@";

  public static final String DESIGNER_STYLES_TAG = "@STYLES@";

  public static final String DESIGNER_SCRIPTS_TAG = "@SCRIPTS@";

  public static final String DESIGNER_RESOURCE = "resources/cdf-dd.html";

  public static final String DESIGNER_STYLES_RESOURCE = "resources/styles.html";

  public static final String DESIGNER_SCRIPTS_RESOURCE = "resources/scripts.html";


  public static class MethodParams {
    public static final String DEBUG = "debug";

    public static final String ROOT = "root";

    public static final String SOLUTION = "solution";

    public static final String PATH = "path";

    public static final String FILE = "file";

    /**
     * JSON structure
     */
    public static final String CDF_STRUCTURE = "cdfstructure";

    public static final String DATA = "data";
  }

  public static final String FILE_NAME_TAG = "@FILENAME@";

  public static final String SERVER_URL_TAG = "@SERVERURL@";

  public static final String SERVER_URL_VALUE = Utils.getBaseUrl() + "content/pentaho-cdf-dd/";

  public static final String DATA_URL_TAG = "cdf-structure.js";

  public static final String DATA_URL_VALUE = Utils.getBaseUrl() + "content/pentaho-cdf-dd/Syncronize";

}
