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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;

import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.writer.cderunjs.amd.CdeRunJsThingWriterFactory;

import static org.mockito.Mockito.*;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.*;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT1;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT2;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public class CdfRunJsDataSourceComponentWriterTest extends TestCase {

  private static Dashboard dash;
  private static CdeRunJsThingWriterFactory factory;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDataSourceComponentWriter dataSourceComponentWriter;
  private static DataSourceComponent dataSourceComponent;
  private static DataSourceComponentType dataSourceComponentType;

  @Before
  public void setUp() throws Exception {
    dash = Mockito.mock( Dashboard.class );
    factory = Mockito.mock( CdeRunJsThingWriterFactory.class );
    dataSourceComponentWriter = new CdfRunJsDataSourceComponentWriter();
    context = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    dataSourceComponent = Mockito.mock( DataSourceComponent.class );
    dataSourceComponentType = Mockito.mock( DataSourceComponentType.class );
  }

  @After
  public void tearDown() throws Exception {
    dash = null;
    factory = null;
    dataSourceComponentWriter = null;
    context = null;
    dataSourceComponent = null;
    dataSourceComponentType = null;
  }

  @Test
  public void testDataSourceComponentWrite() throws ThingWriteException, UnsupportedThingException {

    StringBuilder out = new StringBuilder();

    doReturn( dash ).when( context ).getDashboard();
    doReturn( factory ).when( context ).getFactory();
    doReturn( dataSourceComponentWriter ).when( factory ).getWriter( any( DataSourceComponent.class ) );
    doReturn( dataSourceComponentType ).when( dataSourceComponent ).getMeta();

    // CDA Data Source
    out.setLength( 0 );
    doReturn( "fakeDataAccessId" ).when( dataSourceComponent ).tryGetPropertyValue( "dataAccessId", null );
    doReturn( "/path/fake.cda" ).when( dataSourceComponent ).tryGetPropertyValue( "cdaPath", null );
    dataSourceComponentWriter.write( out, context, dataSourceComponent );
    Assert.assertEquals(
      "{" + NEWLINE
        + INDENT2 + "dataAccessId: \"fakeDataAccessId\"," + NEWLINE
        + INDENT2 + "path: \"/path/fake.cda\"" + NEWLINE
        + INDENT1 + "}",
      out.toString() );

    // Data Source
    out.setLength( 0 );
    doReturn( null ).when( dataSourceComponent ).tryGetPropertyValue( "dataAccessId", null );
    doReturn( "" ).when( dataSourceComponentType ).tryGetAttributeValue( "", "" );
    dataSourceComponentWriter.write( out, context, dataSourceComponent );
    Assert.assertEquals(
      "{" + NEWLINE
        + INDENT2 + "jndi: \"\"," + NEWLINE
        + INDENT2 + "catalog: \"\"," + NEWLINE
        + INDENT2 + "cube: \"\"," + NEWLINE
        + INDENT2 + "query: \"\"," + NEWLINE
        + INDENT2 + "queryType: \"sql\"" + NEWLINE
        + INDENT1 + "}",
      out.toString() );

    // Built-in CDA Data Source
    out.setLength( 0 );
    doReturn( null ).when( dataSourceComponent ).tryGetPropertyValue( "dataAccessId", null );
    doReturn( META_TYPE_CDA ).when( dataSourceComponentType ).tryGetAttributeValue( "", "" );
    dataSourceComponentWriter.write( out, context, dataSourceComponent );
    Assert.assertEquals(
      "{" + NEWLINE
        + INDENT2 + "dataAccessId: \"\"," + NEWLINE
        + INDENT2 + "path: \"\"" + NEWLINE
        + INDENT1 + "}",
      out.toString() );

    // CPK Data Source
    out.setLength( 0 );
    doReturn( null ).when( dataSourceComponent ).tryGetPropertyValue( "dataAccessId", null );
    doReturn( META_TYPE_CPK ).when( dataSourceComponentType ).tryGetAttributeValue( "", "" );
    dataSourceComponentWriter.write( out, context, dataSourceComponent );
    Assert.assertEquals(
      "{" + NEWLINE
        + INDENT2 + "endpoint: \"\"," + NEWLINE
        + INDENT2 + "pluginId: \"\"," + NEWLINE
        + INDENT2 + "stepName: \"\"," + NEWLINE
        + INDENT2 + "kettleOutput: \"\"," + NEWLINE
        + INDENT2 + "queryType: \"cpk\"" + NEWLINE
        + INDENT1 + "}",
      out.toString() );
  }

}
