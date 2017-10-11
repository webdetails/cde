/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class PentahoCdfRunJsDashboardWriteContextTest {

  private static final String ROOT = "test-resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";
  private static final String SYSTEM = "system";
  private static final String TEST_PLUGIN = "test-plugin";

  private static final String indent = "";
  private static final boolean bypassCacheRead = true;

  private static CdfRunJsDashboardWriteOptions options;
  private static CdfRunJsThingWriterFactory factory;

  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() throws Exception {
    factory = new CdfRunJsThingWriterFactory();
    options = getCdfRunJsDashboardWriteOptions();
  }

  @Test
  public void testReplaceTokensForSolutionDashboard() {
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    String jsResource = "${res:script.js}";
    String cssResource = "${res:style.css}";
    String absoluteResource = "${res:/test-resources/style.css}";
    String solutionAbsoluteResource = "${solution:script.js}";
    String solutionRelativeResource = "${solution:/test-resources/script.js}";

    String jsResourceExpected = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, "script.js" );
    String cssResourceExpected = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, "style.css" );
    String absoluteExpected = RepositoryHelper.joinPaths( ROOT, "style.css" );
    String solutionAbsoluteResourceExpected = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, "script.js" );
    String solutionRelativeResourceExpected = RepositoryHelper.joinPaths( ROOT, "script.js" );

    String jsResourceReplaced = context.replaceTokens( jsResource );
    String cssResourceReplaced = context.replaceTokens( cssResource );
    String absoluteResourceReplaced = context.replaceTokens( absoluteResource );
    String solutionAbsoluteResourceReplaced = context.replaceTokens( solutionAbsoluteResource );
    String solutionRelativeResourceReplaced = context.replaceTokens( solutionRelativeResource );

    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );
    Assert.assertEquals( "${res:style.css} replacement failed", cssResourceExpected, cssResourceReplaced );
    Assert.assertEquals( "${res:/test-resources/style.css} replacement failed", absoluteExpected,
      absoluteResourceReplaced );
    Assert.assertEquals( "${solution:script.js} replacement failed", solutionAbsoluteResourceExpected,
      solutionAbsoluteResourceReplaced );
    Assert.assertEquals( "${solution:/test-resources/script.js} replacement failed", solutionRelativeResourceExpected,
      solutionRelativeResourceReplaced );
  }

  @Test
  public void testReplaceTokensForSystemDashboard() {
    //setup context
    final String dashboardPath =
        RepositoryHelper.joinPaths( ROOT, SYSTEM, TEST_PLUGIN, TEST_FOLDER, DASHBOARD ).substring( 1 );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
        indent, bypassCacheRead, getDashboard( dashboardPath, true ), options );

    // Absolute paths
    String filePath = "/pentaho-cdf-dd/css/master.css";
    String systemResourceExpected =
        "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin" + filePath;

    Assert.assertEquals(
        "absolute ${system:} replacement failed",
        systemResourceExpected,
        context.replaceTokens( "${system:" + filePath + "}" ) );

    // Relative paths
    filePath = "pentaho-cdf-dd/css/master.css";
    systemResourceExpected =
      "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    Assert.assertEquals(
        "relative ${system:} replacement failed",
        systemResourceExpected,
        context.replaceTokens( "${system:" + filePath + "}" )
    );

    filePath = "./pentaho-cdf-dd/css/master.css";
    systemResourceExpected =
      "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    Assert.assertEquals(
      "relative ${system:} replacement failed",
      systemResourceExpected,
      context.replaceTokens( "${system:" + filePath + "}" )
    );

    filePath = "../pentaho-cdf-dd/css/master.css";
    systemResourceExpected =
      "/pentaho/plugin/pentaho-cdf-dd/api/resources/system/mockPlugin/" + filePath;
    Assert.assertEquals(
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
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    String jsResource = "${res:script.js}";

    String jsResourceExpected = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, "script.js" );
    String jsResourceAbsoluteExpected = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, "script.js" );

    jsResourceReplaced = context.replaceTokens( jsResource );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( true, false, "localhost:8080", "http" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    jsResourceReplaced = context.replaceTokens( jsResource );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceAbsoluteExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( false, false, "localhost:8080", "http" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    jsResourceReplaced = context.replaceTokens( jsResource );
    Assert.assertEquals( "${res:script.js} replacement failed", jsResourceExpected, jsResourceReplaced );
  }

  @Test
  public void testImgPaths() {
    //setup context
    String dashboardPath = RepositoryHelper.joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );
    options = new CdfRunJsDashboardWriteOptions( false, false, "", "" );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    String image = "image.jpg";
    String img = "${img:" + image + "}";
    String cdePluginUrl = "pentaho/plugin/pentaho-cdf-dd";
    String getResources = "api/resources";

    String failureMessage = "${img:" + image + "} replacement failed";

    String timestamp = String.valueOf( context.getWriteDate().getTime() );
    String jsResourceExpected = RepositoryHelper.joinPaths(
      cdePluginUrl, getResources, ROOT, TEST_FOLDER, image + "?v=" + timestamp );

    String jsResourceReplaced = context.replaceTokens( img );
    Assert.assertEquals( failureMessage, jsResourceExpected, jsResourceReplaced );

    String hostPort = "localhost:8080";
    String scheme = "http";
    options = new CdfRunJsDashboardWriteOptions( true, false, hostPort, scheme );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    timestamp = String.valueOf( context.getWriteDate().getTime() );
    String absolutePath = scheme + "://" + hostPort
      + RepositoryHelper.joinPaths( cdePluginUrl, getResources, ROOT, TEST_FOLDER, image );
    String jsResourceAbsoluteExpected = absolutePath + "?v=" + timestamp;
    jsResourceReplaced = context.replaceTokens( img );
    Assert.assertEquals( failureMessage, jsResourceAbsoluteExpected, jsResourceReplaced );

    options = new CdfRunJsDashboardWriteOptions( false, false, hostPort, scheme );
    context = new PentahoCdfRunJsDashboardWriteContextForTesting( factory,
      indent, bypassCacheRead, getDashboard( dashboardPath, false ), options );

    timestamp = String.valueOf( context.getWriteDate().getTime() );
    jsResourceExpected = RepositoryHelper.joinPaths(
      cdePluginUrl, getResources, ROOT, TEST_FOLDER, image + "?v=" + timestamp );
    jsResourceReplaced = context.replaceTokens( img );
    Assert.assertEquals( failureMessage, jsResourceExpected, jsResourceReplaced );
  }

  private static Dashboard getDashboard( String path, boolean isSystem ) {
    Document wcdfDoc = null;
    try {
      wcdfDoc = Utils.getDocument( new FileInputStream( new File( path ) ) );
    } catch ( DocumentException e ) {
      e.printStackTrace();
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.fromXml( wcdfDoc );
    Dashboard.Builder builder = new Dashboard.Builder();
    DashboardType dashboardType = null;
    try {
      dashboardType = new DashboardType.Builder().build();
    } catch ( ValidationException e ) {
      e.printStackTrace();
    }

    if ( !isSystem ) {
      builder.setSourcePath( path );
    } else {
      // this is needed because we need to remove the test-resources path used to get the file
      // this is secure because there are no folder before the system while getting files from the server
      builder.setSourcePath( path.replace( ROOT + "/", "" ) );
    }

    builder.setWcdf( wcdf );
    builder.setMeta( dashboardType );
    MetaModel.Builder metaBuilder = new MetaModel.Builder();
    MetaModel model;
    Dashboard dashboard = null;
    try {
      model = metaBuilder.build();
      dashboard = builder.build( model );

    } catch ( ValidationException e ) {
      e.printStackTrace();
    }
    return dashboard;
  }

  private static CdfRunJsDashboardWriteOptions getCdfRunJsDashboardWriteOptions() {
    boolean absolute = false;
    boolean debug = false;
    String absRoot = "";
    String scheme = "";
    return new CdfRunJsDashboardWriteOptions( absolute, debug, absRoot, scheme );
  }
}
