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
package org.pentaho.ctools.cde.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.reader.factory.SolutionResourceLoader;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.rca.RemoteBasicFile;
import pt.webdetails.cpf.repository.rca.RemoteReadAccess;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DashboardsImplTest {

  private DashboardsImpl dashboardImpl = null;
  private RemoteReadAccess readAccessMockitoForTwoDashboards = null;
  private List<IBasicFile> fileListTwoDashboards = null;
  private String[] cdeDashboardsExtension = null;
  private JSONArray expectedResult = null;
  private JSONObject expectedResultByPath = null;

  @Before
  public void setUp() {
    SolutionResourceLoader resourceLoaderMockito = Mockito.mock( SolutionResourceLoader.class );
    dashboardImpl = new DashboardsImpl( resourceLoaderMockito );

    long createDate = 1514764800000L;
    LocalDateTime createDateObject = LocalDateTime.ofInstant( new Date( createDate ).toInstant(), ZoneId.systemDefault() );

    long lastModifiedDate = 1517443200000L;
    LocalDateTime lastModifiedDateObject =  LocalDateTime.ofInstant( new Date( lastModifiedDate ).toInstant(), ZoneId.systemDefault() );

    RemoteBasicFile file1 = Mockito.mock( RemoteBasicFile.class );
    when( file1.getName() ).thenReturn( "CDE Test Dashboard" );
    when( file1.getFullPath() ).thenReturn( "/home/CDE Test Dashboard.wcdf" );
    when( file1.getExtension() ).thenReturn( "wcdf" );

    RemoteBasicFile file2 = Mockito.mock( RemoteBasicFile.class );
    when( file2.getName() ).thenReturn( "sample" );
    when( file2.getFullPath() ).thenReturn( "/home/sample CDE/sample.wcdf" );
    when( file2.getExtension() ).thenReturn( "wcdf" );

    RemoteBasicFile fileSolo = Mockito.mock( RemoteBasicFile.class );
    when( fileSolo.getName() ).thenReturn( "sampleSolo" );
    when( fileSolo.getFullPath() ).thenReturn( "/home/sample CDE/sampleSolo.wcdf" );
    when( fileSolo.getExtension() ).thenReturn( "wcdf" );
    when( fileSolo.getTitle() ).thenReturn( "title" );
    when( fileSolo.getDescription() ).thenReturn( "description" );
    when( fileSolo.getCreatedDate() ).thenReturn( String.valueOf( createDate ) );
    when( fileSolo.getLastModifiedDate() ).thenReturn( String.valueOf( lastModifiedDate ) );

    fileListTwoDashboards = new ArrayList<>();
    fileListTwoDashboards.add( file1 );
    fileListTwoDashboards.add( file2 );

    readAccessMockitoForTwoDashboards = Mockito.mock( RemoteReadAccess.class );
    when( readAccessMockitoForTwoDashboards.listFiles( anyString(), any( GenericBasicFileFilter.class ),
      anyInt(), anyBoolean(), anyBoolean() ) )
      .thenReturn( fileListTwoDashboards );
    when( readAccessMockitoForTwoDashboards.fetchFile( "/home/sample CDE/sampleSolo.wcdf" ) ).thenReturn( fileSolo );

    try {
      expectedResult = new JSONArray( "[\n"
        + "  {\n"
        + "    \"name\": \"CDE Test Dashboard\",\n"
        + "    \"path\": \"/home/CDE Test Dashboard.wcdf\"\n"
        + "  },\n"
        + "  {\n"
        + "    \"name\": \"sample\",\n"
        + "    \"path\": \"/home/sample CDE/sample.wcdf\"\n"
        + "  }\n"
        + "]" );
    } catch ( JSONException jEx ) {
      fail( "No exception should be thrown: " + jEx.getMessage() );
    }
    try {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
      expectedResultByPath = new JSONObject( "{\n"
        + "  \"name\": \"sampleSolo\",\n"
        + "  \"path\": \"/home/sample CDE/sampleSolo.wcdf\",\n"
        + "  \"title\": \"title\",\n"
        + "  \"description\": \"description\",\n"
        + "  \"created\": \"" + dtf.format( createDateObject ) + "\",\n"
        + "  \"modified\": \"" + dtf.format( lastModifiedDateObject ) + "\"\n"
        + "}" );
    } catch ( JSONException jEx ) {
      fail( "No exception should be thrown: " + jEx.getMessage() );
    }

    cdeDashboardsExtension = new String[] { "wcdf" };
  }

  @After
  public void tearDown() {
    dashboardImpl = null;
    readAccessMockitoForTwoDashboards = null;
    fileListTwoDashboards = null;
    cdeDashboardsExtension = null;
    expectedResult = null;
    expectedResultByPath = null;
  }

  @Test
  public void testConstructor() {
    ICdeEnvironment mockedEnvironment = mock( ICdeEnvironment.class );
    when( mockedEnvironment.getContentAccessFactory() ).thenReturn( mock( IContentAccessFactory.class ) );
    CdeEngine.getInstance().setEnvironment( mockedEnvironment );
    assertNotNull( new DashboardsImpl() );
  }

  @Test
  public void testGetDashboardList() {
    try {
      //all files should get returned
      dashboardImpl.setReadAccess( readAccessMockitoForTwoDashboards );
      JSONAssert.assertEquals( expectedResult, dashboardImpl.getDashboardList( -1, false ), false );
    } catch ( JSONException jEx ) {
      fail( "No exception should be thrown: " + jEx.getMessage() );
    }
  }

  @Test
  public void testGetDashboardFromPath() {
    try {
      //file2 should be returned
      dashboardImpl.setReadAccess( readAccessMockitoForTwoDashboards );
      JSONAssert.assertEquals( expectedResultByPath, dashboardImpl.getDashboardFromPath( "/home/sample CDE/sampleSolo.wcdf" ), false );
    } catch ( JSONException jEx ) {
      fail( "No exception should be thrown: " + jEx.getMessage() );
    }
  }

  @Test
  public void testGetPath() {
    assertEquals( "", dashboardImpl.getPath() );
  }

  @Test
  public void testGetFilterForCDEDashboardFiles() {
    GenericBasicFileFilter filter = dashboardImpl.getFilterForCDEDashboardFiles();
    assertNull( filter.getFileName() );
    assertArrayEquals( cdeDashboardsExtension, filter.getFileExtensions() );
    assertTrue( filter.isAcceptDirectories() );
  }
}
