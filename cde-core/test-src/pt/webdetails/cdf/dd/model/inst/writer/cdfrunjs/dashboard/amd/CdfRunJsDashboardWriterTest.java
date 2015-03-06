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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class CdfRunJsDashboardWriterTest extends TestCase {

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

  private static CdfRunJsDashboardWriter dashboardWriter;
  private static CdfRunJsDashboardWriter dashboardWriterSpy;

  @Before
  public void setUp() throws Exception {
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
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
      "Logger",
      "$",
      "_",
      "moment",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3" );
    List<String> cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3" );

    String out = dashboardWriter.wrapRequireDefinitions( "fakeContent" );

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      .append( REQUIRE_START )
      .append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," ).append( NEWLINE )
      .append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" ).append( NEWLINE )
      .append( "var dashboard = new Dashboard();" ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( EPILOGUE ).append( NEWLINE )
      .append( REQUIRE_STOP );

    Assert.assertEquals( out, dashboardResult.toString() );


    // test with additional require configurations

    testComponentList = new LinkedHashMap<String, String>();
    testComponentList.put( "TestComponent1", "cdf/components/TestComponent1" );
    testComponentList.put( "TestComponent2", "cdf/components/TestComponent2" );
    testComponentList.put( "TestComponent3", "cdf/components/TestComponent3" );

    dashboardWriter.setComponentList( testComponentList );

    dashboardWriter.addRequireResource( "TestResource1", "TestResourcePath1" );

    out = dashboardWriter.wrapRequireDefinitions( "fakeContent" );

    dashboardResult = new StringBuilder();

    componentClassNames = Arrays.asList(
      "Dashboard",
      "Logger",
      "$",
      "_",
      "moment",
      "TestComponent1",
      "TestComponent2",
      "TestComponent3",
      "TestResource1");
    cdfRequirePaths = Arrays.asList(
      "cdf/Dashboard.Blueprint",
      "cdf/Logger",
      "cdf/lib/jquery",
      "amd!cdf/lib/underscore",
      "cdf/lib/moment",
      "cdf/components/TestComponent1",
      "cdf/components/TestComponent2",
      "cdf/components/TestComponent3",
      "TestResource1");

    dashboardResult
      .append( "requireCfg['paths']['TestResource1'] = 'TestResourcePath1';" ).append( NEWLINE )
      .append( REQUIRE_CONFIG ).append( NEWLINE )
      .append( REQUIRE_START )
      .append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," ).append( NEWLINE )
      .append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" ).append( NEWLINE )
      .append( "var dashboard = new Dashboard();" ).append( NEWLINE )
      .append( "fakeContent" ).append( NEWLINE )
      .append( EPILOGUE ).append( NEWLINE )
      .append( REQUIRE_STOP );

    Assert.assertEquals( dashboardResult.toString(), out );

  }
  
  @Test
  public void testDashboardType() {
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Blueprint");
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Bootstrap");
    dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.MOBILE, false );
    assertEquals( dashboardWriter.getDashboardRequireModuleId(), "cdf/Dashboard.Mobile");
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

  @Test
  public void testGetComponentPath() {

    Component primitiveComp = mock( CustomComponent.class );
    Component customComp1 = mock( CustomComponent.class );
    Component customComp2 = mock( CustomComponent.class );
    Component invalidComp = mock( CustomComponent.class );
    Component customComp3 = mock( CustomComponent.class );

    doReturn( true ).when( dashboardWriterSpy).isPrimitiveComponent( primitiveComp );
    doReturn( true ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( primitiveComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( primitiveComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( primitiveComp );
    Assert.assertEquals(
      CDF_AMD_BASE_COMPONENT_PATH + "FakePrimitiveComponent",
      dashboardWriterSpy.getComponentPath( primitiveComp, "FakePrimitiveComponent" ));

    doReturn( true ).when( dashboardWriterSpy).isCustomComponent( customComp1 );
    doReturn( true ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( customComp1 );
    doReturn( false ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( customComp1 );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( customComp1 );
    Assert.assertEquals(
      CDE_AMD_BASE_COMPONENT_PATH + "Custom1Component",
      dashboardWriterSpy.getComponentPath( customComp1, "Custom1Component" ));

    doReturn( true ).when( dashboardWriterSpy).isCustomComponent( customComp2 );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( customComp2 );
    doReturn( true ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( customComp2 );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( customComp2 );
    doReturn( "fakePath/Custom2Component.js" ).when( dashboardWriterSpy).getComponentImplementationPath( customComp2 );
    Assert.assertEquals(
      CDE_AMD_REPO_COMPONENT_PATH + "fakePath/Custom2Component",
      dashboardWriterSpy.getComponentPath( customComp2, "Custom2Component" ));

    doReturn( true ).when( dashboardWriterSpy).isCustomComponent( invalidComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( invalidComp );
    doReturn( true ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( invalidComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( invalidComp );
    doReturn( "" ).when( dashboardWriterSpy).getComponentImplementationPath( invalidComp );
    Assert.assertEquals( "", dashboardWriterSpy.getComponentPath( invalidComp, "InvalidComponent" ));

    doReturn( true ).when( dashboardWriterSpy).isCustomComponent( customComp3 );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( customComp3 );
    doReturn( false ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( customComp3 );
    doReturn( true ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( customComp3 );
    doReturn( "sparkl" ).when( dashboardWriterSpy).getPluginIdFromOrigin( customComp3 );
    Assert.assertEquals(
      "sparkl" + PLUGIN_COMPONENT_FOLDER + "Custom3Component",
      dashboardWriterSpy.getComponentPath( customComp3, "Custom3Component" ));
  }

  @Test
  public void testWriteComponents() throws ValidationException, UnsupportedThingException, ThingWriteException,
    JSONException {

    CdfRunJsDashboardWriteContext context = mock( CdfRunJsDashboardWriteContext.class );
    Dashboard dash = mock( Dashboard.class );
    DashboardWcdfDescriptor dashboardWcdfDescriptor = mock( DashboardWcdfDescriptor.class );
    CdeRunJsThingWriterFactory factory = mock( CdeRunJsThingWriterFactory.class );
    IThingWriter writer = mock( CdfRunJsGenericComponentWriter.class );

    doReturn( factory ).when( context ).getFactory();
    doReturn( writer ).when( factory ).getWriter( any( Thing.class ) );

    doReturn( JSONObject.fromObject("{}") ).when( dashboardWcdfDescriptor ).toJSON();
    doReturn( dashboardWcdfDescriptor ).when( dash ).getWcdf();

    List<Component> componentList = new ArrayList<Component>(  );

    Component comp1 = mock( CustomComponent.class );
    Component comp2 = mock( CustomComponent.class );
    Component invalidComp = mock( CustomComponent.class );
    Component comp3 = mock( CustomComponent.class );

    componentList.add( comp1 );
    doReturn( "comp1" ).when( dashboardWriterSpy ).getComponentIdFromContext( context, comp1 );
    doReturn( "comp1" ).when( dashboardWriterSpy).getComponentName( comp1 );
    doReturn( true ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( comp1 );
    doReturn( false ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( comp1 );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( comp1 );
    doReturn( "Fake1Component" ).when( dashboardWriterSpy).getComponentClassName( comp1 );
    doReturn( "fake1/path" ).when( dashboardWriterSpy).getComponentPath( comp1, "Fake1Component" );

    componentList.add( comp2 );
    doReturn( "comp2" ).when( dashboardWriterSpy ).getComponentIdFromContext( context, comp2 );
    doReturn( "comp2" ).when( dashboardWriterSpy).getComponentName( comp2 );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( comp2 );
    doReturn( true ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( comp2 );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( comp2 );
    doReturn( "Fake2Component" ).when( dashboardWriterSpy).getComponentClassName( comp2 );
    doReturn( "fake2/path" ).when( dashboardWriterSpy).getComponentPath( comp2, "Fake2Component" );

    componentList.add( invalidComp );
    doReturn( "invalidComponent" ).when( dashboardWriterSpy ).getComponentIdFromContext( context, invalidComp );
    doReturn( "invalidComponent" ).when( dashboardWriterSpy).getComponentName( invalidComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( invalidComp );
    doReturn( true ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( invalidComp );
    doReturn( false ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( invalidComp );
    doReturn( "InvalidComponent" ).when( dashboardWriterSpy ).getComponentClassName( invalidComp );
    // custom components in the repository must contain an implementation path or will be ignored
    // it is needed for AMD path configuration purposes
    doReturn( "" ).when( dashboardWriterSpy).getComponentPath( invalidComp, "InvalidComponent" );

    componentList.add( comp3 );
    doReturn( "comp3" ).when( dashboardWriterSpy ).getComponentIdFromContext( context, comp3 );
    doReturn( "comp3" ).when( dashboardWriterSpy).getComponentName( comp3 );
    doReturn( false ).when( dashboardWriterSpy).isComponentStaticSystemOrigin( comp3 );
    doReturn( false ).when( dashboardWriterSpy).isComponentPluginRepositoryOrigin( comp3 );
    doReturn( true ).when( dashboardWriterSpy).isComponentOtherPluginStaticSystemOrigin( comp3 );
    doReturn( "sparkl" ).when( dashboardWriterSpy ).getPluginIdFromOrigin( comp3 );
    doReturn( "Fake3Component" ).when( dashboardWriterSpy).getComponentClassName( comp3 );
    doReturn( "sparkl/fake3/component" ).when( dashboardWriterSpy).getComponentPath( comp3, "Fake3Component" );

    doReturn( componentList ).when( dash ).getRegulars();

    Assert.assertEquals(
      "wcdfSettings = {};" + NEWLINE + NEWLINE + NEWLINE
        + "dashboard.addComponents([comp1, comp2, comp3]);" + NEWLINE,
      dashboardWriterSpy.writeComponents( context, dash ));

    Assert.assertEquals( 3, dashboardWriter.getComponentList().size() );

  }

}
