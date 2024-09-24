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

package pt.webdetails.cdf.dd.util;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import static org.junit.Assert.assertEquals;

public class XPathUtilsTest {

  private static final String WCDF_AUTHOR = "dummyAuthor";
  private static final String WCDF_DESCRIPTION = "dummyDescription";
  private static final String WCDF_RENDERER_TYPE = "dummyRendererType";
  private static final String WCDF_STYLE = "dummyStyle";
  private static final String WCDF_TITLE = "dummyTitle";

  @Test
  public void testGetStringValue() throws JSONException {
    JSONObject json = new JSONObject();

    json.put( "settings", getDummyDashboardWcdfDescriptor().toJSON() );
    JXPathContext node = JsonUtils.toJXPathContext( json );

    assertEquals( WCDF_AUTHOR, XPathUtils.getStringValue( node, "//author" ) );
    assertEquals( WCDF_DESCRIPTION, XPathUtils.getStringValue( node, "//description" ) );
    assertEquals( WCDF_RENDERER_TYPE, XPathUtils.getStringValue( node, "//rendererType" ) );
    assertEquals( WCDF_STYLE, XPathUtils.getStringValue( node, "//style" ) );
    assertEquals( WCDF_TITLE, XPathUtils.getStringValue( node, "//title" ) );
    assertEquals( "false", XPathUtils.getStringValue( node, "//widget" ) );
  }

  private DashboardWcdfDescriptor getDummyDashboardWcdfDescriptor() {
    DashboardWcdfDescriptor wcdf = new DashboardWcdfDescriptor();
    wcdf.setAuthor( WCDF_AUTHOR );
    wcdf.setDescription( WCDF_DESCRIPTION );
    wcdf.setRendererType( WCDF_RENDERER_TYPE );
    wcdf.setStyle( WCDF_STYLE );
    wcdf.setTitle( WCDF_TITLE );
    wcdf.setWidget( false );
    return wcdf;
  }
}
