/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
