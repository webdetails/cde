/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.io.Serializable;
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
import net.sf.ehcache.CacheManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

/**
 *
 * @author pdpi
 */
abstract class AbstractDashboard implements Serializable, Dashboard
{
  /* CONSTANTS */

  // Dashboard rendering
  private static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
  private static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  private static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";
  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  private static Log logger = LogFactory.getLog(Dashboard.class);
  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  /* FIELDS */
  protected String template, header, content, footer;
  protected String templateFile, dashboardLocation;
  protected Date loaded;
  private WcdfDescriptor wcdf;
  protected DashboardDesignerContentGenerator generator;
  private static CacheManager cacheManager;

  public AbstractDashboard(IParameterProvider pathParams, DashboardDesignerContentGenerator generator)
  {
    this.generator = generator;
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    this.dashboardLocation = generator.getStructureRelativePath(pathParams);
    XmlStructure structure = new XmlStructure(userSession);

    wcdf = null;
    try
    {

      String fileName = pathParams.getStringParameter("file", "");
      if (fileName != null && fileName.endsWith(".wcdf"))
      {
        wcdf = structure.loadWcdfDescriptor(generator.getWcdfRelativePath(pathParams));
      }
      else
      {//we may just be receiving a .cde file (preview)
        wcdf = new WcdfDescriptor();
        wcdf.setStyle(CdfStyles.DEFAULTSTYLE);
      }

      this.footer = ResourceManager.getInstance().getResourceAsString(RESOURCE_FOOTER);
      this.templateFile = CdfStyles.getInstance().getResourceLocation(wcdf.getStyle());
      final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().equals("") ? "http://" + pathParams.getParameter("root").toString() : "" : "";
      final boolean absolute = (!absRoot.equals("")) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");

      final RenderLayout layoutRenderer = new RenderLayout();
      final RenderComponents componentsRenderer = new RenderComponents();


      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true));

      json.put("settings", wcdf.toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = replaceTokens(dashboardBody.toString(), absolute, absRoot);

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
      this.header = renderHeaders(pathParams, this.content.toString());
      this.loaded = new Date();
    }
    catch (Exception e)
    {
      this.templateFile = null;
    }
  }

  public String render()
  {
    return this.template.replaceAll(DASHBOARD_HEADER_TAG, Matcher.quoteReplacement(this.header + generator.getCdfContext())) // Replace the Header
            .replaceAll(DASHBOARD_FOOTER_TAG, Matcher.quoteReplacement(this.footer)) // And the Footer
            .replaceAll(DASHBOARD_CONTENT_TAG, Matcher.quoteReplacement(this.content)); // And even the content!
  }

  protected String replaceTokens(String content, boolean absolute, String absRoot)
  {
    final String DASHBOARD_PATH_REGEXP = "\\$\\{dashboardPath\\}",
            ABS_IMG_TAG_REGEXP = "\\$\\{img:(/.+)\\}",
            ABS_RES_TAG_REGEXP = "\\$\\{res:(/.+)\\}",
            REL_IMG_TAG_REGEXP = "\\$\\{img:(.+)\\}",
            REL_RES_TAG_REGEXP = "\\$\\{res:(.+)\\}";

    final long timestamp = new Date().getTime();
    String root = absolute ? absRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE : "";
    String path = dashboardLocation.replaceAll("(.+/).*", "$1");
    String fixedContent = content // Start with the same content
            .replaceAll(DASHBOARD_PATH_REGEXP, path.replaceAll("(^/.*/$)", "$1")) // replace the dashboard path token
            .replaceAll(ABS_IMG_TAG_REGEXP, root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(REL_IMG_TAG_REGEXP, root + "res" + path + "$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(ABS_RES_TAG_REGEXP, root + "res$1" + "?v=" + timestamp)// build the image links, with a timestamp for caching purposes
            .replaceAll(REL_RES_TAG_REGEXP, root + "res" + path + "$1" + "?v=" + timestamp);// build the image links, with a timestamp for caching purposes

    return fixedContent;
  }

  protected String renderHeaders(final IParameterProvider pathParams)
  {
    return renderHeaders(pathParams, "");
  }

  protected String renderHeaders(final IParameterProvider pathParams, String contents)
  {
    String dependencies, styles, cdfDependencies;
    final boolean debug = pathParams.hasParameter("debug") && pathParams.getParameter("debug").equals("true");
    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().equals("") ? "http://" + pathParams.getParameter("root").toString() : "" : "";
    final boolean absolute = (!absRoot.equals("")) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");
    // Acquire CDF headers
    try
    {
      cdfDependencies = generator.getCdfIncludes(contents, getType(), debug, absRoot);
    }
    catch (Exception e)
    {
      logger.error("Failed to get cdf includes");
      cdfDependencies = "";
    }
    // Acquire CDE-specific headers
    if (absolute)
    {
      StringFilter css = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<link href='" + absRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getCssResource/" + input + "' rel='stylesheet' type='text/css' />\n";
        }
      };
      StringFilter js = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"" + absRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getJsResource/" + input + "\"></script>\n";
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
    return raw + cdfDependencies + dependencies + styles;
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
