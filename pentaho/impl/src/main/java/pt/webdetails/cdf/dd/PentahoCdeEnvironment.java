/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PentahoUrlProvider;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.session.PentahoSessionUtils;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironmentExtended {


  private static final String PLUGIN_REPOSITORY_DIR = "/public/cde";
  private static final String SYSTEM_DIR = "system";
  private static final String PLUGIN = "plugin";
  private static final String CDE_XML = "cde.xml";
  private static final String API_REPOS = "api/repos/";
  protected static Log logger = LogFactory.getLog( PentahoCdeEnvironment.class );
  private IBeanFactory factory;
  private IResourceLoader resourceLoader;

  private IPluginResourceLocationManager pluginResourceLocationManager;
  private ICdeApiPathProvider apiPaths;
  private IFileHandler fileHandler;
  private IAuthorizationPolicy authorizationPolicy;

  public PentahoCdeEnvironment() {

  }

  public void init( IBeanFactory factory ) throws InitializationException {
    this.factory = factory;

    pluginResourceLocationManager = new PluginResourceLocationManager();

    if ( factory.containsBean( IResourceLoader.class.getSimpleName() ) ) {
      resourceLoader = (IResourceLoader) factory.getBean( IResourceLoader.class.getSimpleName() );
    }

    if ( factory.containsBean( IFileHandler.class.getSimpleName() ) ) {
      fileHandler = (IFileHandler) factory.getBean( IFileHandler.class.getSimpleName() );
    }

    if ( factory.containsBean( IAuthorizationPolicy.class.getSimpleName() ) ) {
      authorizationPolicy = (IAuthorizationPolicy) factory.getBean( IAuthorizationPolicy.class.getSimpleName() );
    }

    super.init( this );
  }

  @Override
  public void refresh() {
    try {
      init( this.factory );
    } catch ( InitializationException e ) {
      logger.error( "PentahoCdeEnvironment.refresh()", e );
    }
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

  @Override
  public IResourceLoader getResourceLoader() {
    return resourceLoader;
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

  public String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute, String absRoot,
                                String scheme ) throws Exception {
    return InterPluginBroker.getCdfIncludes( dashboard, type, debug, absolute, absRoot, scheme );
  }

  //  public String getCdfContext(String dashboard, String action, String view) throws Exception {
  //    return InterPluginBroker.getCdfContext( dashboard, action, view );
  //  }

  public PentahoPluginEnvironment getPluginEnv() {
    return PentahoPluginEnvironment.getInstance();
  }

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

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                                                         boolean bypassCacheRead, Dashboard dash,
                                                                         CdfRunJsDashboardWriteOptions options ) {
    if ( dash.getWcdf().isRequire() ) {
      return new pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContext(
          factory, indent, bypassCacheRead, dash, options );
    } else {
      return new PentahoCdfRunJsDashboardWriteContext( factory, indent, bypassCacheRead, dash, options );
    }
  }

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                                                         String indent ) {
    return new PentahoCdfRunJsDashboardWriteContext( factory, indent );
  }

  @Override
  public IBasicFile getCdeXml() {
    if ( getUserContentAccess( "/" ).fileExists( PLUGIN_REPOSITORY_DIR + "/" + CDE_XML ) ) {
      return getUserContentAccess( "/" ).fetchFile( PLUGIN_REPOSITORY_DIR + "/" + CDE_XML );
    } else if ( getPluginSystemReader( null ).fileExists( CDE_XML ) ) {
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
