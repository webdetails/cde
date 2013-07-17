/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.reader.cdfdejs.CdfdeJsReadContext;
import pt.webdetails.cdf.dd.model.inst.reader.cdfdejs.CdfdeJsThingReaderFactory;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor.DashboardRendererType;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.BaseRepositoryAccess.FileAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.IRepositoryFile;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

/**
 * @author dcleao
 */
public final class DashboardManager 
{
  private static final Log _logger = LogFactory.getLog(DashboardManager.class);
  
  private static final DashboardManager _instance = new DashboardManager();
  
  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  
  private final CacheManager _ehCacheManager;
  private final Cache  _ehCache;
  private final Object _ehCacheLock;
  
  private final Map<String, Dashboard> _dashboardsByCdfdeFilePath;
          
  private DashboardManager()
  {
    // The eh-cache holds
    // CdfRunJsDashboardWriteResult objects indexed by DashboardCacheKey
    // Both these types are serializable.
    // 
    // CdfRunJsDashboardWriteResult objects are 
    // an almost-final render of a given Dashboard and options.
    // 
    // Dashboard objects allow rendering a dashboard 
    // multiple times, with different options.
    // 
    // A Dashboard object is re-built from disk 
    // whenever the corresponding WCDF and/or CDE files have changed.
    
    // INIT EH-CACHE for CdfRunJsDashboardWriteResult objects
    _ehCacheManager = createWriteResultCacheManager();
    
    // Not sure we need to check existence of the cache, 
    // since the cache manager is newly created.
    if(!_ehCacheManager.cacheExists(CACHE_NAME))
    {
      _ehCacheManager.addCache(CACHE_NAME);
    }
    
    _ehCache = _ehCacheManager.getCache(CACHE_NAME);
    _ehCacheLock = new Object();
    
    // In memory Dashboard objects cache
    _dashboardsByCdfdeFilePath = new HashMap<String, Dashboard>();
  }
  
  public static DashboardManager getInstance()
  {
    return _instance;
  }
  
  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs(
          String wcdfFilePath,
          CdfRunJsDashboardWriteOptions options,
          IPentahoSession userSession,
          boolean bypassCacheRead) 
          throws ThingWriteException
  {
    if(wcdfFilePath == null) { throw new IllegalArgumentException("wcdfFilePath"); }
    
    // Figure out what dashboard we should be handling: load its wcdf descriptor.
    WcdfDescriptor wcdf;
    if(!wcdfFilePath.isEmpty() && wcdfFilePath.endsWith(".wcdf"))
    {
      try
      {
        wcdf = WcdfDescriptor.load(wcdfFilePath, userSession);
      }
      catch(IOException ex)
      {
        //TODO: User has no permission to WCDF falls here?
        throw new ThingWriteException("Error while accessing the WCDF file.", ex);
      }
      
      if(wcdf == null) 
      {
        // Doesn't exist
        // TODO: Explain: why create a (totally) empty one?
        wcdf = new WcdfDescriptor();
      }
    }
    else
    {
      // We didn't receive a valid path. We're in preview mode.
      // TODO: Support mobile preview mode (must remove dependency on setStyle())
      wcdf = new WcdfDescriptor();
      if(!wcdfFilePath.isEmpty() && wcdfFilePath.endsWith(".cdfde")) {
        wcdf.setPath(wcdfFilePath);
      }
      wcdf.setStyle(CdfStyles.DEFAULTSTYLE);
      wcdf.setRendererType(DashboardRendererType.BLUEPRINT.toString());
      
      bypassCacheRead = true; // no cache for preview
    }
    
    return this.getDashboardCdfRunJs(wcdf, options, userSession, bypassCacheRead);
  }
  
  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs(
          WcdfDescriptor wcdf,
          CdfRunJsDashboardWriteOptions options,
          IPentahoSession userSession,
          boolean bypassCacheRead)
          throws ThingWriteException 
  {
    // 1. Build the cache key.
    String cdeFilePath = Utils.normalizeSolutionRelativePath(wcdf.getStructurePath());
    
    DashboardCacheKey cacheKey = new DashboardCacheKey(
            cdeFilePath, 
            CdfStyles.getInstance().getResourceLocation(wcdf.getStyle()), 
            options.isDebug(),
            options.isAbsolute(),
            options.getSchemedRoot(), 
            options.getAliasPrefix());
    
    // 2. Check existence and permissions to the original CDFDE file
    // NOTE: the cache is shared by all users.
    // The current user may not have access to a cache item previously
    // created by another user.
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    IRepositoryFile cdeFile = repository.getRepositoryFile(cdeFilePath, FileAccess.READ); // was NO_PERM=0;
    if(cdeFile == null)
    {
      throw new ThingWriteException(new FileNotFoundException(cdeFilePath));
    }
    
    // 3. Reading from the cache?
    CdfRunJsDashboardWriteResult dashWrite;
    if(!bypassCacheRead)
    {
      try
      {
        dashWrite = getDashboardWriteResultFromCache(cacheKey, repository, cdeFile);
      }
      catch(FileNotFoundException ex)
      {
        // Is in cache but:
        // * file doesn't exist (anymore)
        // * user has insufficient permissions to access the cdfde file
        throw new ThingWriteException(ex);
      }
      
      if(dashWrite != null)
      {
        // Return cached write result
        return dashWrite;
      }
      
      // Not in cache or cache item expired/invalidated
    }
    
    // 4. Get the Dashboard object
    Dashboard dash;
    try
    {
      dash = this.getDashboard(wcdf, repository, cdeFile, cdeFilePath, bypassCacheRead);
    }
    catch(ThingReadException ex)
    {
      throw new ThingWriteException(ex);
    }
    
    // 5. Obtain a Writer for the CdfRunJs format
    dashWrite = this.writeDashboardToCdfRunJs(dash, options, userSession , bypassCacheRead);
    
    // 6. Cache the dashboard write
    return this.replaceDashboardWriteResultInCache(cacheKey, dashWrite);
  }
  
