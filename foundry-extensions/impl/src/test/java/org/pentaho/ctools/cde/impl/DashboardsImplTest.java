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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import pt.webdetails.cdf.dd.reader.factory.SolutionResourceLoader;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.rca.RemoteBasicFile;
import pt.webdetails.cpf.repository.rca.RemoteReadAccess;

import java.util.ArrayList;
import java.util.List;

import org.skyscreamer.jsonassert.JSONAssert;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DashboardsImplTest {

  private DashboardsImpl dashboardImpl = null;
  private RemoteReadAccess readAccessMockitoForTwoDashboards = null;
  private List<IBasicFile> fileListTwoDashboards = null;
  private String[] cdeDashboardsExtension = null;
  private JSONArray expectedResult = null;

  @Before
  public void setUp() {
    SolutionResourceLoader resourceLoaderMockito = Mockito.mock( SolutionResourceLoader.class );
    dashboardImpl = new DashboardsImpl( resourceLoaderMockito );

    RemoteBasicFile file1 = Mockito.mock( RemoteBasicFile.class );
    when( file1.getName() ).thenReturn( "CDE Test Dashboard" );
    when( file1.getFullPath() ).thenReturn( "/home/CDE Test Dashboard.wcdf" );
    when( file1.getExtension() ).thenReturn( "wcdf" );

    RemoteBasicFile file2 = Mockito.mock( RemoteBasicFile.class );
    when( file2.getName() ).thenReturn( "sample" );
    when( file2.getFullPath() ).thenReturn( "/home/sample CDE/sample.wcdf" );
    when( file2.getExtension() ).thenReturn( "wcdf" );

    fileListTwoDashboards = new ArrayList<>();
    fileListTwoDashboards.add( file1 );
    fileListTwoDashboards.add( file2 );

    readAccessMockitoForTwoDashboards = Mockito.mock( RemoteReadAccess.class );
    when( readAccessMockitoForTwoDashboards.listFiles( anyString(), any( GenericBasicFileFilter.class ),
      anyInt(), anyBoolean(), anyBoolean() ) )
      .thenReturn( fileListTwoDashboards );

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

    cdeDashboardsExtension = new String[] { "wcdf" };
  }

  @After
  public void tearDown() {
    dashboardImpl = null;
    readAccessMockitoForTwoDashboards = null;
    fileListTwoDashboards = null;
    cdeDashboardsExtension = null;
    expectedResult = null;
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
