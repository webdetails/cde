/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.DataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.IPluginCall;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.resources.IResourceLoader;

import java.util.Locale;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironment {

  private static final String PLUGIN_REPOSITORY_DIR = "cde";
  private static final String SYSTEM_DIR = "system";
  private static final String CONTENT = "plugin";
  protected static Log logger = LogFactory.getLog( PentahoCdeEnvironment.class );
  private ICdeBeanFactory factory;
  private IPluginCall interPluginCall;
  private IResourceLoader resourceLoader;
  private IPluginResourceLocationManager pluginResourceLocationManager;

  public void init( ICdeBeanFactory factory ) throws InitializationException {
    this.factory = factory;

    pluginResourceLocationManager = new PluginResourceLocationManager();

    if ( factory.containsBean( IResourceLoader.class.getSimpleName() ) ) {
      resourceLoader = (IResourceLoader) factory.getBean( IResourceLoader.class.getSimpleName() );
    }

    if ( factory.containsBean( IPluginCall.class.getSimpleName() ) ) {
      interPluginCall = (IPluginCall) factory.getBean( IPluginCall.class.getSimpleName() );
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

  @Override
  public String getApplicationBaseUrl() {
    return PentahoSystem.getApplicationContext().getBaseUrl();
  }

  @Override
  public IDataSourceManager getDataSourceManager() {
    return DataSourceManager.getInstance();
  }

  @Override
  public IPluginCall getInterPluginCall() {
    return interPluginCall;
  }

  @Override
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
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/";
  }

  @Override
  public String getRepositoryBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/res/";// TODO:
  }
}