  public Dashboard getDashboard(
          String wcdfPath,
          IPentahoSession userSession,
          boolean bypassCacheRead)
          throws ThingReadException
  {
    try
    {
      WcdfDescriptor wcdf = WcdfDescriptor.load(wcdfPath, userSession);
      if(wcdf == null)
      {
        throw new ThingReadException(new FileNotFoundException(wcdfPath));
      }
      
      return this.getDashboard(wcdf, userSession, bypassCacheRead);
    }
    catch (IOException ex)
    {
      throw new ThingReadException("Error reading dashboard.", ex);
    }
  }
  
  public Dashboard getDashboard(
          WcdfDescriptor wcdf,
          IPentahoSession userSession,
          boolean bypassCacheRead)
          throws ThingReadException
  {
    String cdeFilePath = Utils.normalizeSolutionRelativePath(wcdf.getStructurePath());
    
    // 1. Check existence and permissions to the original CDFDE file
    // NOTE: the cache is shared by all users.
    // The current user may not have access to a cache item previously
    // created by another user.
    IRepositoryAccess repository = PentahoRepositoryAccess.getRepository(userSession);
    IRepositoryFile cdeFile = repository.getRepositoryFile(cdeFilePath, FileAccess.READ); // was NO_PERM=0;
    if(cdeFile == null)
    {
      throw new ThingReadException(new FileNotFoundException(cdeFilePath));
    }
    
    // 2. Get the Dashboard object
    return this.getDashboard(wcdf, repository, cdeFile, cdeFilePath, bypassCacheRead);
  }
  
