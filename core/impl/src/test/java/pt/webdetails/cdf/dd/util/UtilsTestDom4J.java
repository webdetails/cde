/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cpf.utils.XmlDom4JUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTestDom4J {

  private Document document;
  private Element element;

  private final String settings = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    + "<settings>\n"
    + "        <cache>false</cache>\n"
    + "        <cache-messages>true</cache-messages>\n"
    + "        <max-age>2628001</max-age>\n"
    + "</settings>\n";

  @Before
  public void setup() {
    InputStream is = new ByteArrayInputStream( settings.getBytes() );
    document = XmlDom4JUtils.getDocumentFromStream( is );
    element = (Element) document.selectSingleNode( "/settings" );
  }

  @Test
  public void selectElementsWithElement() {
    List<Element> result = Utils.selectElements( element, "/settings/cache" );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ) instanceof Element );
    assertEquals( "cache", result.get( 0 ).getName() );
    assertEquals( "false", result.get( 0 ).getText() );
  }

  @Test
  public void selectElementsWithDocument() {
    List<Element> result = Utils.selectElements( document, "/settings/cache" );
    assertEquals( 1, result.size() );
    assertTrue( result.get( 0 ) instanceof Element );
    assertEquals( "cache", result.get( 0 ).getName() );
    assertEquals( "false", result.get( 0 ).getText() );
  }
}
