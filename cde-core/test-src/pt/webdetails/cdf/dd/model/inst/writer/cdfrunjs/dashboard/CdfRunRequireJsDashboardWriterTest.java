/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.render.RenderResources;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.render.layout.ResourceRender;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CdfRunRequireJsDashboardWriterTest extends TestCase {

  private static final String NEWLINE = System.getProperty( "line.separator" );
  private static final String INDENT = "\t";

  private static final String EPILOGUE = "dashboard.init();" + NEWLINE + "return dashboard;";
  private static final String REQUIRE_START = "require(";
  private static final String REQUIRE_STOP = "});";
  private static final String CDF_AMD_BASE_COMPONENT_PATH = "cdf/components/";
  private static final String CDE_AMD_BASE_COMPONENT_PATH = "cde/components/";
  private static final String CDE_AMD_REPO_COMPONENT_PATH = "cde/repo/components/";
  private static final String PLUGIN_COMPONENT_FOLDER = "/components/";
  private static final String REQUIRE_CONFIG = "require.config(requireCfg);";

  private static CdfRunRequireJsDashboardWriter dashboardWriter;
  private static CdfRunRequireJsDashboardWriter dashboardWriterSpy;

  @Before
  public void setUp() throws Exception {
    dashboardWriter =
      new CdfRunRequireJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
    dashboardWriterSpy = spy( dashboardWriter );
  }

  @After
  public void tearDown() throws Exception {
    dashboardWriter = null;
  }

  @Test
  public void testWrapRequireDefinitions() {

    Map testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    List<String> componentClassNames = Arrays.asList(
      "Dashboard",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3" );
    List<String> cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3" );

    String out = dashboardWriter.wrapRequireDefinitions( "fakeContent" );

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      .append( REQUIRE_START )
      .append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," ).append( NEWLINE )
      .append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" ).append( NEWLINE )
      .append( "var dashboard = new Dashboard();" ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( EPILOGUE ).append( NEWLINE )
      .append( REQUIRE_STOP );

    Assert.assertEquals( out, dashboardResult.toString() );

  }
  
  @Test
  public void testDashboardType() {
    dashboardWriter =
      new CdfRunRequireJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
    assertEquals( dashboardWriter.getDashboardRequireModule(), "cdf/Dashboard.Blueprint");
    dashboardWriter =
      new CdfRunRequireJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP, false );
    assertEquals( dashboardWriter.getDashboardRequireModule(), "cdf/Dashboard.Bootstrap");
    dashboardWriter =
      new CdfRunRequireJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.MOBILE, false );
    assertEquals( dashboardWriter.getDashboardRequireModule(), "cdf/Dashboard.Mobile");
  }
  
  @Test
  public void testGetFileResourcesRequirePaths() {
    dashboardWriter.addRequireResource( "myResource1", "/myResource1.js" );
    dashboardWriter.addRequireResource( "myResource2", "/myResource2.js" );
    dashboardWriter.addRequireResource( "myResource3", "/myResource3.js" );
    
    String fileResources = dashboardWriter.getFileResourcesRequirePaths();
    
    assertEquals( fileResources, "requireCfg['paths']['myResource1'] = '/myResource1.js';\n"
        + "requireCfg['paths']['myResource2'] = '/myResource2.js';\n"
        + "requireCfg['paths']['myResource3'] = '/myResource3.js';\n"
        + REQUIRE_CONFIG + NEWLINE );
  }

  @Test
  public void testWriteResources() throws Exception {
    CdfRunJsDashboardWriteContext context = mock( CdfRunJsDashboardWriteContext.class );
    CdfRunJsDashboardWriteOptions options = mock( CdfRunJsDashboardWriteOptions.class );
    doReturn( "" ).when( options ).getAliasPrefix();
    doReturn( options ).when( context ).getOptions();
    Dashboard dash = mock( Dashboard.class );
    doReturn( 1 ).when( dash ).getLayoutCount();

    LayoutComponent layoutComponent = mock( LayoutComponent.class );
    doReturn( layoutComponent ).when( dash ).getLayout( "TODO" );

    JXPathContext jxPathContext = mock( JXPathContext.class );
    doReturn( jxPathContext ).when( layoutComponent ).getLayoutXPContext();

    List<ResourceMap.Resource> cssResources = new ArrayList<ResourceMap.Resource>();

    ResourceMap.Resource cssResource = mock( ResourceMap.Resource.class );
    doReturn( "" ).when( cssResource ).getProcessedResource();
    cssResources.add( cssResource );
    cssResources.add( cssResource );
    cssResources.add( cssResource );
    List<ResourceMap.Resource> jsResources = new ArrayList<ResourceMap.Resource>();
    ResourceMap.Resource jsResource1 = mock( ResourceMap.Resource.class ),
      jsResource2 = mock( ResourceMap.Resource.class );

    doReturn( ResourceMap.ResourceType.FILE ).when( jsResource1 ).getResourceType();
    doReturn( ResourceMap.ResourceType.CODE ).when( jsResource2 ).getResourceType();

    jsResources.add( jsResource1 );
    jsResources.add( jsResource2 );

    ResourceMap resourceMap = mock( ResourceMap.class );
    doReturn( cssResources ).when( resourceMap ).getCssResources();
    doReturn( jsResources ).when( resourceMap ).getJavascriptResources();

    RenderResources resourceRender = mock( RenderResources.class );
    doReturn( resourceMap ).when( resourceRender ).renderResources( anyString() );
    doReturn( resourceRender ).when( dashboardWriterSpy ).getResourceRenderer( jxPathContext, context );

    dashboardWriterSpy.writeResources( context, dash );
    verify( cssResource, times( 3 ) ).getProcessedResource();
    verify( dashboardWriterSpy, times( 1 ) ).addRequireResource( anyString(), anyString() );
    verify( dashboardWriterSpy, times( 1 ) ).addJsCodeSnippet( anyString() );
    verify( jsResource1, times( 1 ) ).getResourceType();
    verify( jsResource1, times( 1 ) ).getResourcePath();
    verify( jsResource2, times( 1 ) ).getProcessedResource();
  }

}
