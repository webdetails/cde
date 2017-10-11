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

package pt.webdetails.cdf.dd.util;

import junit.framework.Assert;
import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

public class XPathUtilsTest {

  private final String WCDF_AUTHOR = "dummyAuthor";
  private final String WCDF_DESCRIPTION = "dummyDescription";
  private final String WCDF_RENDERER_TYPE = "dummyRendererType";
  private final String WCDF_STYLE = "dummyStyle";
  private final String WCDF_TITLE = "dummyTitle";

  @Test
  public void testGetStringValue() throws JSONException {
    JSONObject json = new JSONObject();

    json.put( "settings", getDummyDashboardWcdfDescriptor().toJSON() );
    JXPathContext node = JsonUtils.toJXPathContext( json );

    Assert.assertEquals( XPathUtils.getStringValue( node, "//author" ), WCDF_AUTHOR );
    Assert.assertEquals( XPathUtils.getStringValue( node, "//description" ), WCDF_DESCRIPTION );
    Assert.assertEquals( XPathUtils.getStringValue( node, "//rendererType" ), WCDF_RENDERER_TYPE );
    Assert.assertEquals( XPathUtils.getStringValue( node, "//style" ), WCDF_STYLE );
    Assert.assertEquals( XPathUtils.getStringValue( node, "//title" ), WCDF_TITLE );
    Assert.assertEquals( XPathUtils.getStringValue( node, "//widget" ), "false" );
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
