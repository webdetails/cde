/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package pt.webdetails.cdf.dd;

import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.util.messages.LocaleHelper;
import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.CdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.FileHandler;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PentahoUrlProvider;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.resources.PentahoPluginResourceLoader;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.session.PentahoSessionUtils;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironmentExtended {

  protected static Log logger = LogFactory.getLog( PentahoCdeEnvironment.class );

  private static final String PLUGIN_REPOSITORY_DIR = "/public/cde";
  private static final String SYSTEM_DIR = "system";
  private static final String PLUGIN = "plugin";
  private static final String CDE_XML = "cde.xml";
  private static final String API_REPOS = "api/repos/";

  private IBeanFactory factory;
  private IResourceLoader resourceLoader;

  private IPluginResourceLocationManager pluginResourceLocationManager;
  private ICdeApiPathProvider apiPaths;
  private IFileHandler fileHandler;
  private IAuthorizationPolicy authorizationPolicy;

  public PentahoCdeEnvironment() {
    //Needs to be here due to inheritance chain
  }

  @Override
  public void init( IBeanFactory factory ) {
    this.factory = factory;

    PluginEnvironment.init( this );
  }

  @Override
  public void refresh() {
    init( this.factory );
  }

  public String getApplicationBaseUrl() {
    return PentahoSystem.getApplicationContext().getBaseUrl();
  }

  public String getApplicationReposUrl() {
    return getApplicationBaseUrl() + API_REPOS;
  }

  @Override
  public IDataSourceManager getDataSourceManager() {
    return DataSourceManager.getInstance();
  }


  public Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  @Override
  public IPluginResourceLocationManager getPluginResourceLocationManager() {
    return pluginResourceLocationManager;
  }

  public void setPluginResourceLocationManager( IPluginResourceLocationManager pluginResourceLocationManager ) {
    this.pluginResourceLocationManager = pluginResourceLocationManager;
  }

  @Override
  public IResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  public void setResourceLoader( PentahoPluginResourceLoader resourceLoader ) {
    this.resourceLoader = resourceLoader;
  }

  public void setAuthorizationPolicy( IAuthorizationPolicy authorizationPolicy ) {
    this.authorizationPolicy = authorizationPolicy;
  }

  @Override
  public String getPluginRepositoryDir() {
    return PLUGIN_REPOSITORY_DIR;
  }

  @Override
  public String getPluginId() {
    return super.getPluginId();
  }

  @Override
  public String getSystemDir() {
    return SYSTEM_DIR;
  }

  @Override
  public String getApplicationBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + "/";
  }

  @Override
  public String getRepositoryBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + "/res/"; // TODO:
  }

  @Override
  public String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute, String absRoot,
                                String scheme ) throws Exception {
    return InterPluginBroker.getCdfIncludes( dashboard, type, debug, absolute, absRoot, scheme );
  }

  @Override
  public PluginEnvironment getPluginEnv() {
    return PentahoPluginEnvironment.getInstance();
  }

  @Override
  public ICdeApiPathProvider getExtApi() {
    // not worth the sync
    if ( apiPaths == null ) {
      apiPaths = new CdeApiPathProvider( getPluginEnv().getUrlProvider() );
    }

    return apiPaths;
  }

  @Override
  public IFileHandler getFileHandler() {
    return fileHandler;
  }

  public void setFileHandler( FileHandler fileHandler ) {
    this.fileHandler = fileHandler;
  }

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                                                         boolean bypassCacheRead, Dashboard dash,
                                                                         CdfRunJsDashboardWriteOptions options ) {
    if ( dash.getWcdf().isRequire() ) {
      return new pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContext(
          factory, indent, bypassCacheRead, dash, options );
    }

    return new PentahoCdfRunJsDashboardWriteContext( factory, indent, bypassCacheRead, dash, options );
  }

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                                                         String indent ) {
    return new PentahoCdfRunJsDashboardWriteContext( factory, indent );
  }

  @Override
  public IBasicFile getCdeXml() {
    final String cdeXmlFile = PLUGIN_REPOSITORY_DIR + "/" + CDE_XML;
    if ( getUserContentAccess( "/" ).fileExists( cdeXmlFile  ) ) {
      return getUserContentAccess( "/" ).fetchFile( cdeXmlFile );
    }

    if ( getPluginSystemReader( null ).fileExists( CDE_XML ) ) {
      return getPluginSystemReader( null ).fetchFile( CDE_XML );
    }

    return null;
  }

  @Override
  public IUrlProvider getUrlProvider() {
    return new PentahoUrlProvider( getPluginId() ) {
      @Override
      public String getResourcesBasePath() {
        return getExtApi().getResourcesBasePath();
      }
    };
  }

  @Override
  public IUserSession getUserSession() {
    return new PentahoSessionUtils().getCurrentSession();
  }

  @Override
  public boolean canCreateContent() {
    if ( authorizationPolicy == null ) {
      authorizationPolicy = PentahoSystem.get( IAuthorizationPolicy.class );

      if ( authorizationPolicy == null ) {
        logger.warn( "Couldn't retrieve Authorization Policy" );

        return getUserSession().isAdministrator();
      }
    }

    return authorizationPolicy.isAllowed( RepositoryCreateAction.NAME );
  }
}
