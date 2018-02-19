/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
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
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.reader.cdfdejs.CdfdeJsReadContext;
import pt.webdetails.cdf.dd.model.inst.reader.cdfdejs.CdfdeJsThingReaderFactory;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.legacy.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor.DashboardRendererType;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class DashboardManager {
  private static final Log _logger = LogFactory.getLog( DashboardManager.class );

  private static DashboardManager _instance;

  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  private static final String[] MAP_PARAMETERS = { "Parameter", "JavascriptParameter", "DateParameter" };

  private CacheManager _ehCacheManager;
  private Cache _ehCache;
  private Object _ehCacheLock;

  private Map<String, Dashboard> _dashboardsByCdfdeFilePath;

  private ICdeEnvironment environment;

  protected DashboardManager() { }

  public static DashboardManager getInstance() {
    if ( _instance == null ) {
      synchronized ( DashboardManager.class ) {
        if ( _instance == null ) {
          _instance = new DashboardManager();
        }
      }
    }
    return _instance;
  }

  public ICdeEnvironment getEnvironment() {
    return this.environment;
  }

  public void setEnvironment( ICdeEnvironment environment ) {
    this.environment = environment;
  }

  public void init() {
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

    // TODO: Not sure we need to check existence of the cache,
    // since the cache manager is newly created.
    if ( !_ehCacheManager.cacheExists( CACHE_NAME ) ) {
      _ehCacheManager.addCache( CACHE_NAME );
    }

    _ehCache = _ehCacheManager.getCache( CACHE_NAME );
    _ehCacheLock = new Object();

    // In memory Dashboard objects cache
    _dashboardsByCdfdeFilePath = new HashMap<String, Dashboard>();
  }

  public static JXPathContext openDashboardAsJXPathContext( DashboardWcdfDescriptor wcdf ) throws IOException,
    JSONException {
    return openDashboardAsJXPathContext( wcdf.getStructurePath(), wcdf );
  }

  public static JXPathContext openDashboardAsJXPathContext( String dashboardLocation, DashboardWcdfDescriptor wcdf )
    throws IOException, JSONException {
    InputStream input = null;
    try {
      input = Utils.getSystemOrUserReadAccess( dashboardLocation ).getFileInputStream( dashboardLocation );
      final JSONObject json = JsonUtils.readJsonFromInputStream( input );

      if ( wcdf != null ) {
        try {
          json.put( "settings", wcdf.toJSON() );
        } catch ( JSONException e ) {
          _logger.error( "Error writing settings to json", e );
        }
      }
      return JsonUtils.toJXPathContext( json );
    } finally {
      IOUtils.closeQuietly( input );
    }
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs( String wcdfFilePath, CdfRunJsDashboardWriteOptions options,
                                                            boolean bypassCacheRead ) throws ThingWriteException {
    return getDashboardCdfRunJs( wcdfFilePath, options, bypassCacheRead, "" );
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs( String wcdfFilePath, CdfRunJsDashboardWriteOptions options,
                                                            boolean bypassCacheRead, String style )
    throws ThingWriteException {
    if ( wcdfFilePath == null ) {
      throw new IllegalArgumentException( "wcdfFilePath" );
    }

    // Figure out what dashboard we should be handling: load its wcdf descriptor.
    DashboardWcdfDescriptor wcdf;
    if ( !wcdfFilePath.isEmpty() && wcdfFilePath.endsWith( ".wcdf" ) ) {
      try {
        wcdf = DashboardWcdfDescriptor.load( wcdfFilePath );
      } catch ( IOException ex ) {
        // TODO: User has no permission to WCDF falls here?
        throw new ThingWriteException( "While accessing the WCDF file.", ex );
      }

      if ( wcdf == null ) {
        // Doesn't exist
        // TODO: Explain or fix, why create a (totally) empty one?
        wcdf = new DashboardWcdfDescriptor();
      }
    } else {
      // We didn't receive a valid path. We're in preview mode.
      // TODO: Support mobile preview mode (must remove dependency on setStyle())
      wcdf = getPreviewWcdf( wcdfFilePath );
      bypassCacheRead = true; // no cache for preview
    }

    if ( StringUtils.isNotEmpty( style ) ) {
      wcdf.setStyle( style );
    }

    return this.getDashboardCdfRunJs( wcdf, options, bypassCacheRead );
  }

  //TODO: is wcdfPath needed?
  public DashboardWcdfDescriptor getPreviewWcdf( String cdfdePath )
    throws ThingWriteException {
    DashboardWcdfDescriptor wcdf = new DashboardWcdfDescriptor();
    //TODO is this needed?
    if ( !cdfdePath.isEmpty() && cdfdePath.endsWith( ".cdfde" ) ) {
      wcdf.setPath( cdfdePath );
    }
    wcdf.setStyle( CdeConstants.DEFAULT_STYLE );
    wcdf.setRendererType( DashboardRendererType.BLUEPRINT.getType() );
    return wcdf;
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs( DashboardWcdfDescriptor wcdf,
                                                            CdfRunJsDashboardWriteOptions options,
                                                            boolean bypassCacheRead ) throws ThingWriteException {
    // 1. Build the cache key.
    String cdeFilePath = Utils.sanitizeSlashesInPath( wcdf.getStructurePath() );

    DashboardCacheKey cacheKey = new DashboardCacheKey(
        cdeFilePath,
        getPluginResourceLocationManager().getStyleResourceLocation( wcdf.getStyle() ),
        options.isDebug(),
        options.isAbsolute(),
        options.getSchemedRoot(),
        options.getAliasPrefix() );

    // 2. Check existence and permissions to the original CDFDE file
    // NOTE: the cache is shared by all users.
    // The current user may not have access to a cache item previously
    // created by another user.
    if ( !Utils.getSystemOrUserReadAccess( wcdf.getPath() ).fileExists( cdeFilePath ) ) {
      throw new ThingWriteException( new FileNotFoundException( cdeFilePath ) );
    }

    // 3. Reading from the cache?
    CdfRunJsDashboardWriteResult dashWrite;
    if ( !bypassCacheRead ) {
      try {
        dashWrite = getDashboardWriteResultFromCache( cacheKey, cdeFilePath );
      } catch ( FileNotFoundException ex ) {
        // Is in cache but:
        // * file doesn't exist (anymore)
        // * user has insufficient permissions to access the cdfde file
        throw new ThingWriteException( ex );
      }

      if ( dashWrite != null ) {
        // Return cached write result
        return dashWrite;
      }

      // Not in cache or cache item expired/invalidated
    } else {
      _logger.info( "Bypassing dashboard render cache, rendering." );
    }

    // 4. Get the Dashboard object
    Dashboard dash;
    try {
      dash = this.getDashboard( wcdf, cdeFilePath, bypassCacheRead );
    } catch ( ThingReadException ex ) {
      throw new ThingWriteException( ex );
    }

    // 5. Obtain a Writer for the CdfRunJs format
    dashWrite = this.writeDashboardToCdfRunJs( dash, options, bypassCacheRead );

    // 6. Cache the dashboard write
    return this.replaceDashboardWriteResultInCache( cacheKey, dashWrite );
  }

  protected IPluginResourceLocationManager getPluginResourceLocationManager() {
    return getEnvironment().getPluginResourceLocationManager();
  }

  public Dashboard getDashboard(
      String wcdfPath,
      boolean bypassCacheRead )
    throws ThingReadException {

    try {
      DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.load( wcdfPath );
      if ( wcdf == null ) {
        throw new ThingReadException( new FileNotFoundException( wcdfPath ) );
      }

      return this.getDashboard( wcdf, bypassCacheRead );
    } catch ( IOException ex ) {
      throw new ThingReadException( "While reading dashboard.", ex );
    }
  }

  public Dashboard getDashboard( DashboardWcdfDescriptor wcdf, boolean bypassCacheRead ) throws ThingReadException {
    String cdeFilePath = Utils.sanitizeSlashesInPath( wcdf.getStructurePath() );

    // 1. Check existence and permissions to the original CDFDE file
    // NOTE: the cache is shared by all users.
    // The current user may not have access to a cache item previously
    // created by another user.
    IBasicFile cdeFile = Utils.getSystemOrUserReadAccess( cdeFilePath ).fetchFile( cdeFilePath );
    if ( cdeFile == null ) {
      throw new ThingReadException( new FileNotFoundException( cdeFilePath ) );
    }

    // 2. Get the Dashboard object
    return this.getDashboard( wcdf, cdeFilePath, bypassCacheRead );
  }
  public String getDashboardParameters( String wcdfPath, boolean bypassCacheRead ) throws ThingReadException {
    return getDashboardParameters( wcdfPath, bypassCacheRead, false );
  }

  public String getDashboardParameters( String wcdfPath, boolean bypassCacheRead, boolean all )
    throws ThingReadException {
    Dashboard dashboard = getDashboard( wcdfPath, bypassCacheRead );
    ArrayList<String> parameters = new ArrayList<String>();
    for ( Component component : dashboard.getRegulars() ) {
      if ( Arrays.asList( MAP_PARAMETERS ).contains( component.getMeta().getName() ) ) {
        // if no 'public' property is present, we must default to true
        if ( !all && Boolean.valueOf( component.tryGetPropertyValue( "public", "true" ) ) ) {
          parameters.add( component.getName() );
        }
      }
    }
    String result = "{";
    if ( parameters.size() > 0 ) {
      String params = "\n\"parameters\": [";
      for ( String parameter : parameters ) {
        params += "," + "\"" + parameter + "\"";
      }
      result += params.replaceFirst( ",", "" ) + "]";
    }
    return result + "\n}";

  }
  /**
   * Returns a json with an array of data source names.
   * Typically used by the editor, to easily map data sources with the DashboardComponent.
   *
   * @param wcdfPath the path to the dashboard to get data sources from
   * @param bypassCacheRead whether to bypassCache when loading the dashboard or not
   * @return A String representation of a json containing the list of data sources
   * */
  public String getDashboardDataSources( String wcdfPath, boolean bypassCacheRead )
    throws ThingReadException, JSONException {
    Dashboard dashboard = getDashboard( wcdfPath, bypassCacheRead );
    ArrayList<String> dataSources = new ArrayList<String>();
    for ( DataSourceComponent dataSource : dashboard.getDataSources() ) {
      dataSources.add( dataSource.getName() );
    }
    JSONObject result = new JSONObject();
    result.put( "dataSources", dataSources );
    return result.toString();
  }

  /**
   * DashboardWriteResult cache already checks the modified dates of the CDFDE and the style-template files, upon access
   * to the cache.
   * <p/>
   * When a dashboard contains widgets and those widgets' structure (internal content/CDFDE) is modified, the dashboard
   * write result is no longer valid.
   * <p/>
   * This method is proactively called whenever the CDFDE file of a dashboard, that is a widget, is saved by the editor.
   * If a widget's file is edited by hand, there's nothing implemented in access-time that detects that a dashboard's
   * contained widget has changed...
   */
  public void invalidateDashboard( String wcdfPath ) {
    // Look for cached Dashboard objects that contain the widget.

    String cdeFilePath = Utils.sanitizeSlashesInPath( DashboardWcdfDescriptor.toStructurePath( wcdfPath ) );

    Map<String, Dashboard> dashboardsByCdfdeFilePath;
    synchronized ( this._dashboardsByCdfdeFilePath ) {
      dashboardsByCdfdeFilePath = new HashMap<String, Dashboard>( this._dashboardsByCdfdeFilePath );
    }

    Set<String> invalidateDashboards = new HashSet<String>();
    invalidateDashboards.add( cdeFilePath );

    Dashboard dash = dashboardsByCdfdeFilePath.get( cdeFilePath );
    if ( dash != null && dash.getWcdf().isWidget() ) {
      collectWidgetsToInvalidate( invalidateDashboards, dashboardsByCdfdeFilePath, cdeFilePath );
    }

    if ( _logger.isDebugEnabled() ) {
      for ( String invalidCdeFilePath : invalidateDashboards ) {
        _logger.debug( "Invalidating cache of dashboard '" + invalidCdeFilePath + "'." );
      }
    }

    synchronized ( this._dashboardsByCdfdeFilePath ) {
      for ( String invalidCdeFilePath : invalidateDashboards ) {
        this._dashboardsByCdfdeFilePath.remove( invalidCdeFilePath );
      }
    }

    // Clear the DashboardWriteResult eh-cache
    synchronized ( this._ehCacheLock ) {
      List<DashboardCacheKey> ehKeys = this._ehCache.getKeys();
      for ( DashboardCacheKey ehKey : ehKeys ) {
        if ( invalidateDashboards.contains( ehKey.getCdfde() ) ) {
          this._ehCache.remove( ehKey );
        }
      }
    }
  }

  public void refreshAll() {
    this.refreshAll( true );
  }

  public void refreshAll( boolean refreshDatasources ) {
    MetaModelManager.getInstance().refresh( refreshDatasources );
    DependenciesManager.refresh();

    synchronized ( this._dashboardsByCdfdeFilePath ) {
      this._dashboardsByCdfdeFilePath.clear();
    }

    // Clear the DashboardWriteResult eh-cache
    synchronized ( this._ehCacheLock ) {
      this._ehCache.removeAll();
    }
  }

  protected IRWAccess getPluginSystemWriter() {
    return getEnvironment().getContentAccessFactory().getPluginSystemWriter( null );
  }

  private void collectWidgetsToInvalidate( Set<String> invalidateDashboards,
                                           Map<String, Dashboard> dashboardsByCdfdeFilePath,
                                           String cdeWidgetFilePath ) {

    // Find not-invalidated dashboards containing widget cdeWidgetFilePath
    for ( Dashboard dash : dashboardsByCdfdeFilePath.values() ) {
      String cdeDashFilePath = dash.getSourcePath();
      if ( !invalidateDashboards.contains( cdeDashFilePath ) ) {
        Iterable<Component> comps = dash.getRegulars();
        for ( Component comp : comps ) {
          if ( comp instanceof WidgetComponent ) {
            WidgetComponent widgetComp = (WidgetComponent) comp;
            if ( DashboardWcdfDescriptor.toStructurePath( widgetComp.getWcdfPath() ).equals( cdeWidgetFilePath ) ) {
              // This dashboard uses this widget
              invalidateDashboards.add( cdeDashFilePath );
              if ( dash.getWcdf().isWidget() ) {
                // If the dashboard is also a widget, recurse
                collectWidgetsToInvalidate(
                    invalidateDashboards,
                    dashboardsByCdfdeFilePath,
                    cdeDashFilePath );
              }
              break;
            }
          }
        }
      }
    }
  }

  private Dashboard getDashboard( DashboardWcdfDescriptor wcdf, String cdeFilePath, boolean bypassCacheRead )
    throws ThingReadException {
    Dashboard cachedDash = null;
    if ( !bypassCacheRead ) {
      cachedDash = this.getDashboardFromCache( cdeFilePath );
      if ( cachedDash == null ) {
        _logger.debug( "Dashboard instance is not in cache, reading from repository." );
      }
    } else {
      _logger.info( "Bypassing Dashboard instance cache, reading from repository." );
    }

    IReadAccess userAccess = Utils.getSystemOrUserReadAccess( cdeFilePath );
    // Read cache, cache item existed and it is valid?
    if ( cachedDash != null && cachedDash.getSourceDate().getTime() >= userAccess.getLastModified( cdeFilePath ) ) {
      // Check WCDF file date as well

      if ( !userAccess.fileExists( wcdf.getPath() ) ) {
        throw new ThingReadException( new FileNotFoundException( wcdf.getPath() ) );
      }

      if ( cachedDash.getSourceDate().getTime() >= userAccess.getLastModified( wcdf.getPath() ) ) {
        _logger.debug( "Cached Dashboard instance is valid, using it." );

        return cachedDash;
      }
    }

    if ( cachedDash != null ) {
      _logger.info( "Cached Dashboard instance invalidated, reading from repository." );
    }

    Dashboard newDash = this.readDashboardFromCdfdeJs( wcdf );

    return this.replaceDashboardInCache( cdeFilePath, newDash, cachedDash );
  }

  private Dashboard readDashboardFromCdfdeJs( DashboardWcdfDescriptor wcdf ) throws ThingReadException {
    // 1. Open the CDFDE file.
    String cdeFilePath = wcdf.getStructurePath();
    JXPathContext cdfdeDoc;

    try {
      cdfdeDoc = openDashboardAsJXPathContext( wcdf );
    } catch ( FileNotFoundException ex ) {
      // File does not exist or
      // User has insufficient permissions
      throw new ThingReadException( "The CDFDE dashboard file does not exist.", ex );
    } catch ( IOException ex ) {
      throw new ThingReadException( "While accessing the CDFDE dashboard file.", ex );
    } catch ( JSONException ex ) {
      throw new ThingReadException( "While opening the CDFDE dashboard file as a JXPathContext.", ex );
    }

    // 2. Obtain a reader to read the dashboard file
    MetaModel metaModel = MetaModelManager.getInstance().getModel();
    CdfdeJsThingReaderFactory thingReaderFactory = new CdfdeJsThingReaderFactory( metaModel );
    IThingReader reader;
    try {
      reader = thingReaderFactory.getReader( KnownThingKind.Dashboard, null, null );
    } catch ( UnsupportedThingException ex ) {
      throw new ThingReadException( "While obtaining a reader for a dashboard.", ex );
    }

    // 3. Read it
    IThingReadContext readContext = new CdfdeJsReadContext( thingReaderFactory, wcdf, metaModel );
    Dashboard.Builder dashBuilder = (Dashboard.Builder) reader.read( readContext, cdfdeDoc, cdeFilePath );

    // 4. Build it
    try {
      return dashBuilder.build( metaModel );
    } catch ( ValidationException ex ) {
      throw new ThingReadException( "While building the read dashboard.", ex );
    }
  }

  /**
   * @param dash
   * @param options
   * @param bypassCacheRead
   * @return
   * @throws ThingWriteException
   */
  private CdfRunJsDashboardWriteResult writeDashboardToCdfRunJs( Dashboard dash, CdfRunJsDashboardWriteOptions options,
                                                                 boolean bypassCacheRead ) throws ThingWriteException {

    // 1. Obtain a Writer for the CdfRunJs format
    IThingWriterFactory factory;
    IThingWriter writer;

    if ( dash.getWcdf().isRequire() ) {
      // AMD version
      pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory amdFactory =
          new pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory();

      if ( options.isAmdModule() ) {
        // write the dashboard as an AMD module definition
        writer = amdFactory.getDashboardModuleWriter( dash );
      } else {
        // write the dashboard as a required AMD module
        writer = amdFactory.getDashboardWriter( dash );
      }

      factory = amdFactory;
    } else {
      // Legacy version
      CdfRunJsThingWriterFactory legacyFactory = new CdfRunJsThingWriterFactory();

      writer = legacyFactory.getDashboardWriter( dash );

      factory = legacyFactory;
    }

    // 2. Write it
    CdfRunJsDashboardWriteContext writeContext = CdeEngine.getInstance().getEnvironment()
        .getCdfRunJsDashboardWriteContext( factory, /*indent*/"", bypassCacheRead, dash, options );

    CdfRunJsDashboardWriteResult.Builder dashboardWriteBuilder = new CdfRunJsDashboardWriteResult.Builder();
    writer.write( dashboardWriteBuilder, writeContext, dash );

    return dashboardWriteBuilder.build();
  }

  private CdfRunJsDashboardWriteResult
      getDashboardWriteResultFromCache( DashboardCacheKey cacheKey, String cdeFilePath ) throws FileNotFoundException {

    IReadAccess userContentAccess = Utils.getSystemOrUserReadAccess( cdeFilePath );

    // 1. Try to obtain dashboard from cache
    Element cacheElement;
    try {
      synchronized ( this._ehCacheLock ) {
        cacheElement = this._ehCache.get( cacheKey );
      }
    } catch ( CacheException ex ) {
      _logger.info( "Cached dashboard render invalidated, re-rendering." );
      return null;
    }

    // 2. In the cache?
    if ( cacheElement == null ) {
      _logger.debug( "Dashboard render is not in cache." );
      return null;
    }

    CdfRunJsDashboardWriteResult dashWrite = (CdfRunJsDashboardWriteResult) cacheElement.getValue();

    // 3. Get the template file
    String templPath = cacheKey.getTemplate();

    // 4. Check if cache item has expired
    // Cache is invalidated if the dashboard or template have changed since
    // the cache was loaded, or at midnight every day,
    // because of dynamic generation of date parameters.
    Calendar cal = Calendar.getInstance();
    cal.set( Calendar.HOUR_OF_DAY, 00 );
    cal.set( Calendar.MINUTE, 00 );
    cal.set( Calendar.SECOND, 1 );

    // The date at which the source Dashboard object
    // was loaded from disk, not the date at which the DashResult was written.
    Date dashLoadedDate = dashWrite.getLoadedDate();

    boolean cacheExpired = cal.getTime().after( dashLoadedDate );
    if ( cacheExpired ) {
      _logger.debug( "Cached dashboard render expired, re-rendering." );
      return null;
    }

    boolean cacheInvalid =
        ( userContentAccess.getLastModified( cdeFilePath ) > dashLoadedDate.getTime() )
        || ( userContentAccess.fileExists( templPath )
        && userContentAccess.getLastModified( templPath ) > dashLoadedDate.getTime() );
    if ( cacheInvalid ) {
      _logger.info( "Cached dashboard render invalidated, re-rendering." );
      return null;
    }

    _logger.info( "Cached dashboard render is valid, using it." );

    return dashWrite;
  }

  private CdfRunJsDashboardWriteResult replaceDashboardWriteResultInCache( DashboardCacheKey cacheKey,
                                                                           CdfRunJsDashboardWriteResult newDashWrite ) {
    synchronized ( this._ehCacheLock ) {
      Element cacheElement;
      try {
        cacheElement = this._ehCache.get( cacheKey );
      } catch ( CacheException ex ) {
        cacheElement = null;
      }

      if ( cacheElement != null ) {
        // Keep the one which corresponds to the newest Dashboard object
        // read from disk.
        CdfRunJsDashboardWriteResult currDashWrite =
            (CdfRunJsDashboardWriteResult) cacheElement.getValue();

        if ( currDashWrite.getLoadedDate().getTime() > newDashWrite.getLoadedDate().getTime() ) {
          return currDashWrite;
        }
      }

      try {
        this._ehCache.put( new Element( cacheKey, newDashWrite ) );
      } catch ( Exception cnfe ) {
        //This is throwing a class not found sometimes... Trying to figure out why
        _logger.warn( "Class not found for cache key while writing to cache.", cnfe );
      }

      return newDashWrite;
    }
  }

  private CacheManager createWriteResultCacheManager() throws CacheException {
    // 'new CacheManager' used instead of 'CacheManager.create' to avoid overriding default cache
    CacheManager cacheMgr;
    try {
      cacheMgr = new CacheManager(
        getEnvironment()
          .getContentAccessFactory()
          .getPluginSystemReader( null )
          .getFileInputStream( CACHE_CFG_FILE ) );
    } catch ( IOException e ) {
      _logger.error( "Cache failed to load the configuration file.", e );
      cacheMgr = new CacheManager();
    }

    // enableCacheProperShutdown
    System.setProperty( CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true" );

    return cacheMgr;
  }

  protected IReadAccess getPluginSystemReader() {
    return getEnvironment().getContentAccessFactory().getPluginSystemReader( null );
  }

  private Dashboard getDashboardFromCache( String cdeFullPath ) {
    synchronized ( this._dashboardsByCdfdeFilePath ) {
      return this._dashboardsByCdfdeFilePath.get( cdeFullPath );
    }
  }

  private Dashboard replaceDashboardInCache( String cdeFullPath, Dashboard newDash, Dashboard oldDash ) {
    assert newDash != null;

    synchronized ( this._dashboardsByCdfdeFilePath ) {
      if ( oldDash != null ) { // otherwise ignore
        Dashboard currDash = this._dashboardsByCdfdeFilePath.get( cdeFullPath );
        if ( currDash != null && currDash != oldDash ) {
          // Do not set.
          // Assume newer
          return currDash;
        }
      }

      this._dashboardsByCdfdeFilePath.put( cdeFullPath, newDash );
      return newDash;
    }
  }
}
