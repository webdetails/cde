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

import org.junit.Test;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContextForTesting;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory;

import static org.junit.Assert.assertEquals;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.SLASH;
import static pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext.RESOURCE_API_GET;
import static pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.amd.PentahoCdfRunJsDashboardWriteContext.*;
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

  // region Replace Dashboard Path Token
  //   DASHBOARD_PATH_TAG = "\\$\\{dashboardPath\\}";
  @Test
  public void testReplaceTokensDashboardPath() {
    this.context = createDashboardContext();

    String expected = joinPaths( ROOT, TEST_FOLDER, SLASH );
    assertReplaceTokens( expected, "${" + DASH_PATH_TAG + "}" );
  }

  @Test
  public void testReplaceTokensDashboardPath_withWhiteSpaces() {
    String dashboardPath = joinPaths( ROOT, TEST_SPACED_FOLDER, DASHBOARD ).substring( 1 );
    this.context = createDashboardContext( dashboardPath );

    String expected = joinPaths( ROOT, TEST_SPACED_FOLDER.replace( " ", "%20") , SLASH );
    assertReplaceTokens( expected, "${" + DASH_PATH_TAG + "}" );
  }

  @Test
  public void testReplaceTokensDashboardPath_withWhiteSpacesWithTwoDashes() {
    String dashboardPath = joinPaths( ROOT, TEST_SPACED_FOLDER, DASHBOARD ).substring( 1 );
    this.context = createDashboardContext( dashboardPath );

    String expected = joinPaths( ROOT, TEST_SPACED_FOLDER.replace( " ", "%20") , SLASH );
    expected = expected + " - " + expected;
    assertReplaceTokens( expected, "${" + DASH_PATH_TAG + "} - ${" + DASH_PATH_TAG + "}" );
  }
  // endregion

  // region Replace Resource Token
  //   RESOURCE_TOKEN = "\\$\\{(res|solution):((/?)(.+?)(/?))\\}"
  @Test
  public void testReplaceTokensRelativeResourceLink() {
    this.context = createDashboardContext();

    String expected = joinPaths( ROOT, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, JS_RESOURCE ) );
    assertReplaceTokens( expected, getContent( RES_TAG, JS_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensRelativeResourceLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String expected = joinPaths( ROOT, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, JS_RESOURCE ) );
    assertReplaceTokens( expected, getContent( RES_TAG, JS_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensAbsoluteResourceLink() {
    this.context = createDashboardContext();

    String absoluteResourcePath = joinPaths( SLASH, TEST_FOLDER, JS_RESOURCE );
    String expected = joinPaths( SLASH, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, absoluteResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, absoluteResourcePath ) );
  }

  @Test
  public void testReplaceTokensAbsoluteResourceLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String absoluteResourcePath = joinPaths( SLASH, TEST_FOLDER, JS_RESOURCE );
    String expected = joinPaths( SLASH, TEST_FOLDER, JS_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, absoluteResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, absoluteResourcePath ) );
  }
  // endregion

  // region Replace Image Resource Token
  //   RESOURCE_TOKEN = "\\$\\{(img):((/?)(.+?)(/?))\\}"
  @Test
  public void testReplaceTokensRelativeImageLink() {
    this.context = createDashboardContext();

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, ROOT, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    assertReplaceTokens( expected, getContent( IMAGE_TAG, IMG_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensRelativeDirectoryLink_absoluteWriteOptionsWithManyTokens() {
    this.context = createDashboardContext( );

    Long timestamp = this.context.getWriteDate().getTime();
    String base = joinPaths(ROOT, TEST_FOLDER, IMG_RESOURCE);

    String expected = "$ {img:" + base + "} = ";
    String url = joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, ROOT, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );
    expected+= url + "<br><img src=\"" + url + "\"><br>";

    assertReplaceTokens( expected,"$ {img:"  + base + "} = ${img:"  + base + "}<br><img src=\"${img:" + base + "}\"><br>" );
  }

  @Test
  public void testReplaceTokensRelativeImageLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, ROOT, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    assertReplaceTokens( expected, getContent( IMAGE_TAG, IMG_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensAbsoluteImageLink() {
    this.context = createDashboardContext();

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = joinPaths(
      SLASH, CDE_PLUGIN_URL, RESOURCE_API_GET, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    String resourcePath = joinPaths( SLASH, TEST_FOLDER, IMG_RESOURCE );
    assertReplaceTokens( expected, getContent( IMAGE_TAG, resourcePath ) );
  }

  @Test
  public void testReplaceTokensAbsoluteImageLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    Long timestamp = this.context.getWriteDate().getTime();
    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      SLASH, CDE_PLUGIN_URL, RESOURCE_API_GET, TEST_FOLDER, IMG_RESOURCE + "?v=" + timestamp
    );

    String resourcePath = joinPaths( SLASH, TEST_FOLDER, IMG_RESOURCE );
    assertReplaceTokens( expected, getContent( IMAGE_TAG, resourcePath ) );
  }
  // endregion

  // region Replace System Resource Token
  //   RESOURCE_TOKEN = "\\$\\{(system):((/?)(.+?)(/?))\\}"
  @Test
  public void testReplaceTokensRelativeSystemResource() {
    this.context = createDashboardContext( false, true );

    String expected = joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    assertReplaceTokens( expected, getContent( SYSTEM_TAG, CSS_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensRelativeSystemResource_absoluteWriteOptions() {
    this.context = createDashboardContext( true, true );

    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    assertReplaceTokens( expected, getContent( SYSTEM_TAG, CSS_RESOURCE ) );
  }

  @Test
  public void testReplaceTokensAbsoluteSystemResource() {
    this.context = createDashboardContext( false, true );

    String expected = joinPaths(
      SLASH, CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    String resourcePath = joinPaths( SLASH, CSS_RESOURCE );
    assertReplaceTokens( expected, getContent( SYSTEM_TAG, resourcePath ) );
  }

  @Test
  public void testReplaceTokensAbsoluteSystemResource_absoluteWriteOptions() {
    this.context = createDashboardContext( true, true );

    String expected = SERVER_SCHEME + "://" + SERVER_HOST + joinPaths(
      SLASH, CDE_PLUGIN_URL, RESOURCE_API_GET, SYSTEM_DIR, PLUGIN_ID, CSS_RESOURCE
    );

    String resourcePath = joinPaths( SLASH, CSS_RESOURCE );
    assertReplaceTokens( expected, getContent( SYSTEM_TAG, resourcePath ) );
  }
  // endregion

  // region Replace Directory Resource Token
  //   RESOURCE_TOKEN = "\\$\\{(res|solution):((/?)(.+?)(/))\\}"
  @Test
  public void testReplaceTokensRelativeDirectoryLink() {
    this.context = createDashboardContext();

    String relativeResourcePath = DIR_RESOURCE;
    String expected = joinPaths( ROOT, TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, relativeResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, relativeResourcePath ) );
  }

  @Test
  public void testReplaceTokensRelativeDirectoryLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String relativeResourcePath = DIR_RESOURCE;
    String expected = joinPaths( ROOT, TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, relativeResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, relativeResourcePath ) );
  }

  @Test
  public void testReplaceTokensAbsoluteDirectoryLink() {
    this.context = createDashboardContext();

    String absoluteResourcePath = joinPaths( SLASH, TEST_FOLDER, DIR_RESOURCE );
    String expected = joinPaths( TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, absoluteResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, absoluteResourcePath ) );
  }

  @Test
  public void testReplaceTokensAbsoluteDirectoryLink_absoluteWriteOptions() {
    this.context = createDashboardContext( true, false );

    String absoluteResourcePath = joinPaths( SLASH, TEST_FOLDER, DIR_RESOURCE );
    String expected = joinPaths( TEST_FOLDER, DIR_RESOURCE );

    assertReplaceTokens( expected, getContent( SOLUTION_TAG, absoluteResourcePath ) );
    assertReplaceTokens( expected, getContent( RES_TAG, absoluteResourcePath ) );
  }
  // endregion

  private void assertReplaceTokens( String expected, String content ) {
    assertEquals( expected, this.context.replaceTokens( content ) );
  }

  // region unit test aux methods
  private String getContent( String tag, String path ) {
    return "${" + tag + ":" + path + "}";
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

    final Dashboard dashboard = CdfRunJsDashboardWriteContextForTesting.getDashboard( dashboardPath, isSystem );

    final CdfRunJsDashboardWriteOptions options = getCdfRunJsDashboardWriteOptions( isAbsoluteWrite );
    final IThingWriterFactory factory = new CdfRunJsThingWriterFactory();

    return new PentahoCdfRunJsDashboardWriteContextForTesting( factory, indent, bypassCacheRead, dashboard, options );
  }
  // endregion
}
