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

package org.pentaho.ctools.cde.api;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class RenderApiTest {
  private final String DEFAULT_SCHEME = "http";
  private final String DASHBOARD_FILE_NAME = "dummyRequire.wcdf";
  private final String DASHBOARD_WCDF_PATH = "src/test/resources/dashboard/" + DASHBOARD_FILE_NAME;

  CdfRunJsDashboardWriteOptions cdfRunJsDashboardWriteOptions;
  private CdfRunJsDashboardWriteResult cdfRunJsDashboardWriteResult;
  private DashboardManager dashboardManager;
  private RenderApi renderApi;

  private String path;
  private boolean inferScheme;
  private String root;
  private boolean absolute;
  private boolean bypassCache;
  private boolean debug;
  private String style;
  private String alias;
  private HttpServletRequest request;
  boolean all;

  @Before
  public void setUp() {
    cdfRunJsDashboardWriteOptions = Mockito.mock( CdfRunJsDashboardWriteOptions.class );
    cdfRunJsDashboardWriteResult = new CdfRunJsDashboardWriteResult.Builder().build();
    dashboardManager = Mockito.mock( DashboardManager.class );
    renderApi = new RenderApi();
    renderApi.setDashboardManager( dashboardManager );

    path = StringUtils.EMPTY;
    inferScheme = false;
    root = StringUtils.EMPTY;
    absolute = true;
    bypassCache = false;
    debug = false;
    style = StringUtils.EMPTY;
    alias = StringUtils.EMPTY;

    request = Mockito.mock( HttpServletRequest.class );
    when( request.getScheme() ).thenReturn( DEFAULT_SCHEME );
  }

  @After
  public void tearDown() {
    cdfRunJsDashboardWriteOptions = null;
    cdfRunJsDashboardWriteResult = null;
    dashboardManager = null;
    renderApi = null;
    path = null;
    root = null;
    style = null;
    alias = null;
    request = null;
  }

  @Test
  public void testGetDashboard() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( anyString(), anyObject(), anyBoolean(), anyString() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    assertEquals( StringUtils.EMPTY, renderApi.getDashboard(
      DASHBOARD_FILE_NAME, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardEmptyPath() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( anyString(), anyObject(), anyBoolean(), anyString() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    assertEquals( "No path provided.", renderApi.getDashboard(
      StringUtils.EMPTY, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardCustomAlias() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( anyString(), anyObject(), anyBoolean(), anyString() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    alias = "alias";
    assertEquals( StringUtils.EMPTY, renderApi.getDashboard(
      DASHBOARD_FILE_NAME, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardErrorMessage() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( anyString(), anyObject(), anyBoolean(), anyString() ) ).thenThrow(
      Exception.class );

    try {
      assertTrue( renderApi.getDashboard(
        DASHBOARD_WCDF_PATH, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request  )
          .startsWith( "Could not load dashboard: " ) );
    } catch( Exception e ) {
      fail( "No exception should be thrown" );
    }
  }

  @Test
  public void testGetDashboardParameters() throws Exception {
    when( dashboardManager.getDashboardParameters( anyString(), anyBoolean(), anyBoolean() ) ).thenReturn(
      StringUtils.EMPTY );

    assertEquals( StringUtils.EMPTY, renderApi.getDashboardParameters( DASHBOARD_WCDF_PATH, bypassCache, all ) );
  }

  @Test
  public void testGetDashboardParametersEmptyPath() throws Exception {
    when( dashboardManager.getDashboardParameters( anyString(), anyBoolean(), anyBoolean() ) ).thenReturn(
      StringUtils.EMPTY );

    assertEquals( "No path provided.", renderApi.getDashboardParameters( path, bypassCache, all ) );
  }

  @Test
  public void testGetDashboardParametersErrorMessage() throws Exception {
    //test error message
    when( dashboardManager.getDashboardParameters( anyString(), anyBoolean(), anyBoolean() ) ).thenThrow( Exception.class );

    try {
      assertTrue( renderApi.getDashboardParameters( DASHBOARD_WCDF_PATH, bypassCache, all )
        .startsWith( "Could not load dashboard parameters: " ) );
    } catch( Exception e ) {
      fail( "No exception should be thrown" );
    }
  }
}
