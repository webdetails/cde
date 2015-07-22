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

package pt.webdetails.cdf.dd.cdf;

import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

public class CdfTemplatesTest {

  private static final String TEST_WCDF_FILE = "test-resources/test/dashboard4Testing.wcdf";
  private static final String TEST_RENDERER = "bootstrap"; // defined within dashboard4Testing.wcdf
  private static final String TEST_STYLE = "WDDocs"; // defined within dashboard4Testing.wcdf

  DashboardWcdfDescriptor mockDashboardWcdfDescriptor = new DashboardWcdfDescriptor();

  @Before
  public void setUp() {

  }

  @Test
  public void testStyleAndRendererInclusionInTemplate() throws Exception {


    mockDashboardWcdfDescriptor.setRendererType( TEST_RENDERER );
    mockDashboardWcdfDescriptor.setStyle( TEST_STYLE );

    JSONObject testJson = new JSONObject();
    testJson.put( "filename", TEST_WCDF_FILE );


    String result =
      new CdfTemplatesForTesting( "", mockDashboardWcdfDescriptor )
        .addDashboardStyleAndRendererTypeToTemplate( testJson.toString( 2 ) );
    Assert.assertTrue( result != null );

    JSONObject resultJson = new JSONObject( result );
    Assert.assertTrue( resultJson != null );
    Assert.assertTrue( resultJson.getString( "style" ) != null );
    Assert.assertTrue( resultJson.getString( "rendererType" ) != null );

    Assert.assertTrue( TEST_STYLE.equals( resultJson.getString( "style" ) ) );
    Assert.assertTrue( TEST_RENDERER.equals( resultJson.getString( "rendererType" ) ) );
  }

  @After
  public void tearDown() {

  }

}
