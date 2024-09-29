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

import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.testUtils.ContentAccessFactoryForTests;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.session.IUserSession;

import java.util.Locale;

public class CdeEnvironmentForTests implements ICdeEnvironment {

  private IUserContentAccess mockedContentAccess;
  private IReadAccess mockedReadAccess;
  private IRWAccess mockedRWAccess;
  private IPluginResourceLocationManager mockedPluginResourceLocationManager;
  private IDataSourceManager mockedDataSourceManager;
  private IUrlProvider mockedUrlProvider;
  private IUserSession mockedUserSession;
  private ICdeApiPathProvider mockedCdeApiPathProvider;
  private boolean canCreateContent;
  private final String SYSTEM_DIR = "system";

  public void setMockedContentAccess( IUserContentAccess mockedContentAccess ) {
    this.mockedContentAccess = mockedContentAccess;
  }

  public void setMockedReadAccess( IReadAccess mockedReadAccess ) {
    this.mockedReadAccess = mockedReadAccess;
  }

  public void setMockedRWAccess( IRWAccess mockedRWAccess ) {
    this.mockedRWAccess = mockedRWAccess;
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

  public void setMockedUserSession( IUserSession mockedUserSession ) {
    this.mockedUserSession = mockedUserSession;
  }

  public void setCanCreateContent( boolean canCreateContent ) {
    this.canCreateContent = canCreateContent;
  }

  public void setMockedCdeApiPathProvider( ICdeApiPathProvider mockedCdeApiPathProvider ) {
    this.mockedCdeApiPathProvider = mockedCdeApiPathProvider;
  }

  @Override
  public void init( IBeanFactory factory ) throws InitializationException {
    canCreateContent = true;
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
    return Locale.getDefault();
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
    if ( this.mockedRWAccess != null ) {
      return new ContentAccessFactoryForTests( mockedContentAccess, mockedReadAccess, mockedRWAccess );
    }
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
    return mockedCdeApiPathProvider;
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

  @Override
  public IUserSession getUserSession() {
    return mockedUserSession;
  }

  @Override
  public boolean canCreateContent() {
    return canCreateContent;
  }
}

