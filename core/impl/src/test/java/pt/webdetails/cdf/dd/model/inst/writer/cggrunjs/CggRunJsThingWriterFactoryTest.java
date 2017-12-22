/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;


import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CggRunJsThingWriterFactoryTest extends TestCase {

  private CggRunJsThingWriterFactoryForTest cggRunJsThingWriterFactory;
  private GenericComponentType meta;

  @Before
  public void setUp() throws Exception {
    meta = mock( GenericComponentType.class );
  }

  @After
  public void tearDown() throws Exception {
    cggRunJsThingWriterFactory = null;
    meta = null;
  }

  @Test
  public void testNullValueThing() {
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "" );
    try {
      cggRunJsThingWriterFactory.getWriter( null );
      fail( "IllegalArgumentException should have been thrown" );
    } catch ( IllegalArgumentException e ) {
      assertTrue( true );
    } catch ( UnsupportedThingException e ) {
      fail( "IllegalArgumentException should have been thrown" );
    }
  }

  @Test
  public void testCggDialThing() {
    GenericComponent thing = mock( GenericComponent.class );
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "cggDial" );

    doReturn( KnownThingKind.Component ).when( thing ).getKind();
    doReturn( meta ).when( thing ).getMeta();
    doReturn( "true" ).when( meta ).tryGetAttributeValue( "cdwSupport", "false" );
    try {

      IThingWriter iWriter = cggRunJsThingWriterFactory.getWriter( thing );
      assertTrue( iWriter instanceof CggRunJsGenericComponentWriter );
      CggRunJsGenericComponentWriter writer = (CggRunJsGenericComponentWriter) iWriter;
      assertFalse( writer.canWrite() );

    } catch ( IllegalArgumentException e ) {
      fail( "IllegalArgumentException should not have been thrown" );
    } catch ( UnsupportedThingException e ) {
      fail( "UnsupportedThingException should not have been thrown" );
    }
  }

  @Test
  public void testOtherComponentThing() {
    GenericComponent thing = mock( GenericComponent.class );
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "" );

    doReturn( KnownThingKind.Component ).when( thing ).getKind();
    doReturn( meta ).when( thing ).getMeta();
    doReturn( "true" ).when( meta ).tryGetAttributeValue( "cdwSupport", "false" );

    try {

      IThingWriter iWriter = cggRunJsThingWriterFactory.getWriter( thing );
      assertTrue( iWriter instanceof CggRunJsGenericComponentWriter );
      CggRunJsGenericComponentWriter writer = (CggRunJsGenericComponentWriter) iWriter;
      assertTrue( writer.canWrite() );

    } catch ( IllegalArgumentException e ) {
      fail( "IllegalArgumentException should not have been thrown" );
    } catch ( UnsupportedThingException e ) {
      fail( "UnsupportedThingException should not have been thrown" );
    }
  }

  @Test
  public void testDataSourceThing() {
    DataSourceComponent thing = mock( DataSourceComponent.class );
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "" );

    doReturn( KnownThingKind.Component ).when( thing ).getKind();

    try {

      IThingWriter iWriter = cggRunJsThingWriterFactory.getWriter( thing );
      assertTrue( iWriter instanceof CggRunJsDataSourceComponentWriter );

    } catch ( IllegalArgumentException e ) {
      fail( "IllegalArgumentException should not have been thrown" );
    } catch ( UnsupportedThingException e ) {
      fail( "UnsupportedThingException should not have been thrown" );
    }
  }

  @Test
  public void testDashboardThing() {
    DataSourceComponent thing = mock( DataSourceComponent.class );
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "" );

    doReturn( KnownThingKind.Dashboard ).when( thing ).getKind();

    try {

      IThingWriter iWriter = cggRunJsThingWriterFactory.getWriter( thing );
      assertTrue( iWriter instanceof CggRunJsDashboardWriter );

    } catch ( IllegalArgumentException e ) {
      fail( "IllegalArgumentException should not have been thrown" );
    } catch ( UnsupportedThingException e ) {
      fail( "UnsupportedThingException should not have been thrown" );
    }
  }

  private class CggRunJsThingWriterFactoryForTest extends CggRunJsThingWriterFactory {

    private String compId;

    public CggRunJsThingWriterFactoryForTest( String id ) {
      this.compId = id;
    }

    @Override
    protected String getId( GenericComponent comp ) {
      return this.compId;
    }
  }
}
