/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 *
 * @author pdpi
 */
class Dashboard implements Serializable
{
  /* CONSTANTS */

  // Dashboard rendering
  private static final String DASHBOARD_HEADER_TAG = "@HEADER@";
  private static final String DASHBOARD_CONTENT_TAG = "@CONTENT@";
  private static final String DASHBOARD_FOOTER_TAG = "@FOOTER@";
  private static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  private static final String I18N_BOILERPLATE = "resources/i18n-boilerplate.js";
  private static Log logger = LogFactory.getLog(Dashboard.class);
  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  /* FIELDS */
  private String template, header, content, footer;
  private String dashboardLocation;
  private Date loaded;
  private DashboardDesignerContentGenerator generator;
  private static CacheManager cacheManager;

  public Dashboard(IParameterProvider pathParams, DashboardDesignerContentGenerator generator)
  {
    this.generator = generator;
    IPentahoSession userSession = generator.getUserSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);
    this.dashboardLocation = generator.getStructureRelativePath(pathParams);
    XmlStructure structure = new XmlStructure(userSession);
    WcdfDescriptor wcdf;
    try
    {
      this.footer = ResourceManager.getInstance().getResourceAsString(RESOURCE_FOOTER);
      wcdf = structure.loadWcdfDescriptor(generator.getWcdfRelativePath(pathParams));
      this.template = CdfStyles.getInstance().getResourceLocation(wcdf.getStyle());
      Cache cache = getCache();
      final boolean bypassCache = pathParams.hasParameter("bypassCache") && pathParams.getParameter("bypassCache").equals("true");
      final boolean debug = pathParams.hasParameter("debug") && pathParams.getParameter("debug").equals("true");
      final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().equals("") ? "http://" + pathParams.getParameter("root").toString() : "" : "";
      final boolean absolute = (!absRoot.equals("")) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");
      DashboardCacheKey key = new DashboardCacheKey(dashboardLocation, template);
      key.setAbs(absolute);
      key.setDebug(debug);
      try
      {

        Element cacheElement = cache.get(key);
        if (!bypassCache && cacheElement != null)
        {
          Dashboard cached = (Dashboard) cacheElement.getValue();
          logger.info("Got dashboard from cache");
          ISolutionFile dash = solutionRepository.getSolutionFile(dashboardLocation, 0);
          ISolutionFile templ = solutionRepository.getSolutionFile("/system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/" + template, 0);

          if (dash.getLastModified() <= cached.getLoaded().getTime()
                  && dash.getLastModified() <= cached.getLoaded().getTime())
          {
            this.content = cached.content;
            this.header = cached.header;
            this.loaded = cached.loaded;
            return;
          }
          else
          {
            logger.info("Dashboard expired, re-rendering");
          }
        }
      }
      catch (CacheException ce)
      {
        // We couldn't get the cache
      }

      final RenderLayout layoutRenderer = new RenderLayout();
      final RenderComponents componentsRenderer = new RenderComponents();


      final JSONObject json = (JSONObject) JsonUtils.readJsonFromInputStream(solutionRepository.getResourceInputStream(dashboardLocation, true));

      json.put("settings", wcdf.toJSON());
      final JXPathContext doc = JXPathContext.newContext(json);

      final StringBuilder dashboardBody = new StringBuilder();

      dashboardBody.append(layoutRenderer.render(doc));
      dashboardBody.append(componentsRenderer.render(doc));

      // set all dashboard members
      this.content = dashboardBody.toString();
      replaceTokens();
      this.header = renderHeaders(pathParams, this.content.toString());
      this.loaded = new Date();
      cache.put(new Element(key, this));
    }
    catch (Exception e)
    {
      this.template = null;
    }
  }

  public String render() throws Exception
  {

    final HashMap<String, String> tokens = new HashMap<String, String>();
    tokens.put(DASHBOARD_HEADER_TAG, this.header + generator.getCdfContext());
    tokens.put(DASHBOARD_FOOTER_TAG, this.footer);
    tokens.put(DASHBOARD_CONTENT_TAG, this.content);

    return ResourceManager.getInstance().getResourceAsString(this.template, tokens);

  }

  private void replaceTokens()
  {
    final String DASHBOARD_PATH_REGEXP = "\\$\\{dashboardPath\\}",
            IMG_TAG_REGEXP = "\\$\\{img:(.+)\\}";
    String path = dashboardLocation.replaceAll("(.+/).*", "$1");
    this.content = this.content // Start with the same content
            .replaceAll(DASHBOARD_PATH_REGEXP, path.replaceAll("(^/.*/$)","$1")) // replace the dashboard path token
            .replaceAll(IMG_TAG_REGEXP, "getimg/" + path + "$1" + "?v=" + new Date().getTime());// build the image links, with a timestamp for caching purposes

  }

  private String renderHeaders(final IParameterProvider pathParams)
  {
    return renderHeaders(pathParams, "");
  }

  private String renderHeaders(final IParameterProvider pathParams, String contents)
  {
    String dependencies, styles, cdfDependencies;
    final boolean debug = pathParams.hasParameter("debug") && pathParams.getParameter("debug").equals("true");
    final String absRoot = pathParams.hasParameter("root") ? !pathParams.getParameter("root").toString().equals("") ? "http://" + pathParams.getParameter("root").toString() : "" : "";
    final boolean absolute = (!absRoot.equals("")) || pathParams.hasParameter("absolute") && pathParams.getParameter("absolute").equals("true");
    // Acquire CDF headers
    try
    {
      cdfDependencies = generator.getCdfIncludes(contents);
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

  protected static synchronized Cache getCache() throws CacheException
  {
    if (cacheManager == null)
    {// 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
      String cacheConfigFile = CACHE_CFG_FILE;

      String cfgFile = PentahoSystem.getApplicationContext().getSolutionPath(DashboardDesignerContentGenerator.PLUGIN_PATH + cacheConfigFile);
      cacheManager = new CacheManager(cfgFile);//CacheManager.create(cfgFile);
    }

    enableCacheProperShutdown(true);

    if (cacheManager.cacheExists(CACHE_NAME) == false)
    {
      cacheManager.addCache(CACHE_NAME);
    }

    return cacheManager.getCache(CACHE_NAME);
  }

  private static void enableCacheProperShutdown(final boolean force)
  {
    if (!force)
    {
      try
      {
        System.getProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY);
        return;//unless force, ignore if already set
      }
      catch (NullPointerException npe)
      {
      } // key null, continue
      catch (InvalidArgumentException iae)
      {
      }// key not there, continue
      catch (SecurityException se)
      {
        return;//no permissions to set
      }
    }
    System.setProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
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
    return template;
  }
/*
  private String renderI18nLoader()
  {
    try
    {
      Locale locale = LocaleHelper.getLocale();
      logger.debug("Rendering dashboard with locale: " + locale.getLanguage());
      HashMap<String, String> tokens = new HashMap<String, String>();
      tokens.put("@GLOBAL_MESSAGE_SET_NAME@", CdfConstants.BASE_GLOBAL_MESSAGE_SET_FILENAME);
      tokens.put("@GLOBAL_MESSAGE_SET_PATH@", header);
      tokens.put("@GLOBAL_MESSAGE_SET@", header);
      tokens.put("@LANGUAGE_CODE@", locale.getLanguage());
      intro = intro.replaceAll("#\\{GLOBAL_MESSAGE_SET_PATH\\}", messageSetPath);
      intro = intro.replaceAll("#\\{GLOBAL_MESSAGE_SET\\}", buildMessageSetCode(i18nTagsList));
      String boilerplate = ResourceManager.getInstance().getResourceAsString(I18N_BOILERPLATE);
      return boilerplate;
    }
    catch (Exception e)
    {
      return "";
    }
  }

  private String processI18nTags(String content)
  {
    ArrayList<String> tagsList = new ArrayList<String>();
    String tagPattern = "CDF.i18n\\(\"";
    String[] test = content.split(tagPattern);
    if (test.length == 1)http://127.0.0.1:8080/pentaho/content/pentaho-cdf-dd/Render?solution=metrics&path=/sumo/sumo_dashboard&file=sumo_forumOverview.wcdf
    {
      return content;
    }
    StringBuffer resBuffer = new StringBuffer();
    int i;
    String tagValue;
    resBuffer.append(test[0]);
    for (i = 1; i < test.length; i++)
    {

      // First tag is processed differently that other because is the only case where I don't
      // have key in first position
      resBuffer.append("<span id=\"");
      if (i != 0)
      {
        // Right part of the string with the value of the tag herein
        tagValue = test[i].substring(0, test[i].indexOf("\")"));
        tagsList.add(tagValue);
        resBuffer.append(updateSelectorName(tagValue));
        resBuffer.append("\"/>");
        resBuffer.append(test[i].substring(test[i].indexOf("\")") + 2, test[i].length()));
      }
    }
    return resBuffer.toString();
  }*/

  private String updateSelectorName(String name)
  {
    // If we've the character . in the message key substitute it conventionally to _
    // when dynamically generating the selector name. The "." character is not permitted in the
    // selector id name
    return name.replace(".", "_");
  }
}

