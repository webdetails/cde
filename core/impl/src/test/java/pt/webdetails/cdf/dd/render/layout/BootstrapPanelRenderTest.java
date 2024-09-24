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

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class BootstrapPanelRenderTest {

  private BootstrapPanelRenderForTest renderForTest;

  private static class BootstrapPanelRenderForTest extends BootstrapPanelRender {

    private String bootstrapPanelStyle = "";

    public BootstrapPanelRenderForTest( JXPathContext context ) {
      super( context );
    }

    @Override
    protected String getBootstrapPanelStyle() {
      return this.bootstrapPanelStyle;
    }

    protected void setBootstrapPanelStyle( String style ) {
      this.bootstrapPanelStyle = style;
    }
  }

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new BootstrapPanelRenderForTest( context );
  }

  @Test
  public void testStartRenderSimple() {
    renderForTest.processProperties();
    String div = renderForTest.renderStart();

    assertEquals( "<div  class='panel ' >", div );
  }

  @Test
  public void testStartRenderWithExtraPanelStyle() {
    renderForTest.setBootstrapPanelStyle( "panel-info" );
    renderForTest.processProperties();
    String div = renderForTest.renderStart();

    assertEquals( "<div  class='panel panel-info ' >", div );
  }

  @Test
  public void testRenderClose() {
    String div = renderForTest.renderClose();
    assertEquals( "</div>", div );
  }
}
