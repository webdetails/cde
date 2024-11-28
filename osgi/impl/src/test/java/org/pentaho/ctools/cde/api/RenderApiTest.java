/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.ctools.cde.api;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
    when( dashboardManager.getDashboardCdfRunJs( any(), any(), anyBoolean(), any() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    assertEquals( StringUtils.EMPTY, renderApi.getDashboard(
      DASHBOARD_FILE_NAME, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardEmptyPath() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( any(), any(), anyBoolean(), any() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    assertEquals( "No path provided.", renderApi.getDashboard(
      StringUtils.EMPTY, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardCustomAlias() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( any(), any(), anyBoolean(), any() ) ).thenReturn(
      cdfRunJsDashboardWriteResult );

    alias = "alias";
    assertEquals( StringUtils.EMPTY, renderApi.getDashboard(
      DASHBOARD_FILE_NAME, inferScheme, root, absolute, bypassCache, debug, DEFAULT_SCHEME, style, alias, request ) );
  }

  @Test
  public void testGetDashboardErrorMessage() throws ThingWriteException {
    when( dashboardManager.getDashboardCdfRunJs( any(), any(), anyBoolean(), any() ) ).thenThrow(
      ThingWriteException.class );

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
    when( dashboardManager.getDashboardParameters( any(), anyBoolean(), anyBoolean() ) ).thenReturn(
      StringUtils.EMPTY );

    assertEquals( StringUtils.EMPTY, renderApi.getDashboardParameters( DASHBOARD_WCDF_PATH, bypassCache, all ) );
  }

  @Test
  public void testGetDashboardParametersEmptyPath() throws Exception {
    when( dashboardManager.getDashboardParameters( any(), anyBoolean(), anyBoolean() ) ).thenReturn(
      StringUtils.EMPTY );

    assertEquals( "No path provided.", renderApi.getDashboardParameters( path, bypassCache, all ) );
  }

  @Test
  public void testGetDashboardParametersErrorMessage() throws Exception {
    //test error message
    when( dashboardManager.getDashboardParameters( any(), anyBoolean(), anyBoolean() ) ).thenThrow( ThingReadException.class );

    try {
      assertTrue( renderApi.getDashboardParameters( DASHBOARD_WCDF_PATH, bypassCache, all )
        .startsWith( "Could not load dashboard parameters: " ) );
    } catch( Exception e ) {
      fail( "No exception should be thrown" );
    }
  }
}
