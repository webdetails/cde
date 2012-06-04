/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.UUID;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;


import pt.webdetails.cdf.dd.datasources.DataSourceReader;
import pt.webdetails.cdf.dd.olap.OlapUtils;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.components.ComponentManager;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;

import pt.webdetails.cdf.dd.packager.Packager;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.audit.CpfAuditHelper;
import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cpf.VersionChecker;

public class DashboardDesignerContentGenerator extends SimpleContentGenerator
{

  public static final String PLUGIN_NAME = "pentaho-cdf-dd";
  public static final String SYSTEM_PATH = "system";
  public static final String PLUGIN_PATH = SYSTEM_PATH + "/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/";
  public static final String MOLAP_PLUGIN_PATH = SYSTEM_PATH + "/MOLA/";
  /**
   * solution folder for custom components, styles and templates
   */
  public static final String SOLUTION_DIR = "cde";
  public static final String SERVER_URL_VALUE = Utils.getBaseUrl() + "content/pentaho-cdf-dd/";
  
  
  private static Log logger = LogFactory.getLog(DashboardDesignerContentGenerator.class);
  private static final long serialVersionUID = 1L;
  private static final String MIME_TYPE = "text/html";
  private static final String CSS_TYPE = "text/css";
  private static final String JAVASCRIPT_TYPE = "text/javascript";
  private static final String FILE_NAME_TAG = "@FILENAME@";
  private static final String SERVER_URL_TAG = "@SERVERURL@";
  private static final String DESIGNER_RESOURCE = "resources/cdf-dd.html";
  private static final String DESIGNER_STYLES_RESOURCE = "resources/styles.html";
  private static final String DESIGNER_SCRIPTS_RESOURCE = "resources/scripts.html";
  private static final String DESIGNER_HEADER_TAG = "@HEADER@";
  private static final String DESIGNER_CDF_TAG = "@CDF@";
  private static final String DESIGNER_STYLES_TAG = "@STYLES@";
  private static final String DESIGNER_SCRIPTS_TAG = "@SCRIPTS@";
  private static final String DATA_URL_TAG = "cdf-structure.js";
  private static final String DATA_URL_VALUE = Utils.getBaseUrl() + "content/pentaho-cdf-dd/Syncronize";
  /**
   * 1 week cache
   */
  private static final int RESOURCE_CACHE_DURATION = 3600 * 24 * 8; //1 week cache
  private static final int NO_CACHE_DURATION = 0;
  private static final String EXTERNAL_EDITOR_PAGE = "resources/ext-editor.html";
  private static final String COMPONENT_EDITOR_PAGE = "resources/cdf-dd-component-editor.html";
  private Packager packager;

  public enum FileTypes
  {

    JPG, JPEG, PNG, GIF, BMP, JS, CSS, HTML, HTM, XML
  }
  public static final EnumMap<FileTypes, String> mimeTypes = new EnumMap<FileTypes, String>(FileTypes.class);

  /**
   * Parameters received by content generator
   */
  protected static class MethodParams
  {

    /**
     * Debug flag
     */
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

  static
  {
    /*
     * Image types
     */
    mimeTypes.put(FileTypes.JPG, "image/jpeg");
    mimeTypes.put(FileTypes.JPEG, "image/jpeg");
    mimeTypes.put(FileTypes.PNG, "image/png");
    mimeTypes.put(FileTypes.GIF, "image/gif");
    mimeTypes.put(FileTypes.BMP, "image/bmp");

    /*
     * HTML (and related) types
     */
    // Deprecated, should be application/javascript, but IE doesn't like that
    mimeTypes.put(FileTypes.JS, "text/javascript");
    mimeTypes.put(FileTypes.HTM, "text/html");
    mimeTypes.put(FileTypes.HTML, "text/html");
    mimeTypes.put(FileTypes.CSS, "text/css");
    mimeTypes.put(FileTypes.XML, "text/xml");
  }

  public DashboardDesignerContentGenerator()
  {
    try
    {
      init();
    }
    catch (IOException ex)
    {
      logger.error("Failed to initialize!");
    }
  }

//  @Override
//  protected boolean invokeMethod(OutputStream out, String methodName, Method method) throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {
//    if(canAccessMethod(method)){
//      method.invoke(this, getRequestParameters(), out);
//      return true;
//    }
//    logger.error("Method " + methodName + " not exposed or user does not have required permissions.");
//    getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
//    return false;
//  }
  
