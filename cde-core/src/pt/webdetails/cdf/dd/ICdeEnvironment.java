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

  public void init( IBeanFactory factory ) throws InitializationException;

  public void refresh();

  public String getApplicationBaseUrl();

  public Locale getLocale();

  // public IPluginCall getInterPluginCall();

  public IResourceLoader getResourceLoader();

  public IDataSourceManager getDataSourceManager();

  public IPluginResourceLocationManager getPluginResourceLocationManager();

  public IContentAccessFactory getContentAccessFactory();

  public String getPluginRepositoryDir();

  public String getSystemDir();

  public String getPluginId();

  PluginEnvironment getPluginEnv();

  public ICdeApiPathProvider getExtApi();

  /**
   * TODO: replace with urlprovider
   *
   * @return Base content URL <u>for this plugin</u>
   */
  public String getApplicationBaseContentUrl();

  public String getApplicationReposUrl();

  public String getRepositoryBaseContentUrl();

  public IUrlProvider getUrlProvider();

  String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute, String absRoot, String scheme )
    throws Exception;

  //String getCdfContext( String dashboard, String action, String view ) throws Exception;

  public IFileHandler getFileHandler();

  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                                                         boolean bypassCacheRead, Dashboard dash,
                                                                         CdfRunJsDashboardWriteOptions options );

  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                                                         String indent );

  public IBasicFile getCdeXml();

  public IUserSession getUserSession();

  public boolean canCreateContent();

}
