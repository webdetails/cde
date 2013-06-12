/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
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
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator.MethodParams;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cdf.dd.structure.XmlStructure;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;

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

  public Dashboard loadDashboard(Map<String, IParameterProvider> params) throws FileNotFoundException
  {
    IParameterProvider pathParams = params.get("path"),
            requestParams = params.get("request");
    String scheme = requestParams.hasParameter("inferScheme") && 
                    requestParams.getParameter("inferScheme").equals("false") ? 
            "" : 
            DashboardDesignerContentGenerator.getScheme(pathParams);
    String root = requestParams.getStringParameter("root", "");
    boolean absolute = (!root.equals("")) || requestParams.hasParameter("absolute") && requestParams.getParameter("absolute").equals("true"),
            bypassCache = requestParams.hasParameter("bypassCache") && requestParams.getParameter("bypassCache").equals("true");
    final boolean debug = requestParams.hasParameter("debug") && requestParams.getParameter("debug").equals("true");

    String wcdfPath = getWcdfPath(requestParams);
    return loadDashboard(wcdfPath, debug, absolute, scheme, root, bypassCache, "");
  }

  public Widget loadWidget(String wcdfPath, String alias) throws FileNotFoundException
  {
    Dashboard d = loadDashboard(wcdfPath, false, false, "", "", true, alias);
    if (d instanceof Widget) {
    return (Widget) d;
    } else {
      throw new ClassCastException("Dashboard isn't a valid Widget");
    }
    
    
  }

  public Dashboard loadDashboard(String wcdfPath, boolean debug, boolean absolute, String scheme, String absRoot, boolean bypassCache, String alias) throws FileNotFoundException
  {

    String dashboardPath = getDashboardPath(wcdfPath);
    IPentahoSession userSession = PentahoSessionHolder.getSession();
    XmlStructure structure = new XmlStructure(userSession);
    Dashboard dashboard = null;
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
        if(wcdfPath != null && wcdfPath.endsWith(".cdfde")) {
          wcdf.setWcdfPath(wcdfPath);
        }
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
    key.setRoot(scheme, absRoot);
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
            dashboard = new MobileDashboard(wcdf, absolute, absRoot, debug, scheme);
            break;

          /* Until we consider it safe to assume that all dashboards have
           * their renderer type correctly identified, we'll have to default
           * to assuming they're blueprint-style dashboards.
           */
          case BLUEPRINT:
          default:
            if (wcdf.isWidget())
            {
              dashboard = new BlueprintWidget(wcdf, absolute, absRoot, debug, scheme, alias);
            }
            else
            {
              dashboard = new BlueprintDashboard(wcdf, absolute, absRoot, debug, scheme);
            }
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

    PentahoRepositoryAccess repository = (PentahoRepositoryAccess) PentahoRepositoryAccess.getRepository();

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
      ISolutionFile dash = repository.getSolutionFile(key.getCdfde(), FileAccess.READ);// was NO_PERM=0;
      if (dash == null)
      {
        logger.error(key.getCdfde() + " not found.");
        return null;
      }
      
      ISolutionFile templ;
      if(key.getTemplate() == null){
        templ = null;
      }else{
        templ = repository.getSolutionFile(key.getTemplate(), FileAccess.READ);
      }

      /* Cache is invalidated if the dashboard or template have changed since
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

  public static synchronized Cache getCache() throws CacheException
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
    return getDashboardPath(path);
  }

  private String getDashboardPath(String wcdfPath)
  {
    return wcdfPath.replace(".wcdf", ".cdfde");
  }

  private String getWcdfPath(final IParameterProvider pathParams)
  {
    final String path = "/" + pathParams.getStringParameter(MethodParams.SOLUTION, null)
            + "/" + pathParams.getStringParameter(MethodParams.PATH, null)
            + "/" + pathParams.getStringParameter(MethodParams.FILE, null);

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

  public void setRoot(String scheme, String root)
  {
    this.root = root.length() == 0 ? "" : scheme + "://" + root;
  }
}
