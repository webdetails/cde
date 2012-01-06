package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import mondrian.olap.InvalidArgumentException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator.PathParams;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cdf.dd.structure.XmlStructure;

/**
 *
 * @author pdpi
 */
public class DashboardFactory
{
  // Cache

  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  private static CacheManager cacheManager;
  private static DashboardFactory instance;
  private static Log logger = LogFactory.getLog(DashboardFactory.class);

  private enum Renderers
  {

    MOBILE, BLUEPRINT
  }

  public static synchronized DashboardFactory getInstance()
  {
    if (instance == null)
    {
      instance = new DashboardFactory();
    }
    return instance;
  }

  private DashboardFactory()
  {
  }

  public Dashboard newDashboard()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Dashboard loadDashboard(Map<String, IParameterProvider> params, DashboardDesignerContentGenerator gen)
  {
    IParameterProvider pathParams = params.get("path"),
            requestParams = params.get("request");
    Dashboard dashboard = null;
    String root = requestParams.hasParameter("root")
            ? !requestParams.getParameter("root").toString().equals("")
            ? DashboardDesignerContentGenerator.getScheme(pathParams) + "://" + requestParams.getParameter("root").toString()
            : ""
            : "";
    boolean absolute = (!root.equals("")) || requestParams.hasParameter("absolute") && requestParams.getParameter("absolute").equals("true"),
            bypassCache = requestParams.hasParameter("bypassCache") && requestParams.getParameter("bypassCache").equals("true");
    final boolean debug = requestParams.hasParameter("debug") && requestParams.getParameter("debug").equals("true");
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    XmlStructure structure = new XmlStructure(userSession);
    String wcdfPath = getWcdfPath(requestParams);
    String dashboardPath = getDashboardPath(requestParams);
    WcdfDescriptor wcdf = null;
    DashboardCacheKey key;
    /*
     * First we must figure out what dashboard we should
     * be handling. We do this by loading up its wcdf
     * descriptor.
     */
    try
    {
      if (!wcdfPath.isEmpty() && wcdfPath.endsWith(".wcdf"))
      {
        wcdf = structure.loadWcdfDescriptor(wcdfPath);
      }
      else
      {
        /* We didn't receive a valid path. We're in preview mode.
         * TODO: Support mobile preview mode (must remove dependency on setStyle())
         */
        wcdf = new WcdfDescriptor();
        wcdf.setStyle(CdfStyles.DEFAULTSTYLE);
        wcdf.setRendererType(Renderers.BLUEPRINT.toString());
        bypassCache = true;//no cache for preview
      }
    }
    catch (IOException ioe)
    {
      logger.error(ioe);
      return null;
    }

    /* Next, if we're using the cache, we try to find
     * the dashboard in there.
     */
    key = new DashboardCacheKey(dashboardPath, CdfStyles.getInstance().getResourceLocation(wcdf.getStyle()), debug);
    key.setAbs(absolute);
    key.setRoot(root);
    if (!bypassCache)
    {
      dashboard = getDashboardFromCache(key);
    }

    /* If it's not there, or we're bypassing the cache,
     * we load it up, and cache the newly loaded dashboard.
     */
    if (dashboard == null)
    {
      Cache cache = getCache();
      try
      {
        switch (Renderers.valueOf(wcdf.getRendererType().toUpperCase()))
        {
          case MOBILE:
            dashboard = new MobileDashboard(pathParams, requestParams);
            break;

          /* Until we consider it safe to assume that all dashboards have
           * their renderer type correctly identified, we'll have to default
           * to assuming they're blueprint-style dashboards.
           */
          case BLUEPRINT:
          default:
            dashboard = new BlueprintDashboard(pathParams, requestParams);
            break;
        }
      }
      catch (IllegalArgumentException e)
      {
        logger.error("Bad renderer type: " + wcdf.getRendererType());
        return null;
      }
      cache.put(new Element(key, dashboard));
    }
    return dashboard;
  }

  protected Dashboard getDashboardFromCache(DashboardCacheKey key)
  {
    Dashboard dashboard;
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    final ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, userSession);

    try
    {
      Cache cache = getCache();
      Element cacheElement = cache.get(key);
      if (cacheElement == null)
      {
        return null;
      }
      else
      {
        dashboard = (Dashboard) cacheElement.getValue();
      }
      logger.info("Got dashboard from cache");
      ISolutionFile dash = solutionRepository.getSolutionFile(key.getCdfde(), 0);
      ISolutionFile templ = key.getTemplate() == null ? null
              : solutionRepository.getSolutionFile("/system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/" + key.getTemplate(), 0);

      /* Cache is invalidated if dashboard or template have changed since the
       * the cache was loaded, or at midnight every day, because of dynamic
       * generation of date parameters.
       */
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, 00);
      cal.set(Calendar.MINUTE, 00);
      cal.set(Calendar.SECOND, 1);
      boolean cacheInvalid = dash.getLastModified() > dashboard.getLoaded().getTime()
              || (templ != null && templ.getLastModified() > dashboard.getLoaded().getTime()),
              cacheExpired = cal.getTime().after(dashboard.getLoaded());


      if (cacheExpired)
      {
        logger.info("Dashboard expired, re-rendering");
        return null;
      }
      else if (cacheInvalid)
      {
        logger.info("Dashboard cache invalidated, re-rendering");
        return null;
      }
      else
      {
        return dashboard;
      }
    }
    catch (CacheException ce)
    {
      logger.info("Dashboard cache invalidated, re-rendering");
      return null;
    }
  }

  private static synchronized Cache getCache() throws CacheException
  {
    if (cacheManager == null)
    {// 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
      String cacheConfigFile = CACHE_CFG_FILE;

      String cfgFile = PentahoSystem.getApplicationContext().getSolutionPath(DashboardDesignerContentGenerator.PLUGIN_PATH + cacheConfigFile);
      cacheManager = new CacheManager(cfgFile);//CacheManager.create(cfgFile);
    }

    enableCacheProperShutdown(true);

    if (!cacheManager.cacheExists(CACHE_NAME))
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

  private String getDashboardPath(final IParameterProvider pathParams)
  {
    String path = getWcdfPath(pathParams);
    return path.replace(".wcdf", ".cdfde");
  }

  private String getWcdfPath(final IParameterProvider pathParams)
  {
    final String path = "/" + pathParams.getStringParameter(PathParams.SOLUTION, null)
            + "/" + pathParams.getStringParameter(PathParams.PATH, null)
            + "/" + pathParams.getStringParameter(PathParams.FILE, null);

    return path.replaceAll("//+", "/");
  }
}

class DashboardCacheKey
{

  private String cdfde, template, root;
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
    this.root = "";
    this.abs = false;
    this.debug = false;
  }

  public DashboardCacheKey(String cdfde, String template, boolean debug)
  {
    this.cdfde = cdfde;
    this.template = template;
    this.root = "";
    this.abs = false;
    this.debug = debug;
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
    if ((this.root == null) ? (other.root != null) : !this.root.equals(other.root))
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

  /**
   * @return the root
   */
  public String getRoot()
  {
    return root;
  }

  /**
   * @param root the root to set
   */
  public void setRoot(String root)
  {
    this.root = root;
  }
}
