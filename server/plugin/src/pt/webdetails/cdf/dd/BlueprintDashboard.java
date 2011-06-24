/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Calendar;
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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import mondrian.olap.InvalidArgumentException;
import net.sf.ehcache.Element;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

/**
 *
 * @author pdpi
 */
public class BlueprintDashboard extends AbstractDashboard
{
  /* CONSTANTS */

  // Dashboard rendering
  private static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
  private static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  private static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";
  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  private static final String I18N_BOILERPLATE = "resources/i18n-boilerplate.js";
  private static Log logger = LogFactory.getLog(Dashboard.class);
  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  /* FIELDS */
  protected final static String TYPE = "blueprint";

  public BlueprintDashboard(IParameterProvider pathParams, DashboardDesignerContentGenerator generator)
  {
    super(pathParams, generator);
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);



    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().isEmpty() ? "http://" + pathParams.getParameter("root").toString() : "" : "";
    final boolean absolute = (!absRoot.isEmpty()) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");

    final RenderLayout layoutRenderer = new RenderLayout();
    final RenderComponents componentsRenderer = new RenderComponents();

    try
    {
      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true));

      json.put("settings", getWcdf().toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = replaceTokens(dashboardBody.toString(), absolute, absRoot);


      this.header = renderHeaders(pathParams, this.content.toString());
      this.loaded = new Date();
    }
    catch (Exception e)
    {
      logger.error(e);
    }
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

  public String getType()
  {
    return TYPE;
  }
}