  /**
   * DashboardWriteResult cache already checks the modified dates
   * of the CDFDE and the style-template files, 
   * upon access to the cache.
   * 
   * When a dashboard contains widgets and those widgets' 
   * structure (internal content/CDFDE) is modified,
   * the dashboard write result is no longer valid.
   * 
   * This method is proactively called whenever the CDFDE file of 
   * a dashboard, that is a widget, is saved by the editor.
   * If a widget's file is edited by hand, 
   * there's nothing implemented in access-time that detects
   * that a dashboard's contained widget has changed...
   */
  public void invalidateDashboard(String wcdfPath)
  {
    // Look for cached Dashboard objects that contain the widget.
    
    String cdeFilePath = Utils.normalizeSolutionRelativePath(WcdfDescriptor.toStructurePath(wcdfPath));
    
    Map<String, Dashboard> dashboardsByCdfdeFilePath;
    synchronized(this._dashboardsByCdfdeFilePath)
    {
      dashboardsByCdfdeFilePath = new HashMap<String, Dashboard>(this._dashboardsByCdfdeFilePath);
    }
    
    Set<String> invalidateDashboards = new HashSet<String>();
    invalidateDashboards.add(cdeFilePath);
    
    Dashboard dash = dashboardsByCdfdeFilePath.get(cdeFilePath);
    if(dash != null && dash.getWcdf().isWidget()) 
    {
      collectWidgetsToInvalidate(invalidateDashboards, dashboardsByCdfdeFilePath, cdeFilePath);
    }
    
    for(String invalidCdeFilePath : invalidateDashboards)
    {
      _logger.info("Invalidating cache of '" + invalidCdeFilePath + "'");
    }
    
    synchronized(this._dashboardsByCdfdeFilePath)
    {
      for(String invalidCdeFilePath : invalidateDashboards)
      {
        this._dashboardsByCdfdeFilePath.remove(invalidCdeFilePath);
      }
    }
    
    // Clear the DashboardWriteResult eh-cache
    synchronized(this._ehCacheLock)
    {
      List<DashboardCacheKey> ehKeys = this._ehCache.getKeys();
      for(DashboardCacheKey ehKey : ehKeys)
      {
        if(invalidateDashboards.contains(ehKey.getCdfde()))
        {
          this._ehCache.remove(ehKey);
        }
      }
    }
  }
  
  private void collectWidgetsToInvalidate(
          Set<String> invalidateDashboards,
          Map<String, Dashboard> dashboardsByCdfdeFilePath,
          String cdeWidgetFilePath)
  {
    // Find not-invalidated dashboards containing widget cdeWidgetFilePath
    
    for(Dashboard dash : dashboardsByCdfdeFilePath.values())
    {
      String cdeDashFilePath = dash.getSourcePath();
      if(!invalidateDashboards.contains(cdeDashFilePath))
      {
        Iterable<Component> comps = dash.getRegulars();
        for(Component comp : comps)
        {
          if(comp instanceof WidgetComponent)
          {
            WidgetComponent widgetComp = (WidgetComponent)comp;
            if(WcdfDescriptor.toStructurePath(widgetComp.getWcdfPath()).equals(cdeWidgetFilePath))
            {
              // This dashboard uses this widget
              invalidateDashboards.add(cdeDashFilePath);
              if(dash.getWcdf().isWidget())
              {
                // If the dashboard is also a widget, recurse
                collectWidgetsToInvalidate(
                        invalidateDashboards, 
                        dashboardsByCdfdeFilePath,
                        cdeDashFilePath);
              }
              break;
            }
          }
        }
      }
    }
  }
  
  private Dashboard getDashboard(
          WcdfDescriptor wcdf, 
          IRepositoryAccess repository,
          IRepositoryFile cdeFile,
          String cdeFilePath,
          boolean bypassCacheRead)
          throws ThingReadException
  {
    Dashboard cachedDash = null;
    if(!bypassCacheRead) 
    {
      cachedDash = this.getDashboardFromCache(cdeFilePath);
    }
    
    // Read cache, cache item existed and it is valid?
    if(cachedDash != null &&
       cachedDash.getSourceDate().getTime() >= cdeFile.getLastModified())
    {
      // Check WCDF file date as well
      IRepositoryFile wcdfFile = repository.getRepositoryFile(wcdf.getPath(), FileAccess.READ); // was NO_PERM=0;
      if(wcdfFile == null)
      {
        throw new ThingReadException(new FileNotFoundException(wcdf.getPath()));
      }

      if(cachedDash.getSourceDate().getTime() >= wcdfFile.getLastModified()) 
      {
        _logger.info("Have cached dashboard instance - valid.");

        return cachedDash;
      }
    }
    
    if(cachedDash != null) 
    {
      _logger.info("Have cached dashboard instance - invalid. Reloading.");
    }
    
    Dashboard newDash = this.readDashboardFromCdfdeJs(wcdf, repository);
    
    return this.replaceDashboardInCache(cdeFilePath, newDash, cachedDash);
  }
  
