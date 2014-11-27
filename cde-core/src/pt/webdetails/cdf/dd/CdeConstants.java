package pt.webdetails.cdf.dd;

/**
 * Just constants. Inner classes serve as namespaces
 */
public class CdeConstants {
	
  public static final String DEFAULT_STYLE = "Clean";

  public static final String DESIGNER_HEADER_TAG = "@HEADER@";

  public static final String DESIGNER_CDF_TAG = "@CDF@";

  public static final String DESIGNER_STYLES_TAG = "@STYLES@";

  public static final String DESIGNER_SCRIPTS_TAG = "@SCRIPTS@";
  
  public static final String DASHBOARD_TITLE_TAG = "@DASBOARD_TITLE@";
  
  public static final String DASHBOARD_DESCRIPTION_TAG = "@DASBOARD_DESCRIPTION@";

  public static final String DESIGNER_RESOURCE = "resources/cdf-dd.html";

  public static final String DESIGNER_RESOURCE_DEFAULT = "resources/cdf-dd-default.html";

  public static final String DESIGNER_STYLES_RESOURCE = "resources/styles.html";

  public static final String DESIGNER_SCRIPTS_RESOURCE = "resources/scripts.html";

  /**
   * Inner paths relative to plugin repository dir
   */
  public static class SolutionFolders {
    public static final String COMPONENTS = "components";
    public static final String WIDGETS = "widgets";
    public static final String STYLES = "styles";
    public static final String TEMPLATES = "templates";
  }


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
  
  public static class Tags {
    public  static final String DASHBOARD_HEADER_TAG  = "\\@HEADER\\@";

    public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";

    public static final String DASHBOARD_FOOTER_TAG  = "\\@FOOTER\\@";

    public static final String FILE_NAME_TAG = "@FILENAME@";

    public static final String SERVER_URL_TAG = "@SERVERURL@";

    public static class Api {
      public static final String RENDERER = "@CDE_RENDERER_API@";
    }
  }

  public static final String FILE_NAME_TAG = "@FILENAME@";

  public static final String SERVER_URL_TAG = "@SERVERURL@";

  public static final String DATA_URL_TAG = "@DATAURL@";//formerly known as cdf-structure.js
  
  public static final String RESOURCE_FOOTER = "resources/patch-footer.html";

  public static final String RESOURCE_FOOTER_REQUIRE = "resources/patch-footer-require.html";
  
  public  static final String DASHBOARD_HEADER_TAG  = "\\@HEADER\\@";
  
  public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  
  public static final String DASHBOARD_FOOTER_TAG  = "\\@FOOTER\\@";

}
