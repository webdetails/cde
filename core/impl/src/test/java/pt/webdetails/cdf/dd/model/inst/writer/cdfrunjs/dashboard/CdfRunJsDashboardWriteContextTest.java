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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cdf.dd.CdeEngineForTests;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.amd.CdfRunJsThingWriterFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CdfRunJsDashboardWriteContextTest {
  private static CdfRunJsThingWriterFactory factory;

  private static final String indent = StringUtils.EMPTY;
  private static final boolean bypassCacheRead = true;

  @BeforeClass
  public static void setUp() throws Exception {
    ICdeEnvironment mockedEnvironment = mock( ICdeEnvironment.class );
    when( mockedEnvironment.getApplicationBaseContentUrl() ).thenReturn( "/pentaho/plugin/pentaho-cdf-dd" );
    new CdeEngineForTests( mockedEnvironment );

    factory = new CdfRunJsThingWriterFactory();
  }

  @Test
  public void testGetRoot() {
    // test an absolute path
    CdfRunJsDashboardWriteOptions options = new CdfRunJsDashboardWriteOptions(
        true,
        false,
        "localhost:8080",
        "http" );
    CdfRunJsDashboardWriteContext context = new CdfRunJsDashboardWriteContextForTesting(
        factory,
        indent,
        bypassCacheRead,
        options );

    assertEquals( "http://localhost:8080/pentaho/plugin/pentaho-cdf-dd", context.getRoot() );

    // test fallback to relative path if options.absRoot is invalid
    options = new CdfRunJsDashboardWriteOptions(
      true,
      false,
      "",
      "http" );
    context = new CdfRunJsDashboardWriteContextForTesting(
      factory,
      indent,
      bypassCacheRead,
      options );

    assertEquals( "/pentaho/plugin/pentaho-cdf-dd", context.getRoot() );

    // setup context for testing a relative path
    options = new CdfRunJsDashboardWriteOptions(
      false,
      false,
      null,
      "http" );
    context = new CdfRunJsDashboardWriteContextForTesting(
      factory,
      indent,
      bypassCacheRead,
      options );

    assertEquals( "/pentaho/plugin/pentaho-cdf-dd", context.getRoot() );
  }
}
