/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

package pt.webdetails.cdf.dd.structure;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.webdetails.cdf.dd.render.CdaRendererForTesting;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Olap4jDatasourceStructureTest {

  private static final Log logger = LogFactory.getLog( Olap4jDatasourceStructureTest.class );

  private static final String CDFDE_FILE = "test-resources/datasources/olap4j/test-olap4j.cdfde";
  private static final String CDA_DATASOURCE_DEFINITIONS = "test-resources/cda-resources/datasource-definitions.js";

  /**
   * map that holds some values we can validate against the generated cda datasource
   */
  private Map validationPropertyMap = new HashMap();

  private String cdfdeContent = StringUtils.EMPTY;
  private String cdaDatasourceDefinitions = StringUtils.EMPTY;

  private static Document stringToDom( String xmlSource ) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse( new InputSource( new StringReader( xmlSource ) ) );
  }

  @Before
  public void setUp() throws Exception {

    // load .cdfde content: when saving a dashboard, this is the dashboard structure the client sends
    // over HTTP POST /pentaho-cdf-dd/api/syncronizer/saveDashboard (as one of the form-data params)

    // we mock this by loading the content from a previously saved .cdfde file
    cdfdeContent = FileUtils.readFileToString( new File( CDFDE_FILE ) );

    Assert.assertTrue( !StringUtils.isEmpty( cdfdeContent ) );

    // we mock the cda interplugin call to fetch datasource definitions
    // by loading the content from a previously saved .js file
    cdaDatasourceDefinitions = FileUtils.readFileToString( new File( CDA_DATASOURCE_DEFINITIONS ) );

    Assert.assertTrue( !StringUtils.isEmpty( cdaDatasourceDefinitions ) );

    validationPropertyMap.put( "JdbcUser" , "pentaho_user" );
    validationPropertyMap.put( "JdbcPassword" , "password" );
    validationPropertyMap.put( "Jdbc" , "jdbc:hsqldb:hsql://localhost:9001/Sampledata" );
    validationPropertyMap.put( "JdbcDriver" , "org.hsqldb.jdbcDriver" );
    validationPropertyMap.put( "Catalog" , "mondrian:/SteelWheels" );
  }

  @Test
  public void testJsonArrayToOlap4jDatasourceConnectionProperties() {

    logger.debug( "cdfdeContent -> " + cdfdeContent );
    logger.debug( "cdaDatasourceDefinitions -> " + cdaDatasourceDefinitions );

    try {

      logger.info( "Preparing to instantiate CdaRenderer..." );

      CdaRendererForTesting cdaRenderer = new CdaRendererForTesting( cdfdeContent );

      logger.info( "CdaRenderer instantiated" );

      //Assert.assertTrue( !cdaRenderer.isEmpty() );

      logger.info( "Calling cdaRenderer.render() ..." );

      // CdaRenderer will build the xml document according to the
      // provided .cdfde content and cda datasource definitions
      String cdaContentAsString = cdaRenderer.render();

      logger.info( "CdaRenderer.render() finished" );

      logger.info( "cdaContentAsString ->\n" + cdaContentAsString );

      Assert.assertTrue( !StringUtils.isEmpty( cdaContentAsString ) );

      // this is the .cda file content ( that would have been saved / stored )
      Document dom = stringToDom( cdaContentAsString );

      NodeList nodes = dom.getElementsByTagName( "Property" );

      Assert.assertNotNull( nodes );
      Assert.assertTrue( nodes.getLength() > 0 );

      logger.info( "Document DataSources/Connection/Property nodes length: " + nodes.getLength() );

      for ( int i = 0; i < nodes.getLength(); i++ ) {

        Node n = nodes.item( i );

        String propName = n.getAttributes().getNamedItem( "name" ).getTextContent();
        String propValue = n.getTextContent();

        logger.info( "name=" + propName + " , value=" + propValue );

        Assert.assertTrue( validationPropertyMap.get( propName ).equals( propValue ) );
      }
    } catch ( Exception e ) {
      logger.error( e );
      Assert.fail( e.getMessage() );
    }
  }

  @After
  public void tearDown() throws Exception {
    cdfdeContent = null;
    cdaDatasourceDefinitions = null;
  }
}
