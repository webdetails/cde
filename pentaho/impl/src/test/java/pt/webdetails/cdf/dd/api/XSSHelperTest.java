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
