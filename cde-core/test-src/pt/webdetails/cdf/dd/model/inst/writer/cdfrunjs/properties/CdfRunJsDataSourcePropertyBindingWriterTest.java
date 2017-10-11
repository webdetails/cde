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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CdfRunJsDataSourcePropertyBindingWriterTest extends TestCase {

  private CdfRunJsDataSourcePropertyBindingWriter dataSourcePropertyBindingWriter;
  private StringBuilder out;
  private CdfRunJsDashboardWriteContext context;
  private DataSourceComponent dataSourceComp;
  private String dashPath = "/solution/path/dashFolder/dash.wcdf";

  @Before
  public void setUp() throws Exception {
    dataSourcePropertyBindingWriter = new CdfRunJsDataSourcePropertyBindingWriter();
    out = new StringBuilder();
    context = mock( CdfRunJsDashboardWriteContext.class );
    dataSourceComp = mock( DataSourceComponent.class );
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
}
