/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.CdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironmentExtended {


  private static final String PLUGIN_REPOSITORY_DIR = "/public/cde";
  private static final String SYSTEM_DIR = "system";
  private static final String PLUGIN = "plugin";
  private static final String CDE_XML = "cde.xml";
  protected static Log logger = LogFactory.getLog( PentahoCdeEnvironment.class );
  private ICdeBeanFactory factory;
  private IResourceLoader resourceLoader;

  private IPluginResourceLocationManager pluginResourceLocationManager;
  private ICdeApiPathProvider apiPaths;
  private IFileHandler fileHandler;

  public PentahoCdeEnvironment() {
    
  }

  public void init( ICdeBeanFactory factory ) throws InitializationException {
    this.factory = factory;

    pluginResourceLocationManager = new PluginResourceLocationManager();

    if ( factory.containsBean( IResourceLoader.class.getSimpleName() ) ) {
      resourceLoader = (IResourceLoader) factory.getBean( IResourceLoader.class.getSimpleName() );
    }
    
    if ( factory.containsBean( IFileHandler.class.getSimpleName() ) ) {
      fileHandler = (IFileHandler) factory.getBean( IFileHandler.class.getSimpleName() );
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
    return Utils.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + "/res/";// TODO:
  }

  public String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws Exception {
    return InterPluginBroker.getCdfIncludes( dashboard, type, debug, absRoot, scheme );
  }

//  public String getCdfContext(String dashboard, String action, String viewId) throws Exception {
//    return InterPluginBroker.getCdfContext( dashboard, action, viewId );
//  }

  public PentahoPluginEnvironment getPluginEnv() {
    return PentahoPluginEnvironment.getInstance();
  }

  public ICdeApiPathProvider getExtApi() {
    // not worth the sync
    if (apiPaths == null) {
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
      boolean bypassCacheRead, Dashboard dash, CdfRunJsDashboardWriteOptions options ) {
    return new PentahoCdfRunJsDashboardWriteContext( factory, indent, bypassCacheRead, dash, options );
  }

  @Override public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory, String indent ) {
    return new PentahoCdfRunJsDashboardWriteContext( factory, indent );
  }

  @Override public IBasicFile getCdeXml() {
    if ( getUserContentAccess( "/" ).fileExists( "/public/cde/" + CDE_XML ) ) {
      return getUserContentAccess("/" ).fetchFile( "/public/cde/" + CDE_XML  );
    } else if ( getPluginSystemReader( null ).fileExists( CDE_XML ) ) {
      return getPluginSystemReader( null ).fetchFile( CDE_XML );
    }
    return null;
  }
}
