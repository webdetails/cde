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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class BootstrapColumnRenderTest {

  private BootstrapColumnRenderForTest renderForTest;

  private static class BootstrapColumnRenderForTest extends BootstrapColumnRender {

    private final Map<String, String> properties = new HashMap<>();

    public BootstrapColumnRenderForTest( JXPathContext context) {
      super( context );
    }

    @Override
    protected String getPropertyString( String prop ) {
      String value = this.properties.get( prop );
      return value != null ? value : "";
    }

    @Override
    protected boolean lastColumn() {
      return false;
    }

    private void addProperty( String prop, String value ) {
      this.properties.put( prop, value );
    }

    private void setPropertyBagClass( String css ) {
      getPropertyBag().addClass( css );
    }
  }

  @Before
  public void setUp() throws Exception {
    JXPathContext context = mock( JXPathContext.class );
    renderForTest = new BootstrapColumnRenderForTest( context );
  }

  @Test
  public void testRenderStartNoCss() {
    renderForTest.processProperties();
    String div = renderForTest.renderStart();

    assertEquals( "<div class='col-xs-12'><div >", div );
  }

  @Test
  public void testRenderStartWithBootstrapCss() {

    renderForTest.addProperty( "bootstrapExtraSmall", "5" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    assertEquals( "<div class='col-xs-5'><div >", div );
  }

  @Test
  public void testRenderStartWithPropertyBag() {

    renderForTest.setPropertyBagClass( "span-6" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    assertEquals( "<div class='col-xs-12'><div  class='span-6 ' >", div );
  }

  @Test
  public void testRenderStartWithAll() {

    renderForTest.setPropertyBagClass( "span-6" );
    renderForTest.addProperty( "bootstrapExtraSmall", "5" );
    renderForTest.processProperties();

    String div = renderForTest.renderStart();

    assertEquals( "<div class='col-xs-5'><div  class='span-6 ' >", div );
  }

  @Test
  public void testRenderClose() {
    String div = renderForTest.renderClose();
    assertEquals( "</div></div>", div );
  }
}
