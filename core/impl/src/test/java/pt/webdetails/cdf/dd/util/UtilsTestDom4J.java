/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
