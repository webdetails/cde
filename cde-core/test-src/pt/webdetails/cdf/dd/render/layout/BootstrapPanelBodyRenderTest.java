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

package pt.webdetails.cdf.dd.render.layout;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BootstrapPanelBodyRenderTest extends TestCase {

  private BootstrapPanelBodyRender renderForTest;

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new BootstrapPanelBodyRender( context );
    renderForTest.processProperties();
  }

  @Test
  public void testRenderStart() {
    String div = renderForTest.renderStart();

    Assert.assertEquals( "<div  class='panel-body ' >", div );
  }

  @Test
  public void testRenderClose() {
    String div = renderForTest.renderClose();

    Assert.assertEquals( "</div>", div );
  }

}
