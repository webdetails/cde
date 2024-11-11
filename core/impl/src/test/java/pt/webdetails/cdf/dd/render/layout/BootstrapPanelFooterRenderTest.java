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
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class BootstrapPanelFooterRenderTest {

  private BootstrapPanelFooterRender renderForTest;

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new BootstrapPanelFooterRender( context );
    renderForTest.processProperties();
  }

  @Test
  public void testRenderStart() {
    String div = renderForTest.renderStart();

    assertEquals( "<div  class='panel-footer ' >", div );
  }

  @Test
  public void testRenderClose() {
    String div = renderForTest.renderClose();

    assertEquals( "</div>", div );
  }
}
