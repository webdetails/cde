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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.DATA_ACCESS_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.MDX_QUERY;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.STREAMING_TYPE;

public class CdfRunJsDataSourcePropertyBindingWriterTest extends TestCase {

  private CdfRunJsDataSourcePropertyBindingWriter dataSourcePropertyBindingWriter;
  private StringBuilder out;
  private CdfRunJsDashboardWriteContext context;
  private DataSourceComponent dataSourceComp;
  private Dashboard dashboard;
  private Dashboard.Builder builder;
  private MetaModel metaModel;
  private String dashPath = "/solution/path/dashFolder/dash.wcdf";

  @Before
  public void setUp() throws Exception {
    dataSourcePropertyBindingWriter = new CdfRunJsDataSourcePropertyBindingWriter();
    out = new StringBuilder();
    context = mock( CdfRunJsDashboardWriteContext.class );
    dataSourceComp = mock( DataSourceComponent.class );

    builder = spy( new Dashboard.Builder() );
    builder.setSourcePath( "" );
    builder.setMeta( new DashboardType.Builder().build() );
    builder.setWcdf( new DashboardWcdfDescriptor() );
    metaModel = mock( MetaModel.class );
    dashboard = builder.build( metaModel );
  }

  @After
  public void tearDown() throws Exception {
    dataSourcePropertyBindingWriter = null;
    out = null;
    context = null;
    dataSourceComp = null;
  }

  @Test
  public void testRenderCdaDataSourceRelativePath() {
    doReturn( "1" ).when( dataSourceComp ).tryGetPropertyValue( "outputIndexId", null );
    doReturn( "../dataSources/file.cda" ).when( dataSourceComp ).tryGetPropertyValue( "cdaPath", null );

    String expectedValue = "path: \"/solution/path/dataSources/file.cda\"";
    dataSourcePropertyBindingWriter.renderCdaDatasource( out, context, dataSourceComp, "1", dashPath );

    assertTrue( out.toString().contains( expectedValue ) );
  }

  @Test
  public void testCdaDataSourceAbsolutePath() {
    doReturn( "1" ).when( dataSourceComp ).tryGetPropertyValue( "outputIndexId", null );
    doReturn( "/solution/path/dataSources/file.cda" ).when( dataSourceComp ).tryGetPropertyValue( "cdaPath", null );

    String expectedValue = "path: \"/solution/path/dataSources/file.cda\"";
    dataSourcePropertyBindingWriter.renderCdaDatasource( out, context, dataSourceComp, "1", dashPath );

    assertTrue( out.toString().contains( expectedValue ) );
  }

  @Test
  public void testRenderCdaDatasourcePushEnabled() {
    renderCdaDatasource( STREAMING_TYPE, true );
  }

  @Test
  public void testRenderCdaDatasource() {
    renderCdaDatasource( MDX_QUERY, false );
  }

  private void renderCdaDatasource ( String dataAccessType, boolean pushEnabled ) {
    doReturn( false ).when( context ).isFirstInList();
    doReturn( "" ).when( context ).getIndent();
    doReturn( dashboard ).when( context).getDashboard();
    doReturn( dataAccessType ).when( dataSourceComp ).tryGetAttributeValue( DATA_ACCESS_TYPE, null );

    dataSourcePropertyBindingWriter.renderCdaDatasource( out, context, dataSourceComp, "dataSourceId" );
    assertEquals( ","+System.lineSeparator()
      + "dataAccessId: \"dataSourceId\","+System.lineSeparator()
      + "solution: \"\","+System.lineSeparator()
      + "path: \"\","+System.lineSeparator()
      + "file: \"\","+System.lineSeparator()
      + "pushEnabled: "+pushEnabled, out.toString() );
  }

  @Test
  public void testRenderBuiltinDatasource() {
    doReturn( false ).when( context ).isFirstInList();
    doReturn( "" ).when( context ).getIndent();
    doReturn( dashboard ).when( context).getDashboard();
    doReturn( STREAMING_TYPE ).when( dataSourceComp ).tryGetAttributeValue( DATA_ACCESS_TYPE, null );
    doReturn( "datasource name" ).when( dataSourceComp ).getName();

    dataSourcePropertyBindingWriter.renderBuiltinCdaDatasource( out, context, dataSourceComp );
    assertEquals( ","+System.lineSeparator()
      + "dataAccessId: \"datasource name\","+System.lineSeparator()
      + "path: \"\","+System.lineSeparator()
      + "pushEnabled: true", out.toString() );
  }
}