  @Override
  public void createContent()
  {
    // Make sure we have the env. correctly inited
    if (SolutionReposHelper.getSolutionRepositoryThreadVariable() == null && PentahoSystem.getObjectFactory().objectDefined(ISolutionRepository.class.getSimpleName()))
    {
      SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession()));
    }
    
    super.createContent();
  }

//  @Override
//  protected Class<?>[] getCGMethodParams() {
//    return new Class<?>[]{IParameterProvider.class, OutputStream.class};
//  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void syncronize(final OutputStream out) throws Exception
  {
    // 0 - Check security. Caveat: if no path is supplied, then we're in the new parameter
    IParameterProvider requestParams = getRequestParameters();
    final String path = ((String) requestParams.getParameter(MethodParams.FILE)).replaceAll("cdfde", "wcdf");
    if (requestParams.hasParameter(MethodParams.PATH) && !RepositoryAccess.getRepository(userSession).hasAccess(path, FileAccess.EXECUTE))
    {
      final HttpServletResponse response = getResponse();
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      logger.warn("Access denied for the syncronize method: " + path + " by user " + userSession.getName());
      return;
    }

    final SyncronizeCdfStructure syncCdfStructure = new SyncronizeCdfStructure();
    syncCdfStructure.syncronize(userSession, out, requestParams);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void newdashboard(final OutputStream out) throws Exception
  {
    this.edit(out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getcomponentdefinitions(OutputStream out) throws Exception
  {
    // The ComponentManager is responsible for producing the component definitions
    ComponentManager engine = ComponentManager.getInstance();
    if (engine.getCdaDefinitions() == null)
    {
      // We want to acquire a handle for the CDA plugin.
      JSON json = getCdaDefs(false);
      engine.parseCdaDefinitions(json);
    }
    // Get and output the definitions
    out.write(engine.getDefinitions().getBytes());
  }
  
//  //TODO: not used?
//  public void getcomponentimplementations(IParameterProvider pathParams, OutputStream out) throws Exception
//  {
//    ComponentManager engine = ComponentManager.getInstance();
//    out.write(engine.getImplementations().getBytes());
//  }

  /**
   * Re-initializes the designer back-end.
   *
   * @author pdpi
   */
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void refresh(OutputStream out) throws Exception
  {
    DependenciesManager.refresh();
    ComponentManager.getInstance().refresh();
    ComponentManager.getInstance().parseCdaDefinitions(getCdaDefs(true));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getcontent(OutputStream out) throws Exception
  {
    Dashboard dashboard = DashboardFactory.getInstance().loadDashboard(parameterProviders, this);
    writeOut(out, dashboard.getContent());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getheaders(final OutputStream out) throws Exception
  {
    Dashboard dashboard = DashboardFactory.getInstance().loadDashboard(parameterProviders, this);
    writeOut(out, dashboard.getHeader());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void render(final OutputStream out) throws IOException // Exception
  {
    // Check security
    if ( !RepositoryAccess.getRepository(userSession).hasAccess(getWcdfRelativePath(getRequestParameters()), FileAccess.EXECUTE))
    {
      writeOut(out, "Access Denied or File Not Found.");
      return;
    }

    final long start = System.currentTimeMillis();        
    UUID uuid = CpfAuditHelper.startAudit(PLUGIN_NAME, "render", getObjectName(), this.userSession, this, getRequestParameters());       
    
    
    // Response
    try
    {
      setResponseHeaders(MIME_TYPE, 0, null);
    }
    catch (Exception e)
    {
      logger.warn(e.toString());
    }
    
    try {
      // Build pieces: render dashboard, footers and headers
      logger.info("[Timing] CDE Starting Dashboard Rendering: " + (new SimpleDateFormat("HH:mm:ss.SSS")).format(new Date()));
      Dashboard dashboard = DashboardFactory.getInstance().loadDashboard(parameterProviders, this);
      writeOut(out, dashboard.render(getRequestParameters()));
      //out.write(dashboard.render(parameterProviders.get("request")).getBytes(ENCODING));
      logger.info("[Timing] CDE Finished Dashboard Rendering: " + (new SimpleDateFormat("H:m:s.S")).format(new Date()));
    }
    catch (FileNotFoundException e){
      //could not open cdfde
      String msg = "File not found: " + e.getLocalizedMessage();
      logger.error(msg);
      writeOut(out, msg);
    }
    CpfAuditHelper.endAudit(PLUGIN_NAME, "render", getObjectName(), this.userSession, this, start, uuid, System.currentTimeMillis());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getcssresource(final OutputStream out) throws Exception
  {
    setResponseHeaders(CSS_TYPE, RESOURCE_CACHE_DURATION, null);
    getresource(out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getjsresource(final OutputStream out) throws Exception
  {
    setResponseHeaders(JAVASCRIPT_TYPE, RESOURCE_CACHE_DURATION, null);
    final HttpServletResponse response = getResponse();
    // Set cache for 1 year, give or take.
    response.setHeader("Cache-Control", "max-age=" + 60 * 60 * 24 * 365);
    try
    {
      getresource(out);
    }
    catch (SecurityException e)
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    catch (FileNotFoundException e)
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getuntypedresource(final OutputStream out) throws IOException
  {
    final HttpServletResponse response = getResponse();
    response.setHeader("Content-Type", "text/plain");
    response.setHeader("content-disposition", "inline");

    getresource(out);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getimg(final OutputStream out) throws Exception
  {
    String pathString = getPathParameters().getStringParameter(MethodParams.PATH, "");
    String resource;
    if (pathString.split("/").length > 2)
    {
      resource = pathString.replaceAll("^/.*?/", "");
    }
    else
    {
      resource = getRequestParameters().getStringParameter(MethodParams.PATH, "");
    }
    resource = resource.startsWith("/") ? resource : "/" + resource;

    String[] path = resource.split("/");
    String[] fileName = path[path.length - 1].split("\\.");

    final String mimeType;
    switch (FileTypes.valueOf(fileName[fileName.length - 1].toUpperCase()))
    {
      case PNG:
        mimeType = "image/png";
        break;
      case JPG:
      case JPEG:
        mimeType = "image/jpeg";
        break;
      case GIF:
        mimeType = "image/gif";
        break;
      case BMP:
        mimeType = "image/bmp";
        break;
      default:
        mimeType = "";
    }
    setResponseHeaders(mimeType, RESOURCE_CACHE_DURATION, null);
    final HttpServletResponse response = getResponse();
    // Set cache for 1 year, give or take.
    response.setHeader("Cache-Control", "max-age=" + 60 * 60 * 24 * 365);
    try
    {
      getresource(out);
    }
    catch (SecurityException e)
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    catch (FileNotFoundException e)
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void res(final OutputStream out) throws Exception
  {
    String pathString = getPathParameters().getStringParameter(MethodParams.PATH, "");
    String resource;
    if (pathString.split("/").length > 2)
    {
      resource = pathString.replaceAll("^/.*?/", "");
    }
    else
    {
      resource = getRequestParameters().getStringParameter(MethodParams.PATH, "");
    }
    resource = resource.startsWith("/") ? resource : "/" + resource;

    String[] path = resource.split("/");
    String[] fileName = path[path.length - 1].split("\\.");


    String mimeType;
    try
    {
      final FileTypes fileType = FileTypes.valueOf(fileName[fileName.length - 1].toUpperCase());
      mimeType = mimeTypes.get(fileType);
    }
    catch (java.lang.IllegalArgumentException ex)
    {
      mimeType = "";
    }
    catch (EnumConstantNotPresentException ex)
    {
      mimeType = "";
    }

    setResponseHeaders(mimeType, RESOURCE_CACHE_DURATION, null);
    final HttpServletResponse response = getResponse();
    // Set cache for 1 year, give or take.
    response.setHeader("Cache-Control", "max-age=" + 60 * 60 * 24 * 365);
    try
    {
      getSolutionResource(out, resource);
    }
    catch (SecurityException e)
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    catch (FileNotFoundException e)
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getresource(final OutputStream out) throws IOException
  {
    String pathString = getPathParameters().getStringParameter(MethodParams.PATH, null);
    String resource;
    if (pathString.split("/").length > 2)
    {
      resource = pathString.replaceAll("^/.*?/", "");
    }
    else
    {
      resource = getRequestParameters().getStringParameter("resource", null);
    }

    if (!Utils.pathStartsWith(resource, SOLUTION_DIR)
            && !Utils.pathStartsWith(resource, PLUGIN_PATH)
            && !Utils.pathStartsWith(resource, "system"))//added for other plugins' components
    {
      resource = Utils.joinPath(PLUGIN_PATH, resource);//default path
    }

    final HttpServletResponse response = getResponse();
    String[] roots = ComponentManager.getInstance().getAllowedLocations(); 
    
    try
    {
      getSolutionResource(out, resource, roots);
    }
    catch (SecurityException e)
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    catch (FileNotFoundException e)
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void edit(final OutputStream out) throws IOException
  {
    // 0 - Check security. Caveat: if no path is supplied, then we're in the new parameter

    final long start = System.currentTimeMillis();        
    UUID uuid = CpfAuditHelper.startAudit(PLUGIN_NAME, "edit", getObjectName(), this.userSession, this, getRequestParameters());       
    IParameterProvider requestParams = getRequestParameters();
    boolean debugMode = requestParams.hasParameter("debug") && requestParams.getParameter("debug").toString().equals("true");
    if (requestParams.hasParameter(MethodParams.PATH) && !RepositoryAccess.getRepository(userSession).hasAccess(getWcdfRelativePath(requestParams), FileAccess.EDIT))
    {
      writeOut(out, "Access Denied");
      return;
    }

    final String header = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDFDD).getDependencies();
    final HashMap<String, String> tokens = new HashMap<String, String>();
    tokens.put(DESIGNER_HEADER_TAG, header);

    // Decide whether we're in debug mode (full-size scripts) or normal mode (minified scripts)
    if (debugMode)
    {
      final String scripts = ResourceManager.getInstance().getResourceAsString(DESIGNER_SCRIPTS_RESOURCE);
      final String styles = ResourceManager.getInstance().getResourceAsString(DESIGNER_STYLES_RESOURCE);
      //DEBUG MODE
      tokens.put(DESIGNER_STYLES_TAG, styles);
      tokens.put(DESIGNER_SCRIPTS_TAG, scripts);
    }
    else
    {
      String stylesHash = packager.minifyPackage("styles");
      String scriptsHash = packager.minifyPackage("scripts");
      // NORMAL MODE
      tokens.put(DESIGNER_STYLES_TAG, "<link href=\"css/styles.css?version=" + stylesHash + "\" rel=\"stylesheet\" type=\"text/css\" />");
      tokens.put(DESIGNER_SCRIPTS_TAG, "<script type=\"text/javascript\" src=\"js/scripts.js?version=" + scriptsHash + "\"></script>");
    }
    IParameterProvider pathParams = getPathParameters();
    tokens.put(DESIGNER_CDF_TAG, DashboardDesignerContentGenerator.getCdfIncludes("empty", "desktop", debugMode, null, DashboardDesignerContentGenerator.getScheme(pathParams)));
    tokens.put(FILE_NAME_TAG, getStructureRelativePath(requestParams));
    tokens.put(SERVER_URL_TAG, SERVER_URL_VALUE);
    tokens.put(DATA_URL_TAG, DATA_URL_VALUE);

    final String resource = ResourceManager.getInstance().getResourceAsString(DESIGNER_RESOURCE, tokens);

    // Cache the output - Disabled for security check reasons
    // setCacheControl();

    writeOut(out, resource);
    CpfAuditHelper.endAudit(PLUGIN_NAME, "edit", getObjectName(), this.userSession, this, start, uuid, System.currentTimeMillis());    
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void synctemplates(final OutputStream out) throws Exception
  {
    final CdfTemplates cdfTemplates = new CdfTemplates(userSession);

    cdfTemplates.syncronize(out, getRequestParameters());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listrenderers(final OutputStream out) throws Exception
  {
    out.write("{\"result\": [\"mobile\",\"blueprint\"]}".getBytes("utf-8"));
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void syncstyles(final OutputStream out) throws Exception
  {
    CdfStyles.getInstance().syncronize(userSession, out, getRequestParameters());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void olaputils(final OutputStream out)
  {

    final OlapUtils olapUtils = new OlapUtils(userSession);

    olapUtils.executeOperation(getRequestParameters());

    try
    {
      final Object result = olapUtils.executeOperation(getRequestParameters());
      JsonUtils.buildJsonResult(out, true, result);
    }
    catch (Exception ex)
    {
      logger.fatal(ex);
      JsonUtils.buildJsonResult(out, false, "Exception found: " + ex.getClass().getName() + " - " + ex.getMessage());
    }

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void explorefolder(final OutputStream out) throws IOException
  {
    IParameterProvider requestParams = getRequestParameters();
    final String folder = requestParams.getStringParameter("dir", null);
    final String fileExtensions = requestParams.getStringParameter("fileExtensions", null);
    final String permission = requestParams.getStringParameter("access", null);
    writeOut(out, FileExplorer.getInstance().getJqueryFileTree(folder, fileExtensions, permission, userSession));
  }
  

  /**
   * List CDA datasources for given dashboard.
   */
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listcdasources(final OutputStream out) throws IOException
  {
    String dashboard = getRequestParameters().getStringParameter("dashboard", null);
    dashboard = StringUtils.replace(dashboard, ".wcdf", ".cdfde");
    List<DataSourceReader.CdaDataSource> dataSourcesList = DataSourceReader.getCdaDataSources(dashboard);
    DataSourceReader.CdaDataSource[] dataSources = dataSourcesList.toArray(new DataSourceReader.CdaDataSource[dataSourcesList.size()]);
    String result = "[" + StringUtils.join(dataSources, ",") + "]";
    writeOut(out, result);
  }

// External Editor v
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getfile(final OutputStream out) throws IOException
  {
    String path = getRequestParameters().getStringParameter(MethodParams.PATH, "");

    String contents = ExternalFileEditorBackend.getFileContents(path, userSession);

    setResponseHeaders("text/plain", NO_CACHE_DURATION, null);
    writeOut(out, contents);
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void createfolder(OutputStream out) throws PentahoAccessControlException, IOException
  {

    String path = getRequestParameters().getStringParameter(MethodParams.PATH, null);

    if (ExternalFileEditorBackend.createFolder(path, userSession))
    {
      IOUtils.write("Path " + path + " created ok", out);
    }
    else
    {
      IOUtils.write("error creating folder " + path, out);
    }

  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void writefile(OutputStream out) throws PentahoAccessControlException, IOException
  {
    IParameterProvider requestParams = getRequestParameters();
    String path = requestParams.getStringParameter(MethodParams.PATH, null);
    String solution = requestParams.getStringParameter(MethodParams.SOLUTION, null);
    String contents = requestParams.getStringParameter(MethodParams.DATA, null);

    if (ExternalFileEditorBackend.writeFile(path, solution, userSession, contents))
    {//saved ok
      IOUtils.write("file '" + path + "' saved ok", out);
    }
    else
    {//error
      IOUtils.write("error saving file " + path, out);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void canedit(OutputStream out) throws IOException
  {
    String path = getRequestParameters().getStringParameter(MethodParams.PATH, null);

    Boolean result = ExternalFileEditorBackend.canEdit(path, userSession);
    writeOut(out, result.toString());
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void exteditor(final OutputStream out) throws IOException
  {
    String editorPath = Utils.joinPath(PLUGIN_PATH, EXTERNAL_EDITOR_PAGE);
    writeOut(out, ExternalFileEditorBackend.getFileContents(editorPath, userSession));
  }

  //External Editor ^ 

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void componenteditor(final OutputStream out) throws IOException
  {
    String editorPath = Utils.joinPath(PLUGIN_PATH, COMPONENT_EDITOR_PAGE);
    writeOut(out,ExternalFileEditorBackend.getFileContents(editorPath, userSession));
  }

  static String getWcdfRelativePath(final IParameterProvider pathParams)
  {
    final String path = "/" + pathParams.getStringParameter(DashboardDesignerContentGenerator.MethodParams.SOLUTION, null)
            + "/" + pathParams.getStringParameter(DashboardDesignerContentGenerator.MethodParams.PATH, null)
            + "/" + pathParams.getStringParameter(DashboardDesignerContentGenerator.MethodParams.FILE, null);

    return path.replaceAll("//+", "/");
  }

  static String getStructureRelativePath(final IParameterProvider pathParams)
  {
    String path = getWcdfRelativePath(pathParams);
    return path.replace(".wcdf", ".cdfde");
  }

  static String getStructureRelativePath(String wcdfPath)
  {
    return wcdfPath.replace(".wcdf", ".cdfde");
  }

  private void getSolutionResource(final OutputStream out, final String resource) throws IOException
  {
    String[] roots = new String[3];
    roots[0] = PentahoSystem.getApplicationContext().getSolutionPath(PLUGIN_PATH);
    roots[1] = PentahoSystem.getApplicationContext().getSolutionPath("");
    roots[2] = PentahoSystem.getApplicationContext().getSolutionPath(MOLAP_PLUGIN_PATH);
    getSolutionResource(out, resource, roots);
  }

  private void getSolutionResource(final OutputStream out, final String resource, final String[] allowedRoots) throws IOException
  {

    setCacheControl();
    final String path = Utils.getSolutionPath(resource); //$NON-NLS-1$ //$NON-NLS-2$

    final IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    String formats = resLoader.getPluginSetting(this.getClass(), "resources/downloadable-formats");

    if (formats == null)
    {
      logger.error("Could not obtain resources/downloadable-formats settings entry, please check plugin.xml and make sure settings are refreshed.");
      formats = ""; //avoid NPE
    }

    List<String> allowedFormats = Arrays.asList(formats.split(","));
    String extension = resource.replaceAll(".*\\.(.*)", "$1");
    if (allowedFormats.indexOf(extension) < 0)
    {
      // We can't provide this type of file
      throw new SecurityException("Not allowed");
    }
    final File file = new File(path);
    final String system = PentahoSystem.getApplicationContext().getSolutionPath(SYSTEM_PATH);
    File rootFile;
    boolean allowed = false;
    for (String root : allowedRoots)
    {
      if (isFileWithinPath(file, root))
      {
        /* If the file's within the specified root, it looks good. But if the
         * file is within /system/, we need to check whether the root specifically
         * allows for files in there as well.
         */
        rootFile = new File(root);
        if (!isFileWithinPath(file, system) || isFileWithinPath(rootFile, system))
        {
          allowed = true;
          break;
        }
      }
    }

    if (!allowed)
    {
      throw new SecurityException("Not allowed");
    }

    InputStream in = null;
    try
    {
      in = new FileInputStream(file);
      IOUtils.copy(in, out);
    }
    catch (FileNotFoundException e)
    {
      logger.warn("Couldn't find file " + file.getCanonicalPath());
      throw e;
    }
    finally
    {
      IOUtils.closeQuietly(in);
    }
  }

  private boolean isFileWithinPath(File file, String absPathBase)
  {
    try
    {
      final String filePath = normalizePathSeparators(file.getCanonicalPath());
      final String basePath = normalizePathSeparators(absPathBase);
      File base = new File(basePath);
      return filePath.startsWith(normalizePathSeparators(base.getCanonicalPath()));
    }
    catch (Exception e)
    {
      return false;
    }
  }

  private String normalizePathSeparators(String path)
  {
    return path.replaceAll("\\\\", "/").replaceAll("/+", "/");
  }

  private void setCacheControl()
  {
    final IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    final String maxAge = resLoader.getPluginSetting(this.getClass(), "max-age");
    final HttpServletResponse response = getResponse();
    if (maxAge != null && response != null)
    {
      response.setHeader("Cache-Control", "max-age=" + maxAge);
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void listdataaccesstypes(final IParameterProvider pathParams, final OutputStream out) throws Exception
  {
    InterPluginCall cdaListDataAccessTypes = getCdaListDataAccessTypesCall(false);
    cdaListDataAccessTypes.setOutputStream(out);
    cdaListDataAccessTypes.run();
  }


  private JSON getCdaDefs(boolean refresh) throws Exception
  {
    InterPluginCall cdaListDataAccessTypes = getCdaListDataAccessTypesCall(refresh);
    return JSONSerializer.toJSON(cdaListDataAccessTypes.call());
  }
  
  private InterPluginCall getCdaListDataAccessTypesCall(boolean refresh){
    InterPluginCall cdaListDataAccessTypes = new InterPluginCall(InterPluginCall.CDA, "listDataAccessTypes");
    cdaListDataAccessTypes.setSession(userSession);
    cdaListDataAccessTypes.putParameter("refreshCache", "" + refresh);
    return cdaListDataAccessTypes;
  }

  private void init() throws IOException
  {
    this.packager = Packager.getInstance();
    Properties props = new Properties();
    String rootdir = PentahoSystem.getApplicationContext().getSolutionPath(PLUGIN_PATH);
    props.load(new FileInputStream(rootdir + "/includes.properties"));

    if (!packager.isPackageRegistered("scripts"))
    {
      String[] files = props.get("scripts").toString().split(",");
      packager.registerPackage("scripts", Packager.Filetype.JS, rootdir, rootdir + "/js/scripts.js", files);
    }
    if (!packager.isPackageRegistered("styles"))
    {
      String[] files = props.get("styles").toString().split(",");
      packager.registerPackage("styles", Packager.Filetype.CSS, rootdir, rootdir + "/css/styles.css", files);
    }
  }

  static String getCdfContext(IParameterProvider requestParameterProvider)
  {
    InterPluginCall cdfContext = new InterPluginCall(InterPluginCall.CDF,"Context");
    cdfContext.setRequestParameters(requestParameterProvider);
    return cdfContext.call();
  }

  static String getCdfIncludes(String dashboard, IParameterProvider pathParams) throws Exception
  {
    return getCdfIncludes(dashboard, null, false, "", getScheme(pathParams));
  }

  static String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws IOException
  {

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dashboardContent", dashboard);
    params.put("debug", debug);
    params.put("scheme", scheme);
    if (type != null)
    {
      params.put("dashboardType", type);
    }
    if (!"".equals(absRoot) && absRoot != null)
    {
      params.put("root", absRoot);
    }
    
    InterPluginCall cdfGetHeaders = new InterPluginCall(InterPluginCall.CDF, "GetHeaders", params); 
    return cdfGetHeaders.call();
  }

  private void setResponseHeaders(final String mimeType, final int cacheDuration, final String attachmentName)
  {
    // Make sure we have the correct mime type
    final HttpServletResponse response = getResponse();

    if (response == null)
    {
      logger.error("Parameter 'httpresponse' not found!");
      return;
    }

    response.setHeader("Content-Type", mimeType);

    if (attachmentName != null)
    {
      response.setHeader("content-disposition", "attachment; filename=" + attachmentName);
    } // Cache?

    if (cacheDuration > 0)
    {
      response.setHeader("Cache-Control", "max-age=" + cacheDuration);
    }
    else
    {
      response.setHeader("Cache-Control", "max-age=0, no-store");
    }
  }

  static public String getScheme(IParameterProvider pathParams)
  {
    try
    {
      ServletRequest req = (ServletRequest) (pathParams.getParameter("httprequest"));
      return req.getScheme();
    }
    catch (Exception e)
    {
      return "http";
    }
  }

  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void checkversion(OutputStream out) throws IOException, JSONException {
    writeOut(out, getVersionChecker().checkVersion());
  }
  
  @Exposed(accessLevel = AccessLevel.PUBLIC)
  public void getversion(OutputStream out) throws IOException, JSONException {
    writeOut(out, getVersionChecker().getVersion());
  }

  public VersionChecker getVersionChecker() {
    return new VersionChecker(CdeSettings.getSettings()){

      @Override
      protected String getVersionCheckUrl(VersionChecker.Branch branch) {
        switch(branch){
          case TRUNK:
            return "http://ci.analytical-labs.com/job/Webdetails-CDE/lastSuccessfulBuild/artifact/dist/marketplace.xml";
          case STABLE:
            return "http://ci.analytical-labs.com/job/Webdetails-CDE-Release/lastSuccessfulBuild/artifact/dist/marketplace.xml";
          default:
            return null;
        }
      }
    };
  }
  
  
}
