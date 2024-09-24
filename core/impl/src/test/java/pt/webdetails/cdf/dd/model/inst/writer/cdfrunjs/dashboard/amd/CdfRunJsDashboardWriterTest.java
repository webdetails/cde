/*!
 * Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeConstants.AmdModule;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.LayoutComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsDataSourceComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd.CdfRunJsGenericComponentWriter;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd.CdeRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.render.RenderLayout;
import pt.webdetails.cdf.dd.render.ResourceMap;
import pt.webdetails.cdf.dd.render.ResourceMap.ResourceKind;
import pt.webdetails.cdf.dd.render.ResourceMap.ResourceType;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.CDE_AMD_BASE_COMPONENT_PATH;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.CDE_AMD_REPO_COMPONENT_PATH;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.CDF_AMD_BASE_COMPONENT_PATH;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_DECLARATION;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_DECLARATION_DEBUG;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DASHBOARD_INIT;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.GET_WCDF_SETTINGS_FUNCTION;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT1;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.PLUGIN_COMPONENT_FOLDER;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.REQUIRE_START;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.REQUIRE_STOP;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.SCRIPT;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.TITLE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.WEBCONTEXT;

public class CdfRunJsDashboardWriterTest {

  private static Dashboard dash;
  private static DashboardWcdfDescriptor dashboardWcdfDescriptor;
  private static CdeRunJsThingWriterFactory factory;
  private static CdfRunJsDashboardWriter dashboardWriterSpy;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDashboardWriteOptions options;
  private static ResourceMap resources;

  private static final String NEWLINE = System.getProperty( "line.separator" );

  @Before
  public void setUp() throws Exception {
    dash = Mockito.mock( Dashboard.class );
    dashboardWcdfDescriptor = mock( DashboardWcdfDescriptor.class );
    factory = mock( CdeRunJsThingWriterFactory.class );
    dashboardWriterSpy = spy( new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT ) );
    context = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );
    resources = Mockito.mock( ResourceMap.class );
  }

  @After
  public void tearDown() throws Exception {
    dash = null;
    dashboardWcdfDescriptor = null;
    factory = null;
    dashboardWriterSpy = null;
    context = null;
    options = null;
    resources = null;
  }


  @Test
  public void testWriteLayout() throws Exception {

    doReturn( 0 ).when( dash ).getLayoutCount();
    assertEquals( "", dashboardWriterSpy.writeLayout( context, dash ) );

    doReturn( 1 ).when( dash ).getLayoutCount();
    LayoutComponent comp = Mockito.mock( LayoutComponent.class );
    doReturn( comp ).when( dash ).getLayout( "TODO" );
    JXPathContext docXP = Mockito.mock( JXPathContext.class );
    doReturn( docXP ).when( comp ).getLayoutXPContext();
    RenderLayout rLayout = Mockito.mock( RenderLayout.class );
    doReturn( rLayout ).when( dashboardWriterSpy ).getLayoutRenderer( docXP, context );
    doReturn( options ).when( context ).getOptions();
    final String aliasPrefix = "_alias_prefix_";
    doReturn( aliasPrefix ).when( options ).getAliasPrefix();
    final String sampleLayout = "<div id='content'></div>";
    doReturn( sampleLayout ).when( rLayout ).render( aliasPrefix );

    String layout = dashboardWriterSpy.writeLayout( context, dash );

    assertEquals( sampleLayout, layout );

    doReturn( "TestResourcePath1" ).when( context ).replaceTokensAndAlias( any() );
  }

  @Test
  public void testWriteCssCodeResources() throws Exception {
    // test resources
    ResourceMap testResources = new ResourceMap();
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc1", "jsFileRsrcPath1", "jsFileRsrc1" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.CODE, "jsCodeRsrc1", "jsCodeRsrcrPath1", "jsCodeRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc1", "cssFileRsrcPath1", "cssFileRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.CODE, "cssCodeRsrc1", "cssCodeRsrcPath1", "cssCodeRsrc1" );

    assertEquals( "cssCodeRsrc1" + NEWLINE, dashboardWriterSpy.writeCssCodeResources( testResources ) );
  }

  @Test
  public void testWriteJsCodeResources() throws Exception {
    // test resources
    ResourceMap testResources = new ResourceMap();
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc1", "jsFileRsrcPath1", "jsFileRsrc1" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.CODE, "jsCodeRsrc1", "jsCodeRsrcrPath1", "jsCodeRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc1", "cssFileRsrcPath1", "cssFileRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.CODE, "cssCodeRsrc1", "cssCodeRsrcPath1", "cssCodeRsrc1" );

    assertEquals( "jsCodeRsrc1" + NEWLINE, dashboardWriterSpy.writeJsCodeResources( testResources ) );
  }

  @Test
  public void testWriteComponents() throws ValidationException, UnsupportedThingException, ThingWriteException,
    JSONException {

    when( context.getOptions() ).thenReturn( options );

    IThingWriter dataSourceWriter = mock( CdfRunJsDataSourceComponentWriter.class );
    IThingWriter componentWriter = mock( CdfRunJsGenericComponentWriter.class );

    doReturn( factory ).when( context ).getFactory();
    doReturn( componentWriter ).when( factory ).getWriter( any( Component.class ) );
    doReturn( dataSourceWriter ).when( factory ).getWriter( any( DataSourceComponent.class ) );

    doReturn( new JSONObject( "{}" ) ).when( dashboardWcdfDescriptor ).toJSON();
    doReturn( dashboardWcdfDescriptor ).when( dash ).getWcdf();

    // Data Source Components
    List<DataSourceComponent> dashComponentList = new ArrayList<>();

    DataSourceComponent dataSourceComp1 = mock( DataSourceComponent.class );
    doReturn( "dataSourceComp1" ).when( dataSourceComp1 ).getName();
    dashComponentList.add( dataSourceComp1 );

    doReturn( dashComponentList ).when( dash ).getDataSources();

    // Regular Components
    List<Component> componentList = new ArrayList<>();

    Component comp1 = mock( Component.class );
    Component comp2 = mock( Component.class );
    Component invalidComp = mock( Component.class );
    Component comp3 = mock( Component.class );
    // custom component from static system origin
    componentList.add( comp1 );
    doReturn( "comp1" ).when( comp1 ).getId();
    doReturn( "comp1" ).when( comp1 ).getName();
    doReturn( true ).when( comp1 ).isVisualComponent();
    doReturn( true ).when( comp1 ).isCustomComponent();
    doReturn( true ).when( comp1 ).isComponentStaticSystemOrigin();
    doReturn( "Comp1Component" ).when( comp1 ).getComponentClassName();
    doReturn( "comp/Comp1Component" ).when( dashboardWriterSpy ).writeComponentModuleId( comp1, "Comp1Component" );
    // custom component from plugin repository origin
    componentList.add( comp2 );
    doReturn( "comp2" ).when( comp2 ).getId();
    doReturn( "comp2" ).when( comp2 ).getName();
    doReturn( true ).when( comp2 ).isVisualComponent();
    doReturn( true ).when( comp2 ).isCustomComponent();
    doReturn( false ).when( comp2 ).isComponentStaticSystemOrigin();
    doReturn( true ).when( comp2 ).isComponentPluginRepositoryOrigin();
    doReturn( false ).when( comp2 ).supportsLegacy();
    doReturn( "/cde/components/comp2/component.xml" ).when( comp2 ).getComponentImplementationPath();
    doReturn( false ).when( comp2 ).isComponentOtherPluginStaticSystemOrigin();
    doReturn( "Comp2Component" ).when( comp2 ).getComponentClassName();
    doReturn( "comp/Comp2Component" ).when( dashboardWriterSpy ).writeComponentModuleId( comp2, "Comp2Component" );
    // invalid custom component from plugin repository origin
    componentList.add( invalidComp );
    doReturn( "invalidComponent" ).when( invalidComp ).getId();
    doReturn( "invalidComponent" ).when( invalidComp ).getName();
    doReturn( true ).when( invalidComp ).isVisualComponent();
    doReturn( true ).when( invalidComp ).isCustomComponent();
    doReturn( false ).when( invalidComp ).isComponentStaticSystemOrigin();
    doReturn( true ).when( invalidComp ).isComponentPluginRepositoryOrigin();
    doReturn( false ).when( invalidComp ).supportsLegacy();
    doReturn( "" ).when( invalidComp ).getComponentImplementationPath();
    doReturn( false ).when( invalidComp ).isComponentOtherPluginStaticSystemOrigin();
    doReturn( "InvalidComponent" ).when( invalidComp ).getComponentClassName();
    // custom components in the repository must contain an implementation path or will be ignored
    // it is needed for AMD path configuration purposes
    doReturn( "" ).when( dashboardWriterSpy ).writeComponentModuleId( invalidComp, "InvalidComponent" );
    // custom component from other plugin static system origin
    componentList.add( comp3 );
    doReturn( "comp3" ).when( comp3 ).getId();
    doReturn( "comp3" ).when( comp3 ).getName();
    doReturn( true ).when( comp3 ).isVisualComponent();
    doReturn( true ).when( comp3 ).isCustomComponent();
    doReturn( false ).when( comp3 ).isComponentStaticSystemOrigin();
    doReturn( false ).when( comp3 ).isComponentPluginRepositoryOrigin();
    doReturn( true ).when( comp3 ).isComponentOtherPluginStaticSystemOrigin();
    doReturn( "sparkl" ).when( comp3 ).getPluginIdFromOrigin();
    doReturn( "Comp3Component" ).when( comp3 ).getComponentClassName();
    doReturn( "sparkl/comp/Comp3Component" ).when( dashboardWriterSpy ).writeComponentModuleId( comp3,
      "Comp3Component" );

    doReturn( componentList ).when( dash ).getRegulars();

    StringBuilder out = new StringBuilder();
    Map<String, String> componentModules = dashboardWriterSpy.writeComponents( context, dash, out );

    assertEquals(
      INDENT1 + "dashboard.addDataSource(\"dataSourceComp1\", );" + NEWLINE + NEWLINE
        + NEWLINE + "dashboard.addComponents([comp1, comp2, comp3]);" + NEWLINE,
      out.toString() );

    // invalid components are not added
    assertEquals( 3, componentModules.size() );
    assertTrue( componentModules.containsKey( "Comp1Component" ) );
    assertTrue( componentModules.containsKey( "Comp2Component" ) );
    assertFalse( componentModules.containsKey( "InvalidComponent" ) );
    assertTrue( componentModules.containsKey( "Comp3Component" ) );
  }

  @Test
  public void testWriteComponentModuleId() {
    final String className = "CompComponent";
    // primitive component from static system origin
    Component comp = mock( Component.class );
    doReturn( true ).when( comp ).isPrimitiveComponent();
    doReturn( true ).when( comp ).isComponentStaticSystemOrigin();
    assertEquals(
      CDF_AMD_BASE_COMPONENT_PATH + className,
      dashboardWriterSpy.writeComponentModuleId( comp, className ) );
    // custom component from static system origin
    comp = mock( Component.class );
    doReturn( true ).when( comp ).isCustomComponent();
    doReturn( true ).when( comp ).isComponentStaticSystemOrigin();
    assertEquals(
      CDE_AMD_BASE_COMPONENT_PATH + className,
      dashboardWriterSpy.writeComponentModuleId( comp, className ) );
    // custom component from plugin repository origin with no implementation path
    comp = mock( Component.class );
    doReturn( true ).when( comp ).isCustomComponent();
    doReturn( false ).when( comp ).isComponentStaticSystemOrigin();
    doReturn( true ).when( comp ).isComponentPluginRepositoryOrigin();
    doReturn( null ).when( comp ).getComponentImplementationPath();
    doReturn( "comp/component.xml" ).when( comp ).getComponentSourcePath();
    assertEquals(
      CDE_AMD_REPO_COMPONENT_PATH + "comp/" + className,
      dashboardWriterSpy.writeComponentModuleId( comp, className ) );
    // custom component from plugin repository origin
    comp = mock( Component.class );
    doReturn( true ).when( comp ).isCustomComponent();
    doReturn( false ).when( comp ).isComponentStaticSystemOrigin();
    doReturn( true ).when( comp ).isComponentPluginRepositoryOrigin();
    doReturn( "comp/comp.js" ).when( comp ).getComponentImplementationPath();
    assertEquals(
      CDE_AMD_REPO_COMPONENT_PATH + "comp/comp",
      dashboardWriterSpy.writeComponentModuleId( comp, className ) );
    // custom component from other plugin static system origin
    comp = mock( Component.class );
    doReturn( true ).when( comp ).isCustomComponent();
    doReturn( false ).when( comp ).isComponentStaticSystemOrigin();
    doReturn( false ).when( comp ).isComponentPluginRepositoryOrigin();
    doReturn( true ).when( comp ).isComponentOtherPluginStaticSystemOrigin();
    doReturn( "sparkl" ).when( comp ).getPluginIdFromOrigin();
    doReturn( "comp/comp.js" ).when( comp ).getComponentImplementationPath();
    assertEquals(
      "sparkl" + PLUGIN_COMPONENT_FOLDER + "CompComponent",
      dashboardWriterSpy.writeComponentModuleId( comp, className ) );
    // widget components are not supported
    comp = mock( Component.class );
    doReturn( true ).when( comp ).isWidgetComponent();
    assertEquals( "", dashboardWriterSpy.writeComponentModuleId( comp, className ) );
  }

  @Test
  public void testWriteHeaders() {
    DashboardWcdfDescriptor wcdf = Mockito.mock( DashboardWcdfDescriptor.class );
    doReturn( "Title 1" ).when( wcdf ).getTitle();
    doReturn( wcdf ).when( dash ).getWcdf();
    assertEquals(
      MessageFormat.format( TITLE, dash.getWcdf().getTitle() ) + NEWLINE
        + MessageFormat.format( SCRIPT, dashboardWriterSpy.writeWebcontext( "cdf", true ) ),
      dashboardWriterSpy.writeHeaders( dash ) );
  }

  @Test
  public void testWriteWebContext() {
    assertEquals(
      MessageFormat.format( WEBCONTEXT, "cdf", "true" ),
      dashboardWriterSpy.writeWebcontext( "cdf", true ) );
  }

  @Test
  public void testWriteContent() {
    // layout
    final String layout = "<div id='content'></div>";
    //component modules
    Map<String, String> componentModules = new LinkedHashMap<>();
    // components sourcecode
    final String components = "var comp = new CompComponent();";
    componentModules.put( "comp", "comp/CompComponent" );

    doReturn( "TestResourcePath1" ).when( context ).replaceTokensAndAlias( any() );
    doReturn( "jsFileRsrcPath1" ).when( context ).replaceTokensAndAlias( "jsFileRsrcPath1" );

    doReturn( "content" ).when( dashboardWriterSpy )
      .wrapRequireDefinitions( resources, componentModules, components, context );

    assertEquals(
      layout + NEWLINE
        + "<script language=\"javascript\" type=\"text/javascript\">" + NEWLINE
        + "content" + NEWLINE
        + "</script>" + NEWLINE,
      dashboardWriterSpy.writeContent( resources, layout, componentModules, components, context ) );
  }

  @Test
  public void testWriteWcdfSettings() throws ThingWriteException, JSONException {

    doReturn( "" ).when( dashboardWcdfDescriptor ).getTitle();
    doReturn( "thatGuy" ).when( dashboardWcdfDescriptor ).getAuthor();
    doReturn( "description" ).when( dashboardWcdfDescriptor ).getDescription();
    doReturn( "WDDocsRequire" ).when( dashboardWcdfDescriptor ).getStyle();
    doReturn( "blueprint" ).when( dashboardWcdfDescriptor ).getRendererType();
    doReturn( true ).when( dashboardWcdfDescriptor ).isRequire();
    // deprecated
    doReturn( false ).when( dashboardWcdfDescriptor ).isWidget();
    doReturn( "" ).when( dashboardWcdfDescriptor ).getWidgetName();
    String[] widgetParams = { "" };
    doReturn( widgetParams ).when( dashboardWcdfDescriptor ).getWidgetParameters();

    JSONObject json = new JSONObject();
    json.put( "title", dashboardWcdfDescriptor.getTitle() );
    json.put( "author", dashboardWcdfDescriptor.getAuthor() );
    json.put( "description", dashboardWcdfDescriptor.getDescription() );
    json.put( "style", dashboardWcdfDescriptor.getStyle() );
    json.put( "rendererType", dashboardWcdfDescriptor.getRendererType() );
    json.put( "require", dashboardWcdfDescriptor.isRequire() );
    // deprecated
    json.put( "widget", dashboardWcdfDescriptor.isWidget() );
    json.put( "widgetName", dashboardWcdfDescriptor.getWidgetName() );
    json.put( "widgetParameters", dashboardWcdfDescriptor.getWidgetParameters() );

    doReturn( json ).when( dashboardWcdfDescriptor ).toJSON();

    doReturn( dashboardWcdfDescriptor ).when( dash ).getWcdf();

    assertEquals(
      MessageFormat.format( GET_WCDF_SETTINGS_FUNCTION, dashboardWcdfDescriptor.toJSON().toString( 6 ) ),
      dashboardWriterSpy.writeWcdfSettings( dash ) );
  }

  @Test
  public void testWrapRequireDefinitions() {

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

    // Add dashboard default AMD module ids and class names
    moduleIds.add( AmdModule.DASHBOARD_BLUEPRINT.getId() );
    moduleIds.add( AmdModule.LOGGER.getId() );
    moduleIds.add( AmdModule.JQUERY.getId() );
    moduleIds.add( AmdModule.UNDERSCORE.getId() );
    moduleIds.add( AmdModule.MOMENT.getId() );
    moduleIds.add( AmdModule.CCC_CDO.getId() );
    moduleIds.add( AmdModule.CCC_DEF.getId() );
    moduleIds.add( AmdModule.CCC_PV.getId() );
    moduleIds.add( AmdModule.CCC_PVC.getId() );
    moduleIds.add( AmdModule.UTILS.getId() );
    moduleClassNames.add( AmdModule.DASHBOARD_BLUEPRINT.getClassName() );
    moduleClassNames.add( AmdModule.LOGGER.getClassName() );
    moduleClassNames.add( AmdModule.JQUERY.getClassName() );
    moduleClassNames.add( AmdModule.UNDERSCORE.getClassName() );
    moduleClassNames.add( AmdModule.MOMENT.getClassName() );
    moduleClassNames.add( AmdModule.CCC_CDO.getClassName() );
    moduleClassNames.add( AmdModule.CCC_DEF.getClassName() );
    moduleClassNames.add( AmdModule.CCC_PV.getClassName() );
    moduleClassNames.add( AmdModule.CCC_PVC.getClassName() );
    moduleClassNames.add( AmdModule.UTILS.getClassName() );
    // Add test dashboard AMD module ids and class names
    moduleIds.add( "cdf/components/TestComponent1" );
    moduleIds.add( "cdf/components/TestComponent2" );
    moduleIds.add( "cde/resources/jsFileRsrcPath1" );
    moduleIds.add( "css!cde/resources/cssFileRsrcPath1" );
    moduleClassNames.add( "TestComponent1" );
    moduleClassNames.add( "TestComponent2" );
    moduleClassNames.add( "jsFileRsrc1" );

    doReturn( false ).when( options ).isDebug();
    doReturn( options ).when( context ).getOptions();

    final String content = "dashboard.addComponent(new TestComponent1({test: 1}));";

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( moduleIds, "'," + NEWLINE + INDENT1 + "'" ),
        StringUtils.join( moduleClassNames, "," + NEWLINE + INDENT1 ) ) ).append( NEWLINE )
      .append( DASHBOARD_DECLARATION ).append( NEWLINE )
      .append( "jsCodeRsrc1" ).append( NEWLINE )
      .append( content ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    assertEquals(
      dashboardResult.toString(),
      dashboardWriterSpy.wrapRequireDefinitions( testResources, testComponentModules, content, context ) );

    verify( dashboardWriterSpy, times( 1 ) ).addDefaultDashboardModules( moduleIds, moduleClassNames );

    // Debug Mode set's window.dashboard
    doReturn( true ).when( options ).isDebug();
    dashboardResult.setLength( 0 );
    dashboardResult
      .append( MessageFormat.format( REQUIRE_START, StringUtils.join( moduleIds, "'," + NEWLINE + INDENT1 + "'" ),
        StringUtils.join( moduleClassNames, "," + NEWLINE + INDENT1 ) ) )
      .append( NEWLINE )
      .append( DASHBOARD_DECLARATION_DEBUG ).append( NEWLINE )
      .append( "jsCodeRsrc1" ).append( NEWLINE )
      .append( content ).append( NEWLINE )
      .append( DASHBOARD_INIT )
      .append( REQUIRE_STOP );

    assertEquals(
      dashboardResult.toString(),
      dashboardWriterSpy.wrapRequireDefinitions( testResources, testComponentModules, content, context ) );

    verify( dashboardWriterSpy, times( 2 ) ).addDefaultDashboardModules( moduleIds, moduleClassNames );
  }

  @Test
  public void testDashboardType() {
    CdfRunJsDashboardWriter dashboardWriter =
      new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT );
    assertEquals( "cdf/Dashboard.Blueprint", dashboardWriter.getDashboardModule().getId() );
    dashboardWriter = new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP );
    assertEquals( "cdf/Dashboard.Bootstrap", dashboardWriter.getDashboardModule().getId() );
    dashboardWriter = new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.MOBILE );
    assertEquals( "cdf/Dashboard.Mobile", dashboardWriter.getDashboardModule().getId() );
    dashboardWriter = new CdfRunJsDashboardWriter( DashboardWcdfDescriptor.DashboardRendererType.CLEAN );
    assertEquals( "cdf/Dashboard.Clean", dashboardWriter.getDashboardModule().getId() );
  }

  @Test
  public void testWriteRequireJsExecutionFunction() {
    StringBuilder out = new StringBuilder();
    ArrayList<String> moduleIds = new ArrayList<>();
    ArrayList<String> moduleClassNames = new ArrayList<>();
    moduleIds.add( "cdf/components/TestComponent1" );
    moduleIds.add( "cde/resources/jsFileRsrc1" );
    moduleIds.add( "css!cde/resources/cssFileRsrc1" );
    moduleClassNames.add( "TestComponent1" );
    moduleClassNames.add( "jsFileRsrc1" );
    moduleClassNames.add( "" );

    dashboardWriterSpy.writeRequireJsExecutionFunction( out, moduleIds, moduleClassNames );

    assertEquals(
      MessageFormat.format(
        REQUIRE_START,
        "cdf/components/TestComponent1',"
          + NEWLINE + INDENT1 + "'cde/resources/jsFileRsrc1',"
          + NEWLINE + INDENT1 + "'css!cde/resources/cssFileRsrc1",
        "TestComponent1,"
          + NEWLINE + INDENT1 + "jsFileRsrc1" ) + NEWLINE,
      out.toString() );
  }

  @Test
  public void testGetDashboardModule() {
    doReturn( DashboardWcdfDescriptor.DashboardRendererType.BLUEPRINT ).when( dashboardWriterSpy ).getType();
    assertEquals( AmdModule.DASHBOARD_BLUEPRINT, dashboardWriterSpy.getDashboardModule() );
    doReturn( DashboardWcdfDescriptor.DashboardRendererType.BOOTSTRAP ).when( dashboardWriterSpy ).getType();
    assertEquals( AmdModule.DASHBOARD_BOOTSTRAP, dashboardWriterSpy.getDashboardModule() );
    doReturn( DashboardWcdfDescriptor.DashboardRendererType.MOBILE ).when( dashboardWriterSpy ).getType();
    assertEquals( AmdModule.DASHBOARD_MOBILE, dashboardWriterSpy.getDashboardModule() );
    doReturn( DashboardWcdfDescriptor.DashboardRendererType.CLEAN ).when( dashboardWriterSpy ).getType();
    assertEquals( AmdModule.DASHBOARD_CLEAN, dashboardWriterSpy.getDashboardModule() );
  }

  @Test
  public void testWriteFileResourcesRequireJSPathConfig() {
    StringBuilder out = new StringBuilder();
    doReturn( "1234" ).when( dashboardWriterSpy ).getRandomUUID();
    // resources
    ResourceMap testResources = new ResourceMap();
    // unnamed file resource
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "", "a/path/../file2.js", "jsFileRsrc2" );
    // named file resource
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc1", "/jsFileRsrcPath1", "jsFileRsrc1" );
    // file resource not normalized with white spaces
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc3White", "a/path/../white space/file3White.js", "jsFileRsrc3White" );
    // file resource not normalized
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc3", "a/path/../file3.js", "jsFileRsrc3" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.CODE, "jsCodeRsrc1", "jsCodeRsrcrPath1", "jsCodeRsrc1" );
    // absolute file resource
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc4", "http://dummy/jsFileRsrcPath4.js",
      "jsFileRsrc4" );
    // absolute file resource with white spaces
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc4White", "http://dummy/white space/jsFileRsrcPath4White.js",
      "jsFileRsrc4White" );
    // absolute unnamed file resource
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "", "http://dummy/jsFileRsrcPath5.js",
      "jsFileRsrc5" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc1", "/cssFileRsrcPath1", "cssFileRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc2", "cssFileRsrcPath2.css", "cssFileRsrc2" );
    testResources.add( ResourceKind.CSS, ResourceType.CODE, "cssCodeRsrc1", "cssCodeRsrcPath1", "cssCodeRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc3", "http://dummy/cssFileRsrcPath3.css",
      "cssFileRsrc3" );

    // context
    doReturn( "/jsFileRsrcPath1" ).when( context ).replaceTokensAndAlias( "/jsFileRsrcPath1" );
    doReturn( "a/path/../file2.js" ).when( context ).replaceTokensAndAlias( "a/path/../file2.js" );
    doReturn( "a/path/../white space/file3White.js" ).when( context ).replaceTokensAndAlias( "a/path/../white space/file3White.js" );
    doReturn( "a/path/../file3.js" ).when( context ).replaceTokensAndAlias( "a/path/../file3.js" );
    doReturn( "http://dummy/jsFileRsrcPath4.js" )
      .when( context ).replaceTokensAndAlias( "http://dummy/jsFileRsrcPath4.js" );
    doReturn( "http://dummy/white space/jsFileRsrcPath4White.js" )
      .when( context ).replaceTokensAndAlias( "http://dummy/white space/jsFileRsrcPath4White.js" );
    doReturn( "http://dummy/jsFileRsrcPath5.js" )
      .when( context ).replaceTokensAndAlias( "http://dummy/jsFileRsrcPath5.js" );
    doReturn( "/cssFileRsrcPath1" ).when( context ).replaceTokensAndAlias( "/cssFileRsrcPath1" );
    doReturn( "cssFileRsrcPath2.css" ).when( context ).replaceTokensAndAlias( "cssFileRsrcPath2.css" );
    doReturn( "http://dummy/cssFileRsrcPath3.css" )
      .when( context ).replaceTokensAndAlias( "http://dummy/cssFileRsrcPath3.css" );

    Map<String, String> resourceModules =
      dashboardWriterSpy.writeFileResourcesRequireJSPathConfig( out, testResources, context );
    assertEquals( "jsFileRsrc1", resourceModules.get( "cde/resources/jsFileRsrcPath1" ) );
    assertEquals( "", resourceModules.get( "cde/resources/a/file2" ) );
    assertEquals( "jsFileRsrc3White", resourceModules.get( "cde/resources/a/white%20space/file3White" ) );
    assertEquals( "jsFileRsrc3", resourceModules.get( "cde/resources/a/file3" ) );
    assertEquals( "jsFileRsrc4", resourceModules.get( "cde/resources/jsFileRsrc4" ) );
    assertEquals( "jsFileRsrc4White", resourceModules.get( "cde/resources/jsFileRsrc4White" ) );
    assertEquals( "", resourceModules.get( "cde/resources/1234" ) ); // random UUID mocked value
    assertEquals( "", resourceModules.get( "css!cde/resources/cssFileRsrcPath1" ) );
    assertEquals( "", resourceModules.get( "css!cde/resources/cssFileRsrcPath2" ) );
    assertEquals( "", resourceModules.get( "css!cde/resources/cssFileRsrc3" ) );

    assertEquals( "requireCfg['paths']['cde/resources/jsFileRsrc4'] = 'http://dummy/jsFileRsrcPath4';"  + NEWLINE
      + "requireCfg['paths']['cde/resources/jsFileRsrc4White'] = 'http://dummy/white%20space/jsFileRsrcPath4White';"  + NEWLINE
      + "requireCfg['paths']['cde/resources/1234'] = 'http://dummy/jsFileRsrcPath5';" + NEWLINE
      + "requireCfg['paths']['cde/resources/cssFileRsrc3'] = 'http://dummy/cssFileRsrcPath3';" + NEWLINE
      + "require.config(requireCfg);", out.toString().trim() );

    Map<String, String> expectedResourceModules = new LinkedHashMap<String, String>();
    expectedResourceModules.put( "cde/resources/jsFileRsrcPath1", "jsFileRsrc1" );
    expectedResourceModules.put( "cde/resources/a/white%20space/file3White", "jsFileRsrc3White" );
    expectedResourceModules.put( "cde/resources/a/file3", "jsFileRsrc3" );
    expectedResourceModules.put( "cde/resources/jsFileRsrc4", "jsFileRsrc4" );
    expectedResourceModules.put( "cde/resources/jsFileRsrc4White", "jsFileRsrc4White" );
    expectedResourceModules.put( "css!cde/resources/cssFileRsrcPath1", "" );
    expectedResourceModules.put( "css!cde/resources/cssFileRsrcPath2", "" );
    expectedResourceModules.put( "css!cde/resources/cssFileRsrc3", "" );
    expectedResourceModules.put( "cde/resources/a/file2", "" );
    // unnamed file resource from full URI (UUID) will come last
    expectedResourceModules.put( "cde/resources/1234", "" );

    assertEquals( expectedResourceModules, resourceModules );
  }

  @Test
  public void testGetJsModuleClassNames() {
    // resources
    ResourceMap testResources = new ResourceMap();
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, "jsFileRsrc1", "jsFileRsrcPath1", "jsFileRsrc1" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.FILE, /*name*/"", "jsFileRsrcPath2", "jsFileRsrc2" );
    testResources.add( ResourceKind.JAVASCRIPT, ResourceType.CODE, "jsCodeRsrc1", "jsCodeRsrcrPath1", "jsCodeRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.FILE, "cssFileRsrc1", "cssFileRsrcPath1", "cssFileRsrc1" );
    testResources.add( ResourceKind.CSS, ResourceType.CODE, "cssCodeRsrc1", "cssCodeRsrcPath1", "cssCodeRsrc1" );

    ArrayList<String> expectedClassNames = new ArrayList<String>();
    expectedClassNames.add( "jsFileRsrc1" );

    assertEquals( expectedClassNames, dashboardWriterSpy.getJsModuleClassNames( testResources ) );
  }
}
