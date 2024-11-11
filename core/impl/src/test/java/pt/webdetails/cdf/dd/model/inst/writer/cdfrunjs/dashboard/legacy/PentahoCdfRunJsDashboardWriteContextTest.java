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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy;

import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContextForTesting;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.legacy.CdfRunJsThingWriterFactory;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import static org.junit.Assert.assertEquals;

public class PentahoCdfRunJsDashboardWriteContextTest {

  private static final String GET_RESOURCES = "api/resources";
  private static final String ROOT = "src/test/resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";
  private static final String SYSTEM = "system";
  private static final String TEST_PLUGIN = "test-plugin";
  private static final String CDE_PLUGIN_URL = "/pentaho/plugin/pentaho-cdf-dd/";

  private static final String indent = "";
  private static final boolean bypassCacheRead = true;

  private static CdfRunJsDashboardWriteOptions options;
  private static CdfRunJsThingWriterFactory factory;

  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() {
    factory = new CdfRunJsThingWriterFactory();
    options = getCdfRunJsDashboardWriteOptions();
  }

  @Test
  public void testReplaceTokensForSolutionDashboard() {
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );

    context = this.getContext( dashboardPath, false );

    String jsResource = "${res:script.js}";
    String cssResource = "${res:style.css}";
    String absoluteResource = "${res:/src/test/resources/style.css}";
    String solutionAbsoluteResource = "${solution:script.js}";
    String solutionRelativeResource = "${solution:/src/test/resources/script.js}";

    String jsResourceExpected = CDE_PLUGIN_URL
      + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring( 1 );
    String cssResourceExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "style.css" ).substring( 1 );
    String absoluteExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, "style.css" ).substring( 1 );
    String solutionAbsoluteResourceExpected = CDE_PLUGIN_URL
      + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring( 1 );
    String solutionRelativeResourceExpected =
      CDE_PLUGIN_URL + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, "script.js" ).substring( 1 );

    String jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    String cssResourceReplaced = removeParams( context.replaceTokens( cssResource ) );
    String absoluteResourceReplaced = removeParams( context.replaceTokens( absoluteResource ) );
    String solutionAbsoluteResourceReplaced = removeParams( context.replaceTokens( solutionAbsoluteResource ) );
    String solutionRelativeResourceReplaced = removeParams( context.replaceTokens( solutionRelativeResource ) );

    assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );
    assertEquals( "${res:style.css} replacement failed", cssResourceExpected, cssResourceReplaced );
    assertEquals( "${res:/src/test/resources/style.css} replacement failed", absoluteExpected,
      absoluteResourceReplaced );
    assertEquals( "${solution:script.js} replacement failed", solutionAbsoluteResourceExpected,
      solutionAbsoluteResourceReplaced );
    assertEquals( "${solution:/src/test/resources/script.js} replacement failed", solutionRelativeResourceExpected,
      solutionRelativeResourceReplaced );
  }

  @Test
  public void testReplaceTokensForSystemDashboard() {
    //setup context
    final String dashboardPath = RepositoryHelper
      .joinPaths( ROOT, SYSTEM, TEST_PLUGIN, TEST_FOLDER, DASHBOARD ).substring( 1 );

    context = this.getContext( dashboardPath, true );

    // Absolute paths
    String filePath = "/pentaho-cdf-dd/css/master.css";
    String systemResourceExpected = "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin" + filePath;

    assertEquals(
      "absolute ${system:} replacement failed",
      systemResourceExpected,
      context.replaceTokens( "${system:" + filePath + "}" ) );

    // Relative paths
    filePath = "pentaho-cdf-dd/css/master.css";
    systemResourceExpected = "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    assertEquals(
      "relative ${system:} replacement failed",
      systemResourceExpected,
      context.replaceTokens( "${system:" + filePath + "}" )
    );

    filePath = "./pentaho-cdf-dd/css/master.css";
    systemResourceExpected = "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    assertEquals(
      "relative ${system:} replacement failed",
      systemResourceExpected,
      context.replaceTokens( "${system:" + filePath + "}" )
    );

    filePath = "../pentaho-cdf-dd/css/master.css";
    systemResourceExpected = "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    assertEquals(
      "relative ${system:} replacement failed",
      systemResourceExpected,
      context.replaceTokens( "${system:" + filePath + "}" )
    );
  }

  @Test
  public void testAbsoluteSchemePaths() {
    String jsResourceReplaced;
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );

    options = new CdfRunJsDashboardWriteOptions( false, false, "", "" );
    context = this.getContext( dashboardPath, false );

    String jsResource = "${res:script.js}";

    String jsResourceExpected = CDE_PLUGIN_URL
      + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" ).substring( 1 );
    String jsResourceAbsoluteExpected = "http://localhost:8080/pentaho/plugin/pentaho-cdf-dd"
      + RepositoryHelper.joinPaths( GET_RESOURCES, ROOT, TEST_FOLDER, "script.js" );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( true, false, "localhost:8080", "http" );
    context = this.getContext( dashboardPath, false );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    assertEquals( "${res:script.js} replacement failed", jsResourceAbsoluteExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( false, false, "localhost:8080", "http" );
    context = this.getContext( dashboardPath, false );

    jsResourceReplaced = removeParams( context.replaceTokens( jsResource ) );
    assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );
  }

  private PentahoCdfRunJsDashboardWriteContext getContext( String dashboardPath, boolean isSystem ) {
    Dashboard dashboard = CdfRunJsDashboardWriteContextForTesting.getDashboard( dashboardPath, isSystem );

    return new PentahoCdfRunJsDashboardWriteContextForTesting( factory, indent, bypassCacheRead, dashboard, options );
  }

  private static CdfRunJsDashboardWriteOptions getCdfRunJsDashboardWriteOptions() {
    boolean absolute = false;
    boolean debug = false;
    String absRoot = "";
    String scheme = "";
    return new CdfRunJsDashboardWriteOptions( absolute, debug, absRoot, scheme );
  }

  private String removeParams( String msg ) {
    return msg.substring( 0, msg.indexOf( "?" ) );
  }

}
