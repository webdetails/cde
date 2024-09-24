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

import static org.junit.Assert.assertEquals;

public class ColumnRenderTest {

  private ColumnRenderForTest renderForTest;

  private static class ColumnRenderForTest extends ColumnRender {

    String testRenderType;

    public ColumnRenderForTest( JXPathContext context ) {
      super( context );
    }

    @Override
    protected boolean lastColumn() {
      return false;
    }

    @Override
    protected String getRenderType() {
      return testRenderType;
    }

    private void setTestRenderType( String type ) {
      this.testRenderType = type;
    }

  }

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new ColumnRenderForTest( context );
  }

   @Test
   public void testBootstrapRender() {
     renderForTest.setTestRenderType( "bootstrap" );
     renderForTest.addColSpan( "5" );
     String div = renderForTest.renderStart();

     assertEquals( "<div  class='col-md-5 ' >", div );
   }

  @Test
  public void testBlueprintRender() {
    renderForTest.setTestRenderType( "blueprint" );
    renderForTest.addColSpan( "5" );
    String div = renderForTest.renderStart();

    assertEquals( "<div  class='span-5 ' >", div );
  }
}