class DashboardCacheKey
{

  private String cdfde, template;
  private boolean debug, abs;

  public boolean isAbs()
  {
    return abs;
  }

  public void setAbs(boolean abs)
  {
    this.abs = abs;
  }

  public boolean isDebug()
  {
    return debug;
  }

  public void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  public String getCdfde()
  {
    return cdfde;
  }

  public String getTemplate()
  {
    return template;
  }

  public DashboardCacheKey(String cdfde, String template)
  {
    this.cdfde = cdfde;
    this.template = template;
    this.abs = false;
    this.debug = false;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DashboardCacheKey other = (DashboardCacheKey) obj;
    if ((this.cdfde == null) ? (other.cdfde != null) : !this.cdfde.equals(other.cdfde))
    {
      return false;
    }
    if ((this.template == null) ? (other.template != null) : !this.template.equals(other.template))
    {
      return false;
    }
    if (this.debug != other.debug)
    {
      return false;
    }
    if (this.abs != other.abs)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + (this.cdfde != null ? this.cdfde.hashCode() : 0);
    hash = 79 * hash + (this.template != null ? this.template.hashCode() : 0);
    hash = 79 * hash + (this.debug ? 1 : 0);
    hash = 79 * hash + (this.abs ? 1 : 0);
    return hash;
  }
}
