/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.CustomComponent;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsGenericComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd.CdeRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.render.RenderResources;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsDashboardWriterTest extends TestCase {

  private static final String CONTEXT_CONFIGURATION = "{" + NEWLINE
    + INDENT1 + "context: {" + NEWLINE
    + INDENT2 + "fakeContext: fakeContext" + NEWLINE
    + INDENT1 + "}" + NEWLINE
    + INDENT1 + "storage: {" + NEWLINE
    + INDENT2 + "fakeStorage: fakeStorage" + NEWLINE
    + INDENT1 + "}" + NEWLINE
    + INDENT1 + "view: {" + NEWLINE
    + INDENT2 + "fakeView: fakeView" + NEWLINE
    + INDENT1 + "}" + NEWLINE;

  private static CdfRunJsDashboardWriter dashboardWriter;
  private static CdfRunJsDashboardWriter dashboardWriterSpy;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDashboardWriteOptions options;

  @Before
  public void setUp() throws Exception {
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
    dashboardWriterSpy = spy( dashboardWriter );
    context = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );
  }

  @After
  public void tearDown() throws Exception {
    dashboardWriter = null;
    context = null;
    options = null;
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
        "Logger",
        "$",
        "_",
        "moment",
        "cdo",
        "Utils",
        "TestComponent1",
        "TestComponent2",
        "TestComponent3" );
    List<String> cdfRequirePaths = Arrays.asList(
        "cdf/Dashboard.Blueprint",
        "cdf/Logger",
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/lib/moment",
        "cdf/lib/CCC/cdo",
        "cdf/dashboard/Utils",
        "cdf/components/TestComponent1",
        "cdf/components/TestComponent2",
        "cdf/components/TestComponent3" );

    String out = dashboardWriter.wrapRequireDefinitions( "fakeContent", CONTEXT_CONFIGURATION );

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_DECLARATION, CONTEXT_CONFIGURATION  ) ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    Assert.assertEquals( out, dashboardResult.toString() );

    // test with additional requireJS configurations

    testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    dashboardWriter.addRequireJsResource( "cde/resources/TestResource1", "/TestResourcePath1" );

    out = dashboardWriter.wrapRequireDefinitions( "fakeContent", CONTEXT_CONFIGURATION );

    dashboardResult = new StringBuilder();

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "cdo",
      "Utils",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3",
      "TestResource1" );
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/lib/CCC/cdo",
      "cdf/dashboard/Utils",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3",
      "cde/resources/TestResource1" );

    dashboardResult
      .append( "requireCfg['paths']['cde/resources/TestResource1'] =" )
      .append( " CONTEXT_PATH + 'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" ).append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_DECLARATION, CONTEXT_CONFIGURATION  ) ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

    // test with additional JS and CSS external file using amd! and css! requireJS loader plugins

    testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    dashboardWriter.addRequireCssResource( "css!cde/resources/TestResourceCSS", "/TestResourceCSSPath" );

    out = dashboardWriter.wrapRequireDefinitions( "fakeContent", CONTEXT_CONFIGURATION );

    dashboardResult.setLength( 0 );

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "cdo",
      "Utils",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3",
      "TestResource1" );
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/lib/CCC/cdo",
      "cdf/dashboard/Utils",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3",
      "cde/resources/TestResource1",
      "css!cde/resources/TestResourceCSS" );

    dashboardResult
      .append(
        "requireCfg['paths']['cde/resources/TestResource1'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" ).append(
      NEWLINE )
      // plugin css! should have been stripped from module id
      .append(
        "requireCfg['paths']['cde/resources/TestResourceCSS'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourceCSSPath';" )
      .append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_DECLARATION, CONTEXT_CONFIGURATION  ) ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

  }

  @Test
  public void testDashboardType() {
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Blueprint" );
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Bootstrap" );
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.MOBILE, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Mobile" );
  }

  @Test
  public void testGetFileResourcesRequirePaths() {
    dashboardWriter.addRequireJsResource( "myResource1", "/myResource1.js" );
    dashboardWriter.addRequireJsResource( "myResource2", "/myResource2.js" );
    dashboardWriter.addRequireJsResource( "myResource3", "/myResource3.js" );

    String fileResources = dashboardWriter.getFileResourcesRequirePaths();

    assertEquals( fileResources,
        "requireCfg['paths']['cde/resources/myResource1'] = CONTEXT_PATH + "
        + "'plugin/pentaho-cdf-dd/api/resources/myResource1.js';\n"
        + "requireCfg['paths']['cde/resources/myResource2'] = CONTEXT_PATH + "
        + "'plugin/pentaho-cdf-dd/api/resources/myResource2.js';\n"
        + "requireCfg['paths']['cde/resources/myResource3'] = CONTEXT_PATH + "
        + "'plugin/pentaho-cdf-dd/api/resources/myResource3.js';\n"
        + REQUIRE_CONFIG + NEWLINE );
  }

  @Test
  public void testWriteResources() throws Exception {
    doReturn( "" ).when( options ).getAliasPrefix();
    doReturn( options ).when( context ).getOptions();
    Dashboard dash = mock( Dashboard.class );
    doReturn( 1 ).when( dash ).getLayoutCount();

    LayoutComponent layoutComponent = mock( LayoutComponent.class );
    doReturn( layoutComponent ).when( dash ).getLayout( "TODO" );

    JXPathContext jxPathContext = mock( JXPathContext.class );
    doReturn( jxPathContext ).when( layoutComponent ).getLayoutXPContext();

    List<ResourceMap.Resource> cssResources = new ArrayList<ResourceMap.Resource>();

    ResourceMap.Resource cssResource1 = mock( ResourceMap.Resource.class ),
        cssResource2 = mock( ResourceMap.Resource.class );
    doReturn( ResourceMap.ResourceType.FILE ).when( cssResource1 ).getResourceType();
    doReturn( "/fakePath" ).when( cssResource1 ).getResourcePath();
    doReturn( ResourceMap.ResourceType.CODE ).when( cssResource2 ).getResourceType();

    doReturn( "" ).when( cssResource1 ).getProcessedResource();
    doReturn( "" ).when( cssResource2 ).getProcessedResource();
    cssResources.add( cssResource1 );
    cssResources.add( cssResource2 );

    List<ResourceMap.Resource> jsResources = new ArrayList<ResourceMap.Resource>();
    ResourceMap.Resource jsResource1 = mock( ResourceMap.Resource.class ),
        jsResource2 = mock( ResourceMap.Resource.class );

    doReturn( ResourceMap.ResourceType.FILE ).when( jsResource1 ).getResourceType();
    doReturn( "/fakePath" ).when( jsResource1 ).getResourcePath();
    doReturn( ResourceMap.ResourceType.CODE ).when( jsResource2 ).getResourceType();
    doReturn( "(function(){ return true; })" ).when( jsResource2 ).getProcessedResource();

    jsResources.add( jsResource1 );
    jsResources.add( jsResource2 );

    ResourceMap resourceMap = mock( ResourceMap.class );
    doReturn( cssResources ).when( resourceMap ).getCssResources();
    doReturn( jsResources ).when( resourceMap ).getJavascriptResources();

    RenderResources resourceRender = mock( RenderResources.class );
    doReturn( resourceMap ).when( resourceRender ).renderResources( anyString() );
    doReturn( resourceRender ).when( dashboardWriterSpy ).getResourceRenderer( jxPathContext, context );

    dashboardWriterSpy.writeResources( context, dash );
    verify( cssResource2, times( 1 ) ).getProcessedResource();
    verify( dashboardWriterSpy, times( 1 ) ).addRequireCssResource( anyString(), anyString() );
    verify( dashboardWriterSpy, times( 1 ) ).addRequireJsResource( anyString(), anyString() );
    verify( dashboardWriterSpy, times( 1 ) ).addJsCodeSnippet( anyString() );
    verify( jsResource1, times( 1 ) ).getResourceType();
    verify( jsResource1, times( 2 ) ).getResourcePath();
    verify( jsResource2, times( 1 ) ).getProcessedResource();
  }

  @Test
  public void testGetComponentPath() {

    Component primitiveComp = mock( CustomComponent.class );
    Component customComp1 = mock( CustomComponent.class );
    Component customComp2 = mock( CustomComponent.class );
    Component customComp3 = mock( CustomComponent.class );

    doReturn( true ).when( dashboardWriterSpy ).isPrimitiveComponent( primitiveComp );
    doReturn( true ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( primitiveComp );
    doReturn( false ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( primitiveComp );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( primitiveComp );
    Assert.assertEquals(
      CDF_AMD_BASE_COMPONENT_PATH + "FakePrimitiveComponent",
        dashboardWriterSpy.getComponentPath( primitiveComp, "FakePrimitiveComponent" ) );

    doReturn( true ).when( dashboardWriterSpy ).isCustomComponent( customComp1 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( customComp1 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( customComp1 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( customComp1 );
    Assert.assertEquals(
        CDE_AMD_BASE_COMPONENT_PATH + "Custom1Component",
        dashboardWriterSpy.getComponentPath( customComp1, "Custom1Component" ) );

    // AMD only with implementation path, custom component uploaded to the repository
    doReturn( true ).when( dashboardWriterSpy ).isCustomComponent( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( customComp2 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( customComp2 );
    doReturn( "fakePath/Custom2Component.js" ).when( dashboardWriterSpy ).getComponentImplementationPath( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).supportsLegacy( customComp2 );
    Assert.assertEquals(
        CDE_AMD_REPO_COMPONENT_PATH + "fakePath/Custom2Component",
        dashboardWriterSpy.getComponentPath( customComp2, "Custom2Component" ) );

    // AMD only without implementation path, custom component uploaded to the repository
    doReturn( true ).when( dashboardWriterSpy ).isCustomComponent( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( customComp2 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( customComp2 );
    doReturn( "" ).when( dashboardWriterSpy ).getComponentImplementationPath( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).supportsLegacy( customComp2 );
    doReturn( "fakePath/component.xml" ).when( dashboardWriterSpy ).getComponentSourcePath( customComp2 );
    Assert.assertEquals(
        CDE_AMD_REPO_COMPONENT_PATH + "fakePath/Custom2Component",
        dashboardWriterSpy.getComponentPath( customComp2, "Custom2Component" ) );

    // AMD and legacy with implementation path, custom component uploaded to the repository
    doReturn( true ).when( dashboardWriterSpy ).isCustomComponent( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( customComp2 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( customComp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( customComp2 );
    doReturn( "fakeIgnoredPath/Custom2Component.js" ).when( dashboardWriterSpy ).getComponentImplementationPath(
        customComp2 );
    doReturn( true ).when( dashboardWriterSpy ).supportsLegacy( customComp2 );
    doReturn( "fakePath/component.xml" ).when( dashboardWriterSpy ).getComponentSourcePath( customComp2 );
    Assert.assertEquals(
        CDE_AMD_REPO_COMPONENT_PATH + "fakePath/Custom2Component",
        dashboardWriterSpy.getComponentPath( customComp2, "Custom2Component" ) );

    doReturn( true ).when( dashboardWriterSpy ).isCustomComponent( customComp3 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( customComp3 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( customComp3 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( customComp3 );
    doReturn( "sparkl" ).when( dashboardWriterSpy ).getPluginIdFromOrigin( customComp3 );
    Assert.assertEquals(
        "sparkl" + PLUGIN_COMPONENT_FOLDER + "Custom3Component",
        dashboardWriterSpy.getComponentPath( customComp3, "Custom3Component" ) );
  }

  @Test
  public void testWriteComponents() throws ValidationException, UnsupportedThingException, ThingWriteException,
    JSONException {

    when( options.isAmdModule() ).thenReturn( false );
    when( context.getOptions() ).thenReturn( options );

    Dashboard dash = mock( Dashboard.class );
    DashboardWcdfDescriptor dashboardWcdfDescriptor = mock( DashboardWcdfDescriptor.class );
    CdeRunJsThingWriterFactory factory = mock( CdeRunJsThingWriterFactory.class );
    IThingWriter writer = mock( CdfRunJsGenericComponentWriter.class );

    doReturn( factory ).when( context ).getFactory();
    doReturn( writer ).when( factory ).getWriter( any( Thing.class ) );

    doReturn( JSONObject.fromObject( "{}" ) ).when( dashboardWcdfDescriptor ).toJSON();
    doReturn( dashboardWcdfDescriptor ).when( dash ).getWcdf();

    List<Component> componentList = new ArrayList<Component>();

    Component comp1 = mock( CustomComponent.class );
    Component comp2 = mock( CustomComponent.class );
    Component invalidComp = mock( CustomComponent.class );
    Component comp3 = mock( CustomComponent.class );

    componentList.add( comp1 );
    doReturn( "comp1" ).when( comp1 ).getId();
    doReturn( "comp1" ).when( dashboardWriterSpy ).getComponentName( comp1 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( comp1 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( comp1 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( comp1 );
    doReturn( "Fake1Component" ).when( dashboardWriterSpy ).getComponentClassName( comp1 );
    doReturn( "fake1/path" ).when( dashboardWriterSpy ).getComponentPath( comp1, "Fake1Component" );

    componentList.add( comp2 );
    doReturn( "comp2" ).when( comp2 ).getId();
    doReturn( "comp2" ).when( dashboardWriterSpy ).getComponentName( comp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( comp2 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( comp2 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( comp2 );
    doReturn( "Fake2Component" ).when( dashboardWriterSpy ).getComponentClassName( comp2 );
    doReturn( "fake2/path" ).when( dashboardWriterSpy ).getComponentPath( comp2, "Fake2Component" );

    componentList.add( invalidComp );
    doReturn( "invalidComponent" ).when( invalidComp ).getId();
    doReturn( "invalidComponent" ).when( dashboardWriterSpy ).getComponentName( invalidComp );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( invalidComp );
    doReturn( true ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( invalidComp );
    doReturn( false ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( invalidComp );
    doReturn( "InvalidComponent" ).when( dashboardWriterSpy ).getComponentClassName( invalidComp );
    // custom components in the repository must contain an implementation path or will be ignored
    // it is needed for AMD path configuration purposes
    doReturn( "" ).when( dashboardWriterSpy ).getComponentPath( invalidComp, "InvalidComponent" );

    componentList.add( comp3 );
    doReturn( "comp3" ).when( comp3 ).getId();
    doReturn( "comp3" ).when( dashboardWriterSpy ).getComponentName( comp3 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentStaticSystemOrigin( comp3 );
    doReturn( false ).when( dashboardWriterSpy ).isComponentPluginRepositoryOrigin( comp3 );
    doReturn( true ).when( dashboardWriterSpy ).isComponentOtherPluginStaticSystemOrigin( comp3 );
    doReturn( "sparkl" ).when( dashboardWriterSpy ).getPluginIdFromOrigin( comp3 );
    doReturn( "Fake3Component" ).when( dashboardWriterSpy ).getComponentClassName( comp3 );
    doReturn( "sparkl/fake3/component" ).when( dashboardWriterSpy ).getComponentPath( comp3, "Fake3Component" );

    doReturn( componentList ).when( dash ).getRegulars();

    Assert.assertEquals(
        "var wcdfSettings = {};" + NEWLINE + NEWLINE + NEWLINE
        + "dashboard.addComponents([comp1, comp2, comp3]);" + NEWLINE,
        dashboardWriterSpy.writeComponents( context, dash ) );

    Assert.assertEquals( 3, dashboardWriter.getComponentList().size() );

  }

  @Test
  public void testWrapRequireModuleDefinitions() {

    Map testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    List<String> componentClassNames = Arrays.asList(
        "Dashboard",
        "Logger",
        "$",
        "_",
        "moment",
        "cdo",
        "Utils",
        "TestComponent1",
        "TestComponent2",
        "TestComponent3" );
    List<String> cdfRequirePaths = Arrays.asList(
        "cdf/Dashboard.Blueprint",
        "cdf/Logger",
        "cdf/lib/jquery",
        "amd!cdf/lib/underscore",
        "cdf/lib/moment",
        "cdf/lib/CCC/cdo",
        "cdf/dashboard/Utils",
        "cdf/components/TestComponent1",
        "cdf/components/TestComponent2",
        "cdf/components/TestComponent3" );

    String out = dashboardWriter.wrapRequireModuleDefinitions( "fakeContent", "fakeLayout", false, CONTEXT_CONFIGURATION );

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( "" )
      .append( MessageFormat.format(DASHBOARD_MODULE_START, CONTEXT_CONFIGURATION) )
      .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT, "fakeLayout" ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, "fakeContent" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

    // test with additional requireJS configurations for JS external resource

    testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    dashboardWriter.addRequireJsResource( "cde/resources/TestResource1", "/TestResourcePath1" );

    out = dashboardWriter.wrapRequireModuleDefinitions( "fakeContent", "fakeLayout", false, CONTEXT_CONFIGURATION );

    dashboardResult.setLength( 0 );

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "cdo",
      "Utils",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3",
      "TestResource1" );
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/lib/CCC/cdo",
      "cdf/dashboard/Utils",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3",
      "cde/resources/TestResource1" );

    dashboardResult
      .append(
        "requireCfg['paths']['cde/resources/TestResource1'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" )
      .append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( "" )
      .append( MessageFormat.format(DASHBOARD_MODULE_START, CONTEXT_CONFIGURATION) )
      .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT, "fakeLayout" ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, "fakeContent" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

    // test with empty alias

    out = dashboardWriter.wrapRequireModuleDefinitions( "fakeContent", "fakeLayout", true, CONTEXT_CONFIGURATION );

    dashboardResult.setLength( 0 );

    dashboardResult
      .append(
        "requireCfg['paths']['cde/resources/TestResource1'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" )
      .append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( "" )
      .append( MessageFormat.format( DASHBOARD_MODULE_START_EMPTY_ALIAS,CONTEXT_CONFIGURATION, "fakeLayout" ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, "fakeContent" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

    // test with additional JS snippet

    StringBuffer jsCodeSnippet = new StringBuffer( "(function(){return;})()" );

    doReturn( jsCodeSnippet ).when( dashboardWriterSpy ).getJsCodeSnippets();

    out = dashboardWriterSpy.wrapRequireModuleDefinitions( "fakeContent", "fakeLayout", false, CONTEXT_CONFIGURATION );

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "cdo",
      "Utils",
      "TestResource1" );
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/lib/CCC/cdo",
      "cdf/dashboard/Utils",
      "cde/resources/TestResource1" );

    dashboardResult.setLength( 0 );

    dashboardResult
      .append(
        "requireCfg['paths']['cde/resources/TestResource1'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" )
      .append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( MessageFormat.format(DASHBOARD_MODULE_START, CONTEXT_CONFIGURATION) )
      .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT, "fakeLayout" ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS,
        jsCodeSnippet.toString() + NEWLINE + "fakeContent" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

    // test with additional JS and CSS external file using amd! and css! requireJS loader plugins

    testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    dashboardWriter.addRequireCssResource( "css!cde/resources/TestResourceCSS", "/TestResourceCSSPath" );

    //testing cases which may use a full url as a resource
    dashboardWriter
      .addRequireCssResource( "css!cde/resources/TestResourceCSSFullUrl", "http://TestResourceCSSFullUrl" );
    dashboardWriter.addRequireJsResource( "cde/resources/TestResourceJSFullUrl", "http://TestResourceJSFullUrl" );

    out = dashboardWriter.wrapRequireModuleDefinitions( "fakeContent", "fakeLayout", false, CONTEXT_CONFIGURATION );

    dashboardResult.setLength( 0 );

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "cdo",
      "Utils",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3",
      "TestResource1",
      "TestResourceJSFullUrl" );
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/lib/CCC/cdo",
      "cdf/dashboard/Utils",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3",
      "cde/resources/TestResource1",
      "cde/resources/TestResourceJSFullUrl",
      "css!cde/resources/TestResourceCSS",
      "css!cde/resources/TestResourceCSSFullUrl" );

    dashboardResult
      .append(
        "requireCfg['paths']['cde/resources/TestResource1'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourcePath1';" )
      .append( NEWLINE )
      .append( "requireCfg['paths']['cde/resources/TestResourceJSFullUrl'] = 'http://TestResourceJSFullUrl'" ).append(
      NEWLINE )
      // plugin css! should have been stripped from module id
      .append(
        "requireCfg['paths']['cde/resources/TestResourceCSS'] = CONTEXT_PATH + "
          + "'plugin/pentaho-cdf-dd/api/resources/TestResourceCSSPath';" ).append( NEWLINE )
      .append(
        "requireCfg['paths']['cde/resources/TestResourceCSSFullUrl'] = 'http://TestResourceCSSFullUrl'" )
      .append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      // Output module paths and module class names
      .append( MessageFormat.format( DEFINE_START,
        StringUtils.join( cdfRequirePaths, "', '" ),
        StringUtils.join( componentClassNames, ", " ) ) )
      .append( "" )
      .append( MessageFormat.format( DASHBOARD_MODULE_START, CONTEXT_CONFIGURATION ) )
      .append( MessageFormat.format( DASHBOARD_MODULE_LAYOUT, "fakeLayout" ) )
      .append( DASHBOARD_MODULE_RENDERER ).append( NEWLINE )
      .append( DASHBOARD_MODULE_SETUP_DOM ).append( NEWLINE )
      .append( MessageFormat.format( DASHBOARD_MODULE_PROCESS_COMPONENTS, "fakeContent" ) )
      .append( DASHBOARD_MODULE_STOP ).append( NEWLINE )
      .append( DEFINE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );
  }
}
