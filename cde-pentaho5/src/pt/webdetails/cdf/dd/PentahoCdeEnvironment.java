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
import pt.webdetails.cdf.dd.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.resources.IResourceLoader;

public class PentahoCdeEnvironment extends PentahoPluginEnvironment implements ICdeEnvironment {


  private static final String PLUGIN_REPOSITORY_DIR = "cde";
  private static final String SYSTEM_DIR = "system";
  private static final String CONTENT = "plugin";
  protected static Log logger = LogFactory.getLog( PentahoCdeEnvironment.class );
  private ICdeBeanFactory factory;
  private IResourceLoader resourceLoader;

  private IPluginResourceLocationManager pluginResourceLocationManager;

  public PentahoCdeEnvironment() {
    
  }

  public void init( ICdeBeanFactory factory ) throws InitializationException {
    this.factory = factory;

    pluginResourceLocationManager = new PluginResourceLocationManager();

    if ( factory.containsBean( IResourceLoader.class.getSimpleName() ) ) {
      resourceLoader = (IResourceLoader) factory.getBean( IResourceLoader.class.getSimpleName() );
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
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/";
  }

  @Override
  public String getRepositoryBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/res/";// TODO:
  }

  public String getCdfIncludes(String dashboard, String type, boolean debug, String absRoot, String scheme) throws Exception {
    return InterPluginBroker.getCdfIncludes( dashboard, type, debug, absRoot, scheme );
}
}
