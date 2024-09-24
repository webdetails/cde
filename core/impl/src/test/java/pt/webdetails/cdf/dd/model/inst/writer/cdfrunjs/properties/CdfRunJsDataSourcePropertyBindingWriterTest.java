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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.meta.DashboardType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.AttributeName.DATA_ACCESS_TYPE;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.MDX_QUERY;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.DataSource.PropertyName.STREAMING_TYPE;

public class CdfRunJsDataSourcePropertyBindingWriterTest {

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
