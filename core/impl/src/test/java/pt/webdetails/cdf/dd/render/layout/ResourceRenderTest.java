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

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeConstants;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ResourceRenderTest {
  ResourceRender resourceRender;

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    resourceRender = new ResourceRender( context );
  }

  @Test
  public void testRenderStart() {
    // CSS file
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "test.css" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.CSS );
    assertEquals( "<link rel=\"stylesheet\" type=\"text/css\" href=\"test.css\" />", resourceRender.renderStart() );

    // JS file
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "test.js" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.JAVASCRIPT );
    assertEquals( "<script language=\"javascript\" type=\"text/javascript\" src=\"test.js\"></script>",
      resourceRender.renderStart() );

    // clear previous return values
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_FILE ) ).thenReturn( "" );

    // CSS code snippet
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_CODE ) ).thenReturn( "div {color: inherit;}" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.CSS );
    assertEquals( "<style>\n<!--\ndiv {color: inherit;}\n-->\n</style>", resourceRender.renderStart() );

    // JS code snippet
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_CODE ) ).thenReturn( "(function() { return; })();" );
    when( resourceRender.getPropertyString( CdeConstants.RESOURCE_TYPE ) ).thenReturn( CdeConstants.JAVASCRIPT );
    assertEquals( "<script language=\"javascript\" type=\"text/javascript\">\n(function() { return; })();\n</script>",
      resourceRender.renderStart() );
  }
}
