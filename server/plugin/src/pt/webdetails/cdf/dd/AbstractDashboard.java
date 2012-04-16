/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Matcher;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.RenderComponents;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.render.StringFilter;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cdf.dd.structure.XmlStructure;
import pt.webdetails.cdf.dd.util.JsonUtils;

// Imports for the cache
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

/**
 *
 * @author pdpi
 */
public abstract class AbstractDashboard implements Serializable, Dashboard
{
  /* CONSTANTS */

  private static final long serialVersionUID = 1L;
  // Dashboard rendering
  private static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
  private static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  private static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";
  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  private static Log logger = LogFactory.getLog(Dashboard.class);
  /* FIELDS */
  protected boolean absolute, debug;
  protected String template, header, content, footer, absRoot, scheme;
  protected String templateFile, dashboardLocation;
  protected Date loaded;
  private WcdfDescriptor wcdf;

  public AbstractDashboard(IParameterProvider pathParams, IParameterProvider requestParams)
  {
    absRoot = requestParams.hasParameter("root") ? requestParams.getParameter("root").toString() : "";
    absolute = (!absRoot.equals("")) || requestParams.hasParameter("absolute") && requestParams.getParameter("absolute").equals("true");
    debug = requestParams.hasParameter("debug") && requestParams.getParameter("debug").equals("true");
    scheme = DashboardDesignerContentGenerator.getScheme(pathParams);
    construct(DashboardDesignerContentGenerator.getWcdfRelativePath(requestParams));
  }

