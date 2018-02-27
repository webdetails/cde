/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
package org.pentaho.ctools.cde.environment;

import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.ctools.cde.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.InitializationException;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.session.IUserSession;

public class CdeEnvironment implements ICdeEnvironment {

  private static final Log logger = LogFactory.getLog( CdeEnvironment.class );
  private static final String PLUGIN_REPOSITORY_DIR = "/public/cde";
  private static final String CDE_XML = "cde.xml";
  private static final String SYSTEM_DIR = "system";
  private static final String PLUGIN = "plugin";
  private static final String DEFAULT_PLUGIN_ID = "cde";

  private IPluginResourceLocationManager pluginResourceLocationManager;
  private IContentAccessFactory contentAccessFactory;
  private IDataSourceManager dataSourceManager;

  public CdeEnvironment() {
    pluginResourceLocationManager = new PluginResourceLocationManager();
  }

  @Override
  public void init( IBeanFactory factory ) throws InitializationException {
    logger.fatal( "Not implemented for the OSGi environment" );
  }

  @Override
  public Locale getLocale() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public IResourceLoader getResourceLoader() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute, String absRoot,
                                String scheme ) throws Exception {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public ICdeApiPathProvider getExtApi() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public IFileHandler getFileHandler() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public IUrlProvider getUrlProvider() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public IUserSession getUserSession() {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  @Override
  public void refresh() {
    logger.fatal( "Not implemented for the OSGi environment" );
  }

  @Override
  public String getApplicationBaseUrl() {
    return "";
  }

  @Override
  public String getApplicationReposUrl() {
    return "";
  }

  @Override
  public IDataSourceManager getDataSourceManager() {
    return this.dataSourceManager;
  }

  public void setDataSourceManager( IDataSourceManager dataSourceManager ) {
    this.dataSourceManager = dataSourceManager;
  }

  @Override
  public IPluginResourceLocationManager getPluginResourceLocationManager() {
    return pluginResourceLocationManager;
  }

  public void setPluginResourceLocationManager( IPluginResourceLocationManager pluginResourceLocationManager ) {
    this.pluginResourceLocationManager = pluginResourceLocationManager;
  }

  @Override
  public String getPluginRepositoryDir() {
    return PLUGIN_REPOSITORY_DIR;
  }

  @Override
  public String getPluginId() {
    return DEFAULT_PLUGIN_ID; // TODO: any reason to keep supporting???
  }

  @Override
  public PluginEnvironment getPluginEnv() {
    return null;
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
    return Utils.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + "/res/"; // TODO: review for osgi, deprecate ???
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
    if ( getContentAccessFactory().getUserContentAccess( "/" )
        .fileExists( PLUGIN_REPOSITORY_DIR + "/" + CDE_XML ) ) {
      return getContentAccessFactory().getUserContentAccess( "/" )
        .fetchFile( PLUGIN_REPOSITORY_DIR + "/" + CDE_XML );
    } else if ( getContentAccessFactory().getPluginSystemReader( null ).fileExists( CDE_XML ) ) {
      return getContentAccessFactory().getPluginSystemReader( null ).fetchFile( CDE_XML );
    }
    return null;
  }

  @Override
  public boolean canCreateContent() {
    return false;
  }

  @Override
  public IContentAccessFactory getContentAccessFactory() {
    return this.contentAccessFactory;
  }

  public void setContentAccessFactory( IContentAccessFactory contentAccessFactory ) {
    this.contentAccessFactory = contentAccessFactory;
  }
}
