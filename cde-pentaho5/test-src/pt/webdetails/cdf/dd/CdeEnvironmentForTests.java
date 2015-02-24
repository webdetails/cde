/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.testUtils.ContentAccessFactoryForTests;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.resources.IResourceLoader;

import java.util.Locale;

public class CdeEnvironmentForTests implements ICdeEnvironment {

  private IUserContentAccess mockedContentAccess;
  private IReadAccess mockedReadAccess;
  private IPluginResourceLocationManager mockedPluginResourceLocationManager;
  private IDataSourceManager mockedDataSourceManager;
  private IUrlProvider mockedUrlProvider;
  private final String SYSTEM_DIR = "system";

  public void setMockedContentAccess( IUserContentAccess mockedContentAccess ) {
    this.mockedContentAccess = mockedContentAccess;
  }

  public void setMockedReadAccess( IReadAccess mockedReadAccess ) {
    this.mockedReadAccess = mockedReadAccess;
  }

  public void setMockedPluginResourceLocationManager(
      IPluginResourceLocationManager mockedPluginResourceLocationManager ) {
    this.mockedPluginResourceLocationManager = mockedPluginResourceLocationManager;
  }

  public void setMockedDataSourceManager( IDataSourceManager mockedDataSourceManager ) {
    this.mockedDataSourceManager = mockedDataSourceManager;
  }

  public void setMockedUrlProvider( IUrlProvider mockedUrlProvider ) {
    this.mockedUrlProvider = mockedUrlProvider;
  }

  @Override
  public void init( ICdeBeanFactory factory ) throws InitializationException {

  }

  @Override
  public void refresh() {

  }

  @Override
  public String getApplicationBaseUrl() {
    return null;
  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public IResourceLoader getResourceLoader() {
    return null;
  }

  @Override
  public IDataSourceManager getDataSourceManager() {
    return mockedDataSourceManager;
  }

  @Override
  public IPluginResourceLocationManager getPluginResourceLocationManager() {
    return mockedPluginResourceLocationManager;
  }

  @Override
  public IContentAccessFactory getContentAccessFactory() {
    return new ContentAccessFactoryForTests( mockedContentAccess, mockedReadAccess );
  }

  @Override
  public String getPluginRepositoryDir() {
    return null;
  }

  @Override
  public String getSystemDir() {
    return SYSTEM_DIR;
  }

  @Override
  public String getPluginId() {
    return null;
  }

  @Override
  public PluginEnvironment getPluginEnv() {
    return null;
  }

  @Override
  public ICdeApiPathProvider getExtApi() {
    return null;
  }

  @Override
  public String getApplicationBaseContentUrl() {
    return null;
  }

  @Override
  public String getApplicationReposUrl() {
    return null;
  }

  @Override
  public String getRepositoryBaseContentUrl() {
    return null;
  }

  @Override
  public IUrlProvider getUrlProvider() {
    return mockedUrlProvider;
  }

  @Override
  public String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute, String absRoot,
                                String scheme ) throws Exception {
    return null;
  }

  @Override
  public IFileHandler getFileHandler() {
    return null;
  }

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( IThingWriterFactory factory, String indent,
                                                                         boolean bypassCacheRead, Dashboard dash,
                                                                         CdfRunJsDashboardWriteOptions options ) {
    return new PentahoCdfRunJsDashboardWriteContext( factory, indent, bypassCacheRead, dash, options );
  }

  @Override
  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext( CdfRunJsDashboardWriteContext factory,
                                                                         String indent ) {
    return null;
  }

  @Override
  public IBasicFile getCdeXml() {
    return null;
  }

}
