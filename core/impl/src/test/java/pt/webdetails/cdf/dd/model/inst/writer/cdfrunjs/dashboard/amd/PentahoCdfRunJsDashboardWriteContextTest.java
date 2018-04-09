/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Test;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext.RESOURCE_API_GET;
import static pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContextForTesting.PLUGIN_ID;
import static pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContextForTesting.SYSTEM_DIR;
import static pt.webdetails.cpf.repository.util.RepositoryHelper.joinPaths;

public class PentahoCdfRunJsDashboardWriteContextTest {

  private static final String ROOT = "src/test/resources";

  private static final String TEST_FOLDER = "test";
  private static final String TEST_SPACED_FOLDER = "test folder";

  private static final String DASHBOARD = "testDashboard.wcdf";
  private static final String SYSTEM = "system";
  private static final String TEST_PLUGIN = "test-plugin";

  private static final String CDE_PLUGIN_URL = "pentaho/plugin/pentaho-cdf-dd";

  private static final String JS_RESOURCE = "script.js";
  private static final String CSS_RESOURCE = "style.css";
  private static final String DIR_RESOURCE = "a_folder/";
  private static final String IMG_RESOURCE = "image.jpg";

  private static final String SERVER_SCHEME = "http";
  private static final String SERVER_HOST = "localhost:8080";

  private PentahoCdfRunJsDashboardWriteContext context;

  // region Replace dashboard path token
  //   DASHBOARD_PATH_TAG = "\\$\\{dashboardPath\\}";
  @Test
  public void testReplaceTokensDashboardPath() {
    this.context = createDashboardContext();

    String expected = joinPaths( ROOT, TEST_FOLDER, "/" );
    assertReplaceTokens( expected, "${dashboardPath}" );
  }

  @Test
  public void testReplaceTokensDashboardPath_withWhiteSpaces() {
    String dashboardPath = joinPaths( ROOT, TEST_SPACED_FOLDER, DASHBOARD ).substring( 1 );
    this.context = createDashboardContext( dashboardPath );

    String expected = joinPaths( ROOT, TEST_SPACED_FOLDER.replace( " ", "%20") , "/" );
    assertReplaceTokens( expected, "${dashboardPath}" );
  }
  // endregion