  private void construct(String wcdfPath)
  {
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    this.dashboardLocation = DashboardDesignerContentGenerator.getStructureRelativePath(wcdfPath);
    XmlStructure structure = new XmlStructure(userSession);

    wcdf = null;
    try
    {

      if (wcdfPath != null && wcdfPath.endsWith(".wcdf"))
      {
        wcdf = structure.loadWcdfDescriptor(wcdfPath);
      }
      else
      {//we may just be receiving a .cde file (preview)
        wcdf = new WcdfDescriptor();
        wcdf.setStyle(CdfStyles.DEFAULTSTYLE);
      }

      this.footer = ResourceManager.getInstance().getResourceAsString(RESOURCE_FOOTER);
      this.templateFile = CdfStyles.getInstance().getResourceLocation(wcdf.getStyle());

      final RenderLayout layoutRenderer = new RenderLayout();
      final RenderComponents componentsRenderer = new RenderComponents();


      final JXPathContext doc = openDashboardAsJXPathContext(solutionRepository, dashboardLocation, wcdf);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = replaceTokens(dashboardBody.toString(), absolute, absRoot);
      this.header = replaceTokens(renderHeaders(this.content.toString()), absolute, absRoot);

      try
      {//attempt to read template file
        this.template = replaceTokens(ResourceManager.getInstance().getResourceAsString(this.templateFile), absolute, absRoot);
      }
      catch (IOException e)
      {
        //couldn't open template file, attempt to use default
        logger.error(MessageFormat.format("Couldn''t open template file {0}.", this.templateFile), e);
        String templateFile = CdfStyles.getInstance().getResourceLocation(CdfStyles.DEFAULTSTYLE);
        this.template = replaceTokens(ResourceManager.getInstance().getResourceAsString(templateFile), absolute, absRoot);
      }
      this.loaded = new Date();
    }
    catch (Exception e)
    {
      this.templateFile = null;
    }
  }

//  private JXPathContext openDashboardAsJXPathContext(final ISolutionRepository solutionRepository) throws IOException, FileNotFoundException {
//    final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true, ISolutionRepository.ACTION_EXECUTE));
//
//    json.put("settings", wcdf.toJSON());
//    final JXPathContext doc = JXPathContext.newContext(json);
//    return doc;
//  }
  public static JXPathContext openDashboardAsJXPathContext(ISolutionRepository solutionRepository, String dashboardLocation, WcdfDescriptor wcdf)
          throws IOException, FileNotFoundException
  {
    final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true, ISolutionRepository.ACTION_EXECUTE));

    if (wcdf != null)
    {
      json.put("settings", wcdf.toJSON());
    }
    final JXPathContext doc = JXPathContext.newContext(json);
    return doc;
  }

  public String render()
  {
    return render(null);
  }

  public String render(IParameterProvider params)
  {
    String context = DashboardDesignerContentGenerator.getCdfContext(params);
    logger.info("[Timing] Starting render proper: " + (new SimpleDateFormat("H:m:s.S")).format(new Date()));
    String quotedFooter = Matcher.quoteReplacement(this.footer),
            quotedHeader = Matcher.quoteReplacement(this.header + context),
            quotedContent = Matcher.quoteReplacement(this.content);
    logger.info("[Timing] Replacing tokens: " + (new SimpleDateFormat("H:m:s.S")).format(new Date()));

    String result = this.template.replaceAll(DASHBOARD_HEADER_TAG, quotedHeader) // Replace the Header
            .replaceAll(DASHBOARD_FOOTER_TAG, quotedFooter) // And the Footer
            .replaceAll(DASHBOARD_CONTENT_TAG, quotedContent); // And even the content!
    logger.info("[Timing] Finished render proper: " + (new SimpleDateFormat("H:m:s.S")).format(new Date()));
    return result;
  }

  protected String replaceTokens(String content, boolean absolute, String absRoot)
  {
    final String DASHBOARD_PATH_REGEXP = "\\$\\{dashboardPath\\}",
            ABS_DIR_RES_TAG_REGEXP = "\\$\\{res:(/.+/)\\}",
            ABS_IMG_TAG_REGEXP = "\\$\\{img:(/.+)\\}",
            ABS_RES_TAG_REGEXP = "\\$\\{res:(/.+)\\}",
            REL_DIR_RES_TAG_REGEXP = "\\$\\{res:(.+/)\\}",
            REL_IMG_TAG_REGEXP = "\\$\\{img:(.+)\\}",
            REL_RES_TAG_REGEXP = "\\$\\{res:(.+)\\}";

    final long timestamp = new Date().getTime();
    String root = absolute ? scheme + "://" + absRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE : "";
    String path = dashboardLocation.replaceAll("(.+/).*", "$1");
    String fixedContent = content // Start with the same content
            .replaceAll(DASHBOARD_PATH_REGEXP, path.replaceAll("(^/.*/$)", "$1")) // replace the dashboard path token
            .replaceAll(ABS_IMG_TAG_REGEXP, root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(REL_IMG_TAG_REGEXP, root + "res" + path + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(ABS_DIR_RES_TAG_REGEXP, root + "res$1")// Directories don't need the caching timestamp
            .replaceAll(REL_DIR_RES_TAG_REGEXP, root + "res" + path + "$1")// Directories don't need the caching timestamp
            .replaceAll(ABS_RES_TAG_REGEXP, root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(REL_RES_TAG_REGEXP, root + "res" + path + "$1" + "?v=" + timestamp);// build the image links, with a timestamp for caching purposes

    return fixedContent;
  }

  protected String renderHeaders()
  {
    return renderHeaders("");
  }

  protected String renderHeaders(String contents)
  {
    String dependencies, styles, cdfDependencies;
    final String title = "<title>" + getWcdf().getTitle() + "</title>";
    // Acquire CDF headers
    try
    {
      cdfDependencies = DashboardDesignerContentGenerator.getCdfIncludes(contents, getType(), debug, absRoot, scheme);
    }
    catch (Exception e)
    {
      logger.error("Failed to get cdf includes");
      cdfDependencies = "";
    }
    // Acquire CDE-specific headers
    if (absolute)
    {
      final String adornedRoot = "http://" + absRoot;
      StringFilter css = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<link href='" + adornedRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getCssResource/" + input + "' rel='stylesheet' type='text/css' />\n";
        }
      };
      StringFilter js = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"" + adornedRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getJsResource/" + input + "\"></script>\n";
        }
      };
      if (debug)
      {
        dependencies = DependenciesManager.getInstance().getEngine("CDF").getDependencies(js);
        styles = DependenciesManager.getInstance().getEngine("CDF-CSS").getDependencies(css);
      }
      else
      {
        dependencies = DependenciesManager.getInstance().getEngine("CDF").getPackagedDependencies(js);
        styles = DependenciesManager.getInstance().getEngine("CDF-CSS").getPackagedDependencies(css);
      }
    }
    else
    {
      if (debug)
      {
        dependencies = DependenciesManager.getInstance().getEngine("CDF").getDependencies();
        styles = DependenciesManager.getInstance().getEngine("CDF-CSS").getDependencies();
      }
      else
      {
        dependencies = DependenciesManager.getInstance().getEngine("CDF").getPackagedDependencies();
        styles = DependenciesManager.getInstance().getEngine("CDF-CSS").getPackagedDependencies();
      }
    }

    String raw = DependenciesManager.getInstance().getEngine("CDF-RAW").getDependencies();
    return title + cdfDependencies + raw + dependencies + styles;
  }

  public String getContent()
  {
    return content;
  }

  public String getFooter()
  {
    return footer;
  }

  public String getHeader()
  {
    return header;
  }

  public Date getLoaded()
  {
    return loaded;
  }

  public String getTemplate()
  {
    return templateFile;
  }

  /**
   * @return the wcdf
   */
  protected WcdfDescriptor getWcdf()
  {
    return wcdf;
  }

  /**
   * @param wcdf the wcdf to set
   */
  protected void setWcdf(WcdfDescriptor wcdf)
  {
    this.wcdf = wcdf;
  }
}
