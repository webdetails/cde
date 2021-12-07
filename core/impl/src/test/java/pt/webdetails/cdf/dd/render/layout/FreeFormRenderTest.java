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
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class FreeFormRenderTest {

  private FreeFormRenderForTest renderForTest;
  private static class FreeFormRenderForTest extends FreeFormRender {

    private String properties;
    public FreeFormRenderForTest( JXPathContext context ) {
      super( context );
      this.properties = "[]";
    }

    @Override
    protected String getMoreProperties() {
      return properties;
    }

    private void setProperties( String info ) {
      this.properties = info;
    }

    private void setPropertyBag( String css ) {
      getPropertyBag().addClass( css );
    }
  }

  @Before
  public void setUp() throws Exception {
    JXPathContext context = Mockito.mock( JXPathContext.class );
    renderForTest = new FreeFormRenderForTest( context );
    renderForTest.processProperties();
    renderForTest.setElementTag( "select" );
  }

  @Test
  public void testRenderStartWithTagOnly() throws JSONException {
    String select = renderForTest.renderStart();

    assertEquals( "<select >", select );
  }

  @Test
  public void testRenderStartWithMorePropertiesSimple() throws JSONException {
    renderForTest.setProperties( "[[\"arg1\",\"value1\"],[\"arg2\",\"value2\"]]" );
    String select = renderForTest.renderStart();

    assertEquals( "<select  arg1='value1' arg2='value2'>", select );
  }

  @Test
  public void testRenderStartWithMorePropertiesComplex() throws JSONException {
    renderForTest.setProperties( "[['arg1','{{[ \"value11\", \"value12\"]}}'],"
        + "['arg2', \"{{[ 'value21', 'value22']}}\"]]" );
    String select = renderForTest.renderStart();

    assertEquals( "<select  arg1='{{[ \"value11\", \"value12\"]}}' arg2=\"{{[ 'value21', 'value22']}}\">",
        select );
  }

  @Test
  public void testRenderStartWithPropertyBag() throws JSONException {
    renderForTest.setPropertyBag( "clear" );
    String select = renderForTest.renderStart();

    assertEquals( "<select  class='clear ' >", select );
  }

  @Test
  public void testRenderStartWithAll() throws JSONException {
    renderForTest.setPropertyBag( "clear" );
    renderForTest.setProperties( "[[\"arg1\",\"value1\"],[\"arg2\",\"value2\"]]" );
    String select = renderForTest.renderStart();

    assertEquals( "<select  class='clear '  arg1='value1' arg2='value2'>", select );
  }

  @Test
  public void testRenderClose() {
    String select = renderForTest.renderClose();

    assertEquals( "</select>", select );
  }
}
