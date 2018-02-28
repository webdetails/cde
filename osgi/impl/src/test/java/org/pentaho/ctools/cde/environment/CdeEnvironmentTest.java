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

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.ctools.cde.datasources.manager.DataSourceManager;
import org.pentaho.ctools.cde.plugin.resource.PluginResourceLocationManager;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class CdeEnvironmentTest {
  private static final String PLUGIN_REPOSITORY_DIR = "/public/cde";
  private CdeEnvironment cdeEnvironment;
  private DataSourceManager dataSourceManager;
  private PluginResourceLocationManager pluginResourceLocationManager;

  @Before
  public void setUp() {
    cdeEnvironment = new CdeEnvironment();
    dataSourceManager = Mockito.mock( DataSourceManager.class );
    cdeEnvironment.setDataSourceManager( dataSourceManager );
    pluginResourceLocationManager = Mockito.mock( PluginResourceLocationManager.class );
  }

  @After
  public void tearDown() {
    cdeEnvironment = null;
  }

  @Test
  public void testGetLocale() {
    assertNull( cdeEnvironment.getLocale() );
  }

  @Test
  public void testGetResourceLoader() {
    assertNull( cdeEnvironment.getResourceLoader() );
  }

  @Test
  public void testGetCdfIncludes() throws Exception {
    assertNull( cdeEnvironment.getCdfIncludes( null, null, false, false, null, null ) );
  }

  @Test
  public void testGetExtApi() {
    assertNull( cdeEnvironment.getExtApi() );
  }

  @Test
  public void testGetFileHandler() {
    assertNull( cdeEnvironment.getFileHandler() );
  }

  @Test
  public void testGetUrlProvider() {
    assertNull( cdeEnvironment.getUrlProvider() );
  }

  @Test
  public void testGetUserSession() {
    assertNull( cdeEnvironment.getUserSession() );
  }

  @Test
  public void testGetApplicationBaseUrl() {
    assertEquals( StringUtils.EMPTY, cdeEnvironment.getApplicationBaseUrl() );
  }

  @Test
  public void testGetApplicationReposUrl() {
    assertEquals( StringUtils.EMPTY, cdeEnvironment.getApplicationReposUrl() );
  }

  @Test
  public void testGetDataSourceManager() {
    assertEquals( dataSourceManager, cdeEnvironment.getDataSourceManager() );
  }

  @Test
  public void testSetDataSourceManager() {
    final IDataSourceManager expected = Mockito.mock( IDataSourceManager.class );
    cdeEnvironment.setDataSourceManager( expected );
    assertEquals( expected, cdeEnvironment.getDataSourceManager() );
  }

  @Test
  public void testSetPluginResourceLocationManager() {
    final IPluginResourceLocationManager expected = Mockito.mock( IPluginResourceLocationManager.class );
    cdeEnvironment.setPluginResourceLocationManager( expected );
    assertEquals( expected, cdeEnvironment.getPluginResourceLocationManager() );
  }

  @Test
  public void testGetPluginRepositoryDir() {
    assertEquals( PLUGIN_REPOSITORY_DIR, cdeEnvironment.getPluginRepositoryDir() );
  }

  @Test
  public void testGetPluginId() {
    assertEquals( "cde", cdeEnvironment.getPluginId() );
  }

  @Test
  public void testGetPluginEnv() {
    assertNull( cdeEnvironment.getPluginEnv() );
  }

  @Test
  public void testGetSystemDir() {
    final String expected = "system";
    assertEquals( expected, cdeEnvironment.getSystemDir() );
  }

  @Test
  public void testGetApplicationBaseContentUrl() {
    final String expected = "/plugin/cde/";
    assertEquals( expected, cdeEnvironment.getApplicationBaseContentUrl() );
  }

  @Test
  public void testGetRepositoryBaseContentUrl() {
    final String expected = "/plugin/cde/res/";
    assertEquals( expected, cdeEnvironment.getRepositoryBaseContentUrl() );
  }

  @Test
  public void testGetCdfRunJsDashboardWriteContext() {
    final DashboardWcdfDescriptor dashboardWcdfDescriptor = Mockito.mock( DashboardWcdfDescriptor.class );
    when( dashboardWcdfDescriptor.isRequire() ).thenReturn( true );
    final Dashboard dash = Mockito.mock( Dashboard.class );
    when( dash.getWcdf() ).thenReturn( dashboardWcdfDescriptor );
    final IThingWriterFactory factory = Mockito.mock( CdfRunJsThingWriterFactory.class );

    final CdfRunJsDashboardWriteContext cdfRunJsDashboardWriteContext = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    when( cdfRunJsDashboardWriteContext.getFactory() ).thenReturn( factory );
    assertThat(
      cdeEnvironment.getCdfRunJsDashboardWriteContext( cdfRunJsDashboardWriteContext, "" ),
      instanceOf( pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext.class ) );
  }

  @Test
  public void testGetCdfRunJsDashboardWriteContextAMD() {
    final DashboardWcdfDescriptor dashboardWcdfDescriptor = Mockito.mock( DashboardWcdfDescriptor.class );
    when( dashboardWcdfDescriptor.isRequire() ).thenReturn( true );
    final Dashboard dash = Mockito.mock( Dashboard.class );
    when( dash.getWcdf() ).thenReturn( dashboardWcdfDescriptor );
    final IThingWriterFactory factory = Mockito.mock( CdfRunJsThingWriterFactory.class );
    final CdfRunJsDashboardWriteOptions options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );

    assertThat(
      cdeEnvironment.getCdfRunJsDashboardWriteContext( factory, "", false, dash, options ),
      instanceOf( pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContext.class ) );
  }

  @Test
  public void testGetCdfRunJsDashboardWriteContextLegacy() {
    final DashboardWcdfDescriptor dashboardWcdfDescriptor = Mockito.mock( DashboardWcdfDescriptor.class );
    when( dashboardWcdfDescriptor.isRequire() ).thenReturn( false );
    final Dashboard dash = Mockito.mock( Dashboard.class );
    when( dash.getWcdf() ).thenReturn( dashboardWcdfDescriptor );
    final IThingWriterFactory factory = Mockito.mock( CdfRunJsThingWriterFactory.class );
    final CdfRunJsDashboardWriteOptions options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );

    assertThat(
      cdeEnvironment.getCdfRunJsDashboardWriteContext( factory, "", false, dash, options ),
      instanceOf( pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext.class ) );
  }

  @Test
  public void testGetCdeXml() {
    final IBasicFile file = Mockito.mock( IBasicFile.class );
    final IUserContentAccess userContentAccess = Mockito.mock( IUserContentAccess.class );
    when( userContentAccess.fileExists( anyString() ) ).thenReturn( true );
    when( userContentAccess.fetchFile( anyString() ) ).thenReturn( file );
    final IContentAccessFactory contentAccessFactory = Mockito.mock( IContentAccessFactory.class );
    when( contentAccessFactory.getUserContentAccess( anyString()) ).thenReturn( userContentAccess );
    cdeEnvironment.setContentAccessFactory( contentAccessFactory );
    assertEquals( file, cdeEnvironment.getCdeXml() );

    when( userContentAccess.fileExists( anyString() ) ).thenReturn( false );
    final IReadAccess readAccess = Mockito.mock( IReadAccess.class );
    when( readAccess.fileExists( anyString() ) ).thenReturn( true );
    when( readAccess.fetchFile( anyString() ) ).thenReturn( file );
    when( contentAccessFactory.getPluginSystemReader( anyObject() ) ).thenReturn( readAccess );
    assertEquals( file, cdeEnvironment.getCdeXml() );

    when( readAccess.fileExists( anyString() ) ).thenReturn( false );
    assertNull( cdeEnvironment.getCdeXml() );
  }

  @Test
  public void testGetCdeXmlViaUserContentAccess() {
    final IBasicFile file = Mockito.mock( IBasicFile.class );
    final IUserContentAccess userContentAccess = Mockito.mock( IUserContentAccess.class );
    when( userContentAccess.fileExists( anyString() ) ).thenReturn( true );
    when( userContentAccess.fetchFile( anyString() ) ).thenReturn( file );
    final IContentAccessFactory contentAccessFactory = Mockito.mock( IContentAccessFactory.class );
    when( contentAccessFactory.getUserContentAccess( anyString()) ).thenReturn( userContentAccess );
    cdeEnvironment.setContentAccessFactory( contentAccessFactory );

    assertEquals( file, cdeEnvironment.getCdeXml() );
  }

  @Test
  public void testGetCdeXmlViaReadAccess() {
    final IBasicFile file = Mockito.mock( IBasicFile.class );
    final IUserContentAccess userContentAccess = Mockito.mock( IUserContentAccess.class );
    when( userContentAccess.fileExists( anyString() ) ).thenReturn( false );
    final IReadAccess readAccess = Mockito.mock( IReadAccess.class );
    when( readAccess.fileExists( anyString() ) ).thenReturn( true );
    when( readAccess.fetchFile( anyString() ) ).thenReturn( file );
    final IContentAccessFactory contentAccessFactory = Mockito.mock( IContentAccessFactory.class );
    when( contentAccessFactory.getUserContentAccess( anyString()) ).thenReturn( userContentAccess );
    when( contentAccessFactory.getPluginSystemReader( anyObject() ) ).thenReturn( readAccess );
    cdeEnvironment.setContentAccessFactory( contentAccessFactory );

    assertEquals( file, cdeEnvironment.getCdeXml() );
  }

  @Test
  public void testGetCdeXmlNoFileFound() {
    final IUserContentAccess userContentAccess = Mockito.mock( IUserContentAccess.class );
    when( userContentAccess.fileExists( anyString() ) ).thenReturn( false );
    final IReadAccess readAccess = Mockito.mock( IReadAccess.class );
    when( readAccess.fileExists( anyString() ) ).thenReturn( false );
    final IContentAccessFactory contentAccessFactory = Mockito.mock( IContentAccessFactory.class );
    when( contentAccessFactory.getUserContentAccess( anyString()) ).thenReturn( userContentAccess );
    when( contentAccessFactory.getPluginSystemReader( anyObject() ) ).thenReturn( readAccess );
    cdeEnvironment.setContentAccessFactory( contentAccessFactory );

    assertNull( cdeEnvironment.getCdeXml() );
  }

  @Test
  public void testCanCreateContent() {
    assertEquals( false, cdeEnvironment.canCreateContent() );
  }

  @Test
  public void testSetContentAccessFactory() {
    IContentAccessFactory expected = Mockito.mock( IContentAccessFactory.class );
    cdeEnvironment.setContentAccessFactory( expected );
    assertEquals( expected, cdeEnvironment.getContentAccessFactory() );
  }
}