  private Dashboard readDashboardFromCdfdeJs(
          WcdfDescriptor wcdf, 
          IRepositoryAccess repository)
          throws ThingReadException
  {
    // 1. Open the CDFDE file.
    String cdeFilePath = wcdf.getStructurePath();
    JXPathContext cdfdeDoc;
    
    try
    {
      cdfdeDoc = openDashboardAsJXPathContext(repository, wcdf);
    }
    catch(FileNotFoundException ex)
    {
      // File does not exist or
      // User has insufficient permissions
      throw new ThingReadException("The CDFDE dashboard file does not exist.", ex);
    }
    catch(IOException ex)
    {
      throw new ThingReadException("Error while accessing the CDFDE dashboard file.", ex);
    }
    
    // 2. Obtain a reader to read the dashboard file
    MetaModel metaModel = MetaModelManager.getInstance().getModel();
    CdfdeJsThingReaderFactory thingReaderFactory = new CdfdeJsThingReaderFactory(metaModel);
    IThingReader reader;
    try
    {
      reader = thingReaderFactory.getReader(KnownThingKind.Dashboard, null, null);
    }
    catch(UnsupportedThingException ex)
    {
      throw new ThingReadException("Error while obtaining a reader for reading the CDFDE dashboard file.", ex);
    }
    
    // 3. Read it
    IThingReadContext readContext = new CdfdeJsReadContext(thingReaderFactory, wcdf, metaModel);
    Dashboard.Builder dashBuilder = (Dashboard.Builder)reader.read(readContext, cdfdeDoc, cdeFilePath);
    
    // 4. Build it
    try
    {
      return dashBuilder.build(metaModel);
    }
    catch(ValidationException ex)
    {
      throw new ThingReadException("Error while validating the CDFDE dashboard file.", ex);
    }
  }
  
  private CdfRunJsDashboardWriteResult writeDashboardToCdfRunJs(
          Dashboard dash,
          CdfRunJsDashboardWriteOptions options,
          IPentahoSession userSession,
          boolean bypassCacheRead) 
          throws ThingWriteException
  {
    // 1. Obtain a Writer for the CdfRunJs format
    IThingWriterFactory writerFactory = new CdfRunJsThingWriterFactory();
    IThingWriter writer;
    try
    {
      writer = writerFactory.getWriter(dash);
    }
    catch(UnsupportedThingException ex)
    {
      throw new ThingWriteException("Error while obtaining a writer for rendering the dashboard.", ex);
    }
    
    // 2. Write it
    CdfRunJsDashboardWriteContext writeContext = 
      new CdfRunJsDashboardWriteContext(writerFactory, /*indent*/"", bypassCacheRead, dash, userSession, options);
      
    CdfRunJsDashboardWriteResult.Builder dashboardWriteBuilder = 
            new CdfRunJsDashboardWriteResult.Builder();
    
    writer.write(dashboardWriteBuilder, writeContext, dash);
      
    return dashboardWriteBuilder.build();
  }
  
  private CdfRunJsDashboardWriteResult getDashboardWriteResultFromCache(
          DashboardCacheKey cacheKey,
          IRepositoryAccess repository,
          IRepositoryFile cdeFile) 
          throws FileNotFoundException
  {
    // 1. Try to obtain dashboard from cache
    Element cacheElement;
    try
    {
      synchronized(this._ehCacheLock)
      {
        cacheElement = this._ehCache.get(cacheKey);
      }
    }
    catch(CacheException ex)
    {
      _logger.info("Dashboard cache invalidated, re-rendering");
      return null;
    }
    
    // 2. In the cache?
    if(cacheElement == null) { return null; }
    
    CdfRunJsDashboardWriteResult dashWrite = 
            (CdfRunJsDashboardWriteResult)cacheElement.getValue();
      
    _logger.info("Got dashboard from cache");
    
    // 3. Get the template file
    IRepositoryFile templFile = null;
    String templPath = cacheKey.getTemplate();
    if(StringUtils.isNotEmpty(templPath))
    {
      templFile = repository.getRepositoryFile(templPath, FileAccess.READ);
    }
    
    // 4. Check if cache item has expired
    //    Cache is invalidated if the dashboard or template have changed since
    //    the cache was loaded, or at midnight every day, 
    //    because of dynamic generation of date parameters.
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 00);
    cal.set(Calendar.MINUTE, 00);
    cal.set(Calendar.SECOND, 1);
    
