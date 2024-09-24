/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
package pt.webdetails.cdf.dd.api;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.CdeConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class XSSHelperTest {

  private static final String XSS = "<html><script>alert(1);</script></html>";

  private static final String JSON_OBJECT_XSS = "{\"foo\": \"bar\", \"xss\": \"" + XSS + "\"}";
  private static final String JSON_ARRAY_XSS = "[\"foo\", \"" + XSS +"\"]";


  @Before
  public void setUp() {
    XSSHelper helper = spy( new XSSHelper() );

    doReturn( "true" ).when( helper ).getXssEscapingPluginSetting();

    XSSHelper.setInstance( helper );
  }

  @Test
  public void testEscape() {
    final String actual = XSSHelper.getInstance().escape( XSS );

    assertXssSafe( actual );
  }

  @Test
  public void testEscape_jsonObject() {
    final String actual = XSSHelper.getInstance().escape( JSON_OBJECT_XSS );

    assertIsIndented( actual );
    assertXssSafe( actual );
  }

  @Test
  public void testEscape_jsonArray() {
    final String actual = XSSHelper.getInstance().escape( JSON_ARRAY_XSS );

    assertIsIndented( actual );
    assertXssSafe( actual );
  }

  @Test
  public void testEscape_empty() {
    assertEquals( "", XSSHelper.getInstance().escape( "" ) );
  }

  @Test
  public void testEscape_null() {
    assertNull( XSSHelper.getInstance().escape( null ) );
  }

  // region aux methods
  private void assertIsIndented( String value ) {
    assertTrue( value.contains( CdeConstants.Writer.NEWLINE ) );
  }

  private void assertXssSafe( String value ) {
    assertFalse( value.contains( XSS ) );

    assertTrue( StringEscapeUtils.unescapeJava( value ).contains( XSS ) );
  }
  // endregion
}
