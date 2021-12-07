/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.render.ResourceMap.ResourceKind;
import pt.webdetails.cdf.dd.render.ResourceMap.ResourceType;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_GET_MESSAGES_PATH;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_NORMALIZE_ALIAS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_PROCESS_COMPONENTS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_RENDERER;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_SETUP_DOM;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_START_EMPTY_ALIAS;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_MODULE_STOP;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DEFINE_START;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DEFINE_STOP;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public class CdfRunJsDashboardModuleWriterTest {

  private static final String MESSAGES_PATH = "/test/repos/:path:to:dash.wcdf/";

  private static CdfRunJsDashboardModuleWriter dashboardWriterSpy;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDashboardWriteOptions options;

  @Before
  public void setUp() throws Exception {
    dashboardWriterSpy =
      spy( new CdfRunJsDashboardModuleWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT ) );
    doReturn( MESSAGES_PATH ).when( dashboardWriterSpy ).getWcdfReposPath( anyString() );

    context = mock( CdfRunJsDashboardWriteContext.class );
    doReturn( mock( Dashboard.class ) ).when( context ).getDashboard();

    options = mock( CdfRunJsDashboardWriteOptions.class );
  }

  @After
  public void tearDown() throws Exception {
    dashboardWriterSpy = null;
    context = null;
    options = null;
  }

  @Test
  public void testWrapRequireModuleDefinitions() {
    final String layout = "fakeLayout";

    ResourceMap testResources = new ResourceMap();
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc1", "jsFileRsrcPath1", "jsFileRsrc1" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.CODE, "jsCodeRsrc1", "jsCodeRsrcrPath1", "jsCodeRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc1", "cssFileRsrcPath1", "cssFileRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.CODE, "cssCodeRsrc1", "cssCodeRsrcPath1", "cssCodeRsrc1" );

    doReturn( "jsFileRsrcPath1" ).when( context ).replaceTokensAndAlias( "jsFileRsrcPath1" );
    doReturn( "cssFileRsrcPath1" ).when( context ).replaceTokensAndAlias( "cssFileRsrcPath1" );

    Map<String, String> testComponentModules = new LinkedHashMap<>();
    testComponentModules.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentModules.put( "TestComponent2", "cdf/components/TestComponent2" );

    StringBuilder out = new StringBuilder();
    doReturn( testComponentModules ).when( dashboardWriterSpy )
      .writeFileResourcesRequireJSPathConfig( out, testResources, context );

    ArrayList<String> moduleIds = new ArrayList<>();
    ArrayList<String> moduleClassNames = new ArrayList<>();

    dashboardWriterSpy.addDefaultDashboardModules( moduleIds, moduleClassNames );
    moduleIds.add( "cdf/components/TestComponent1" );
    moduleIds.add( "cdf/components/TestComponent2" );
    moduleIds.add( "cde/resources/jsFileRsrcPath1" );
    moduleIds.add( "css!cde/resources/cssFileRsrcPath1" );
    moduleClassNames.add( "TestComponent1" );
    moduleClassNames.add( "TestComponent2" );
    moduleClassNames.add( "jsFileRsrc1" );

    doReturn( "@ALIAS@" ).when( options ).getAliasPrefix();
    doReturn( options ).when( context ).getOptions();

    final String content = "dashboard.addComponent(new TestComponent1({test: 1}));";

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( moduleIds, "', '" ),
        StringUtils.join( moduleClassNames, ", " ) ) )
      .append( MessageFormat.format( DASHBOARD_MODULE_START_EMPTY_ALIAS, layout ) )
      .append( MessageFormat.format( DASHBOARD_MODULE_NORMALIZE_ALIAS, "\" + this._alias + \"" ) )
      .append( MessageFormat.format( DASHBOARD_MODULE_GET_MESSAGES_PATH, MESSAGES_PATH ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, "jsCodeRsrc1" + NEWLINE + NEWLINE
        + "dashboard.addComponent(new TestComponent1({test: 1}));" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    assertEquals(
      dashboardResult.toString(),
      dashboardWriterSpy
        .wrapRequireModuleDefinitions( layout, testResources, testComponentModules, content, context ) );
  }

  @Test
  public void testWriteRequireJsExecutionFunction() {
    StringBuilder out = new StringBuilder();
    ArrayList<String> moduleIds = new ArrayList<String>();
    ArrayList<String> moduleClassNames = new ArrayList<String>();
    moduleIds.add( "cdf/components/TestComponent1" );
    moduleIds.add( "cde/resources/jsFileRsrc1" );
    moduleIds.add( "css!cde/resources/cssFileRsrc1" );
    moduleClassNames.add( "TestComponent1" );
    moduleClassNames.add( "jsFileRsrc1" );
    moduleClassNames.add( "" );

    dashboardWriterSpy.writeRequireJsExecutionFunction( out, moduleIds, moduleClassNames );

    assertEquals(
      MessageFormat.format( DEFINE_START,
        "cdf/components/TestComponent1', 'cde/resources/jsFileRsrc1', 'css!cde/resources/cssFileRsrc1",
        "TestComponent1, jsFileRsrc1" ),
      out.toString() );
  }

  @Test
  public void testReplaceCdfdeExtension() {
    String[] paths = new String[] { "/path/to/file.wcdf", ":path:to:file",
      ":path.cdfde:to.wcdf:file", ":path.cdfde/to.wcdf:file" };
    for ( int i = 0; i < paths.length; i++ ) {
      // everything that ends in .cdfde will now end in .wcdf
      assertEquals(
        dashboardWriterSpy.replaceCdfdeExtension( paths[ i ] + ".cdfde" ), paths[ i ] + ".wcdf" );
    }
    for ( int i = 0; i < paths.length; i++ ) {
      // if it doesn't end in .cdfde, it will just be returned the same
      assertEquals(
        dashboardWriterSpy.replaceCdfdeExtension( paths[ i ] ), paths[ i ] );
    }
  }
}
