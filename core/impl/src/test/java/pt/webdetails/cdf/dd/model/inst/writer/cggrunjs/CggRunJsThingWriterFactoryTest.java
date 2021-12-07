/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CggRunJsThingWriterFactoryTest {

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

  @Test( expected = IllegalArgumentException.class )
  public void testNullValueThing() throws Exception {
    cggRunJsThingWriterFactory = new CggRunJsThingWriterFactoryForTest( "" );

    // An IllegalArgumentException should be thrown here
    cggRunJsThingWriterFactory.getWriter( null );
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
      assertFalse( ((CggRunJsGenericComponentWriter) iWriter).canWrite() );

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
      assertTrue( ((CggRunJsGenericComponentWriter) iWriter).canWrite() );

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

  private static class CggRunJsThingWriterFactoryForTest extends CggRunJsThingWriterFactory {

    private final String compId;

    public CggRunJsThingWriterFactoryForTest( String id ) {
      this.compId = id;
    }

    @Override
    protected String getId( GenericComponent comp ) {
      return this.compId;
    }
  }
}