  // region Build the resource links
  //   ABS_RES_TAG = "\\$\\{(res|solution):(/.+)\\}";
  //   REL_RES_TAG = "\\$\\{(res|solution):(.+)\\}";
  @Test
  public void testReplaceTokensRelativeResourceLink() {
    this.context = createDashboardContext();

    String expected = joinPaths( ROOT, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + JS_RESOURCE + "}" );
    assertReplaceTokens( expected, "${res:" + JS_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensRelativeResourceLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String expected = joinPaths( ROOT, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + JS_RESOURCE + "}" );
    assertReplaceTokens( expected, "${res:" + JS_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteResourceLink() {
    this.context = createDashboardContext();

    String absoluteResourcePath = joinPaths( "/", TEST_FOLDER, JS_RESOURCE );
    String expected = joinPaths( "/", TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + absoluteResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + absoluteResourcePath + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteResourceLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String absoluteResourcePath = joinPaths( "/", TEST_FOLDER, JS_RESOURCE );
    String expected = joinPaths( "/", TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + absoluteResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + absoluteResourcePath + "}" );
  }
  // endregion

  // region Build image links
  //   ABS_IMG_TAG = "\\$\\{img:(/.+)\\}";
  //   REL_IMG_TAG = "\\$\\{img:(.+)\\}";
  @Test
  public void testReplaceTokensRelativeImageLink() {
    this.context = createDashboardContext();

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, ROOT, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    assertReplaceTokens( expected, "${img:" + IMG_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensRelativeImageLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, ROOT, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    assertReplaceTokens( expected, "${img:" + IMG_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteImageLink() {
    this.context = createDashboardContext();

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = joinPaths(
      "/", CDE_PLUGIN_URL, RESOURCE_API_GET, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    String resourcePath = joinPaths( "/", TEST_FOLDER, IMG_RESOURCE );
    assertReplaceTokens( expected, "${img:" + resourcePath + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteImageLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      "/", CDE_PLUGIN_URL, RESOURCE_API_GET, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    String resourcePath = joinPaths( "/", TEST_FOLDER, IMG_RESOURCE );
    assertReplaceTokens( expected, "${img:" + resourcePath + "}" );
  }
  // endregion

  // region Build system resource links
  //   ABS_SYS_RES_TAG = "\\$\\{system:(/.+)\\}";
  //   REL_SYS_RES_TAG = "\\$\\{system:(.+)\\}";
  @Test
  public void testReplaceTokensRelativeSystemResource() {
    this.context = createDashboardContext( false, true );

    String expected = joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    assertReplaceTokens( expected, "${system:" + CSS_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensRelativeSystemResource_absoluteWriteOptions() {
    this.context = createDashboardContext( true, true );

    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    assertReplaceTokens( expected, "${system:" + CSS_RESOURCE + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteSystemResource() {
    this.context = createDashboardContext( false, true );

    String expected = joinPaths(
      "/", CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    String resourcePath = joinPaths( "/", CSS_RESOURCE );
    assertReplaceTokens( expected, "${system:" + resourcePath + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteSystemResource_absoluteWriteOptions() {
    this.context = createDashboardContext( true, true );

    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      "/", CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    String resourcePath = joinPaths( "/", CSS_RESOURCE );
    assertReplaceTokens( expected, "${system:" + resourcePath + "}" );
  }
  // endregion

  // region Build directory links
  //   ABS_DIR_RES_TAG = "\\$\\{(res|solution):(/.+/)\\}";
  //   REL_DIR_RES_TAG = "\\$\\{(res|solution):(.+/)\\}";
  @Test
  public void testReplaceTokensRelativeDirectoryLink() {
    this.context = createDashboardContext();

    String relativeResourcePath = DIR_RESOURCE;
    String expected = joinPaths( ROOT, TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + relativeResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + relativeResourcePath + "}" );
  }

  @Test
  public void testReplaceTokensRelativeDirectoryLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String relativeResourcePath = DIR_RESOURCE;
    String expected = joinPaths( ROOT, TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + relativeResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + relativeResourcePath + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteDirectoryLink() {
    this.context = createDashboardContext();

    String absoluteResourcePath = joinPaths( "/", TEST_FOLDER, DIR_RESOURCE );
    String expected = joinPaths( TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + absoluteResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + absoluteResourcePath + "}" );
  }

  @Test
  public void testReplaceTokensAbsoluteDirectoryLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String absoluteResourcePath = joinPaths( "/", TEST_FOLDER, DIR_RESOURCE );
    String expected = joinPaths( TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, "${solution:" + absoluteResourcePath + "}" );
    assertReplaceTokens( expected, "${res:" + absoluteResourcePath + "}" );
  }
  // endregion

  private void assertReplaceTokens( String expected, String content ) {
    assertEquals( expected, this.context.replaceTokens( content ) );
  }

  // region unit test aux methods
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
      // this is needed because we need to remove the src/test/resources path used to get the file
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

  private String getDashboardPath() {
    return joinPaths( ROOT, TEST_FOLDER, DASHBOARD ).substring( 1 );
  }

  private String getDashboardSystemPath() {
    return joinPaths( ROOT, SYSTEM, TEST_PLUGIN, TEST_FOLDER, DASHBOARD ).substring( 1 );
  }

  private CdfRunJsDashboardWriteOptions getCdfRunJsDashboardWriteOptions( boolean isAbsolute ) {
    final boolean isDebug = false;

    return new CdfRunJsDashboardWriteOptions( isAbsolute, isDebug, SERVER_HOST, SERVER_SCHEME );
  }

  private PentahoCdfRunJsDashboardWriteContext createDashboardContext() {
    return createDashboardContext( false, false );
  }

  private PentahoCdfRunJsDashboardWriteContext createDashboardContext( String dashboardPath ) {
    return createDashboardContext( dashboardPath, false, false );
  }

  private PentahoCdfRunJsDashboardWriteContext createDashboardContext( boolean isAbsoluteWrite, boolean isSystem ) {
    final String dashboardPath = isSystem ? getDashboardSystemPath() : getDashboardPath();

    return createDashboardContext( dashboardPath, isAbsoluteWrite, isSystem );
  }

  private PentahoCdfRunJsDashboardWriteContext createDashboardContext( String dashboardPath,
                                                                       boolean isAbsoluteWrite, boolean isSystem ) {
    final String indent = "";
    final boolean bypassCacheRead = true;

    final Dashboard dashboard = getDashboard( dashboardPath, isSystem );

    final CdfRunJsDashboardWriteOptions options = getCdfRunJsDashboardWriteOptions( isAbsoluteWrite );
    final IThingWriterFactory factory = new CdfRunJsThingWriterFactory();

    return new PentahoCdfRunJsDashboardWriteContextForTesting( factory, indent, bypassCacheRead, dashboard, options );
  }
  // endregion
}
