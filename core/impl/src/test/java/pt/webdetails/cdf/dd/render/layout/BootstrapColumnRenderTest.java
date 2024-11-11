/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
