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
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  private static CdfRunRequireJsDashboardWriter dashboardWriter;

  @Before
  public void setUp() throws Exception {
    dashboardWriter = new CdfRunRequireJsBlueprintDashboardWriter();
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
    dashboardResult.append( REQUIRE_START )
        .append( "['" + StringUtils.join( cdfRequirePaths, "', '" ) + "']" + "," ).append( NEWLINE )
        .append( "function(" + StringUtils.join( componentClassNames, ", " ) + ") {" ).append( NEWLINE )
        .append( "var dashboard = new Dashboard();" ).append( NEWLINE )
        .append( "fakeContent" ).append( NEWLINE )
        .append( EPILOGUE ).append( NEWLINE )
        .append( REQUIRE_STOP );

    Assert.assertEquals( out, dashboardResult.toString() );

  }

}
