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
