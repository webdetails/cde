/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd;

import java.util.Locale;

import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.session.IUserSession;

public interface ICdeEnvironment {

  void init( IBeanFactory factory ) throws InitializationException;

  void refresh();

  String getApplicationBaseUrl();

  Locale getLocale();

  IResourceLoader getResourceLoader();

  IDataSourceManager getDataSourceManager();

  IPluginResourceLocationManager getPluginResourceLocationManager();

  IContentAccessFactory getContentAccessFactory();

  String getPluginRepositoryDir();

  String getSystemDir();

  String getPluginId();

  PluginEnvironment getPluginEnv();

  ICdeApiPathProvider getExtApi();

  /**
   * TODO: replace with urlprovider
   *
   * @return Base content URL <u>for this plugin</u>
   */
  String getApplicationBaseContentUrl();

  String getApplicationReposUrl();

  String getRepositoryBaseContentUrl();

  IUrlProvider getUrlProvider();

  String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute,
                         String absRoot, String scheme ) throws Exception;

  IFileHandler getFileHandler();

  CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                                                  boolean bypassCacheRead, Dashboard dash,
                                                                  CdfRunJsDashboardWriteOptions options );

  CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                                                  String indent );

  IBasicFile getCdeXml();

  IUserSession getUserSession();

  boolean canCreateContent();

}
