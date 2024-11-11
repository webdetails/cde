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


package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.meta.ComponentType;
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.reader.cdexml.fs.XmlFsPluginThingReaderFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class XmlComponentTypeReaderTest {

  private static XmlComponentTypeReader xmlComponentTypeReader;

  @Before
  public void setUp() throws Exception {
    xmlComponentTypeReader = new XmlAdhocComponentTypeReader(
      CustomComponentType.Builder.class,
      mock( XmlFsPluginThingReaderFactory.class ) );
  }

  @After
  public void tearDown() throws Exception {
    xmlComponentTypeReader = null;
  }

  @Test
  public void testReadComponentSupportType() {

    ComponentType.Builder builder;
    Element elem;

    // supports AMD
    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement("DesignerComponent");
    elem.addElement( "Contents" )
      .addElement( "Implementation" )
      .addAttribute( "supportsAMD", "true" )
      .addAttribute( "supportsLegacy", "false" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertFalse( builder.isSupportsLegacy() );
    assertTrue( builder.isSupportsAMD() );

    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement( "DesignerComponent" );
    elem.addElement( "Contents" )
      .addElement( "Implementation" )
      .addAttribute( "supportsAMD", "true" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertFalse( builder.isSupportsLegacy() );
    assertTrue( builder.isSupportsAMD() );

    // supports Legacy
    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement( "DesignerComponent" );
    elem.addElement( "Contents" )
      .addElement( "Implementation" )
      .addAttribute( "supportsAMD", "false" )
      .addAttribute( "supportsLegacy", "true" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertTrue( builder.isSupportsLegacy() );
    assertFalse( builder.isSupportsAMD() );

    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement( "DesignerComponent" );
    elem.addElement( "Contents" )
      .addElement( "Implementation" )
      .addAttribute( "supportsLegacy", "true" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertTrue( builder.isSupportsLegacy() );
    assertFalse( builder.isSupportsAMD() );

    // supports both AMD and Legacy
    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement( "DesignerComponent" );
    elem.addElement( "Contents" )
      .addElement( "Implementation" )
      .addAttribute( "supportsAMD", "true" )
      .addAttribute( "supportsLegacy", "true" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertTrue( builder.isSupportsLegacy() );
    assertTrue( builder.isSupportsAMD() );

    // empty support type should use default values
    builder = new CustomComponentType.Builder();
    elem = DocumentHelper.createDocument().addElement( "DesignerComponent" );
    elem.addElement( "Contents" )
      .addElement( "Implementation" );
    xmlComponentTypeReader.readComponentSupportType( builder, elem );
    assertTrue( builder.isSupportsLegacy() );
    assertFalse( builder.isSupportsAMD() );
  }
}
