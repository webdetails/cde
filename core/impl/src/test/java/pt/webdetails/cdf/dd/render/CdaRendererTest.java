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


package pt.webdetails.cdf.dd.render;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CdaRendererTest {
  CdaRenderer cdaRenderer;

  private static final String CDFDE_FILE = "src/test/resources/datasources/scripting/test-scripting.cdfde";

  private static Document stringToDom( String xmlSource ) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse( new InputSource( new StringReader( xmlSource ) ) );
  }

  @Before
  public void setup() throws IOException, JSONException {
    cdaRenderer = new CdaRendererForTesting( FileUtils.readFileToString( new File( CDFDE_FILE ) ) );
  }

  @After
  public void tearDown() throws Exception {
    cdaRenderer = null;
  }

  @Test
  public void testRender() throws Exception {
    // this is the .cda file content ( that would have been saved / stored )
    Document dom = stringToDom( cdaRenderer.render() );

    NodeList cDADescriptor = dom.getElementsByTagName( "CDADescriptor" );
    assertNotNull( cDADescriptor );
    assertEquals( 1, cDADescriptor.getLength() );

    NodeList dataSources = dom.getElementsByTagName( "DataSources" );
    assertNotNull( dataSources );
    assertEquals( 1, dataSources.getLength() );

    NodeList connections = dom.getElementsByTagName( "Connection" );
    assertNotNull( connections );
    assertEquals( 1, connections.getLength() );

    Node connection = connections.item( 0 );
    assertEquals( "testQueryFTW", connection.getAttributes().getNamedItem( "id" ).getNodeValue() );

    NodeList dataAccesses = dom.getElementsByTagName( "DataAccess" );
    assertNotNull( dataSources );
    assertEquals( 1, dataSources.getLength() );

    Node dataAccess = dataAccesses.item( 0 );
    assertEquals( "public", dataAccess.getAttributes().getNamedItem( "access" ).getNodeValue() );
    assertEquals( "testQueryFTW", dataAccess.getAttributes().getNamedItem( "connection" ).getNodeValue() );
    assertEquals( "testQueryFTW", dataAccess.getAttributes().getNamedItem( "id" ).getNodeValue() );
    assertEquals( "scriptable", dataAccess.getAttributes().getNamedItem( "type" ).getNodeValue() );
    NodeList nodes = dataAccess.getChildNodes();
    for ( int i = 0; i < nodes.getLength(); i++ ) {
      Node el = nodes.item( i );
      switch ( el.getNodeName() ) {
        case "Name":
          assertEquals( "testQueryFTW", el.getFirstChild().getNodeValue() );
          break;
        case "Cache":
          assertEquals( "3600", el.getAttributes().getNamedItem( "duration" ).getNodeValue() );
          assertEquals( "true", el.getAttributes().getNamedItem( "enabled" ).getNodeValue() );
          break;
        case "Query":
          assertEquals( CDATASection.CDATA_SECTION_NODE, el.getFirstChild().getNodeType() );
          assertEquals(
            el.getFirstChild().getNodeValue(),
            "import org.pentaho.reporting.engine.classic.core.util"
              + ".TypedTableModel;\n\n"
              + "String[] columnNames = new String[]{\n"
              + "\"value\",\"name2\"\n"
              + "};\n\n\n"
              + "Class[] columnTypes = new Class[]{\n"
              + "Integer.class,\n"
              + "String.class\n"
              + "};\n\n"
              + "TypedTableModel model = new TypedTableModel(columnNames, columnTypes);\n\n"
              + "model.addRow(new Object[]{ new Integer(\"0\"), new String(\"Name\") });\n\n"
              + "return model;" );
          break;
      }
    }
  }
}
