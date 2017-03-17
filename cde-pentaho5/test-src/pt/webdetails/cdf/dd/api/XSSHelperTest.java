/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class XSSHelperTest {

  private final String XSS = "<html><script>alert(1);</script></html>";

  @Test
  public void escape() throws Exception {
    assertFalse( XSS.equals( XSSHelper.getInstance().escape( XSS ) ) );
  }

  @Test
  public void escapeEmpty() throws Exception {
    assertEquals( "", XSSHelper.getInstance().escape( "" ) );
  }

  @Test
  public void escapeNull() throws Exception {
    assertEquals( null, XSSHelper.getInstance().escape( null ) );
  }

}