    // The date at which the source Dashboard object
    // was loaded from disk, not the date at which the DashResult was written.
    Date dashLoadedDate = dashWrite.getLoadedDate();
    
    boolean cacheExpired = cal.getTime().after(dashLoadedDate);
    if(cacheExpired)
    {
      _logger.info("Dashboard expired, re-rendering");
      return null;
    }

    boolean cacheInvalid = (cdeFile.getLastModified  () > dashLoadedDate.getTime()) ||
                           (templFile != null && 
                            templFile.getLastModified() > dashLoadedDate.getTime());
    if(cacheInvalid)
    {
      _logger.info("Dashboard cache invalidated, re-rendering");
      return null;
    }

    return dashWrite;
  }
  
  private CdfRunJsDashboardWriteResult replaceDashboardWriteResultInCache(
          DashboardCacheKey cacheKey,
          CdfRunJsDashboardWriteResult newDashWrite)
  {
    synchronized(this._ehCacheLock)
    {
      Element cacheElement;
      try
      {
        cacheElement = this._ehCache.get(cacheKey);
      }
      catch(CacheException ex)
      {
        cacheElement = null;
      }
    
      if(cacheElement != null)
      {
        // Keep the one which corresponds to the newest Dashboard object
        // read from disk.
        CdfRunJsDashboardWriteResult currDashWrite = 
                (CdfRunJsDashboardWriteResult)cacheElement.getValue();
        
        if(currDashWrite.getLoadedDate().getTime() > 
           newDashWrite .getLoadedDate().getTime())
        {
          return currDashWrite;
        }
      }
      
      this._ehCache.put(new Element(cacheKey, newDashWrite));
      
      return newDashWrite;
    }
  }
  
  private static CacheManager createWriteResultCacheManager() throws CacheException
  {
    // 'new CacheManager' used instead of 'CacheManager.create' 
    // to avoid overriding default cache
    String cacheConfigFile = CACHE_CFG_FILE;

    String cfgFile = PentahoSystem.getApplicationContext()
          .getSolutionPath(DashboardDesignerContentGenerator.PLUGIN_PATH + cacheConfigFile);
   
    CacheManager cacheMgr = new CacheManager(cfgFile);
    
    // enableCacheProperShutdown
    System.setProperty(CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
    
    return cacheMgr;
  }
  
  private Dashboard getDashboardFromCache(String cdeFullPath)
  {
    synchronized(this._dashboardsByCdfdeFilePath)
    {
      return this._dashboardsByCdfdeFilePath.get(cdeFullPath);
    }
  }
  
  private Dashboard replaceDashboardInCache(
          String cdeFullPath, 
          Dashboard newDash, 
          Dashboard oldDash)
  {
    assert newDash != null;
    
    synchronized(this._dashboardsByCdfdeFilePath)
    {
      if(oldDash != null) // otherwise ignore
      {
        Dashboard currDash = this._dashboardsByCdfdeFilePath.get(cdeFullPath);
        if(currDash != null && currDash != oldDash)
        {
          // Do not set.
          // Assume newer
          return currDash;
        }
      }
      
      this._dashboardsByCdfdeFilePath.put(cdeFullPath, newDash);
      return newDash;
    }
  }
  
  public static JXPathContext openDashboardAsJXPathContext(
          IRepositoryAccess repository, 
          WcdfDescriptor wcdf)
          throws IOException, FileNotFoundException
  {
    return openDashboardAsJXPathContext(repository, wcdf.getStructurePath(), wcdf);
  }
  
  public static JXPathContext openDashboardAsJXPathContext(
          IRepositoryAccess repository, 
          String dashboardLocation, 
          WcdfDescriptor wcdf)
          throws IOException, FileNotFoundException
  {
    final JSONObject json = (JSONObject)JsonUtils.readJsonFromInputStream(
            repository.getResourceInputStream(dashboardLocation));

    if (wcdf != null)
    {
      json.put("settings", wcdf.toJSON());
    }
    
    return JXPathContext.newContext(json);
  }
}
