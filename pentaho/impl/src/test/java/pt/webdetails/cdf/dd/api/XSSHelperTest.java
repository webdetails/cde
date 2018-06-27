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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class XSSHelperTest {

  private final String XSS = "<html><script>alert(1);</script></html>";
  private static XSSHelper xssHelper;

  @BeforeClass
  public static void setUp() {
    xssHelper = new XSSHelper() {
      @Override
      protected String getXssEscapingPluginSetting() {
        return "true";
      }
    };
  }

  @Test
  public void escape() {
    assertNotEquals( XSS, xssHelper.escape( XSS ) );
  }

  @Test
  public void escapeEmpty() {
    assertEquals( "", xssHelper.escape( "" ) );
  }

  @Test
  public void escapeNull() {
    assertNull( xssHelper.escape( null ) );
  }

}
