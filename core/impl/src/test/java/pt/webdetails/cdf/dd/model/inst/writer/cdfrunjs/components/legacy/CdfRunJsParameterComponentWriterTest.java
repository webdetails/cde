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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.legacy.PentahoCdfRunJsDashboardWriteContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CdfRunJsParameterComponentWriterTest {
  private static final String ROOT = "src/test/resources";
  private static final String TEST_FOLDER = "test";
  private static final String DASHBOARD = "testDashboard.wcdf";
  private static final String NEWLINE = System.getProperty( "line.separator" );

  private static IThingWriter writer;
  private static PentahoCdfRunJsDashboardWriteContext context;

  @BeforeClass
  public static void setUp() throws Exception {
    writer = new CdfRunJsParameterComponentWriter();
    context = Mockito.mock( PentahoCdfRunJsDashboardWriteContext.class );
  }

  @Test
  public void testParameterComponentWrite() {
    ParameterComponent parameterComponent = Mockito.mock( ParameterComponent.class );
    when( parameterComponent.tryGetPropertyValue( "propertyValue", "" ) ).thenReturn( "1" );
    when( parameterComponent.tryGetPropertyValue( "parameterViewRole", "unused" ) ).thenReturn( "unused" );
    when( context.getId( parameterComponent ) ).thenReturn( "param1" );

    StringBuilder dashboardResult = new StringBuilder();

    try {
      writer.write( dashboardResult, context, parameterComponent );

      assertEquals( "Dashboards.addParameter(\"param1\", \"1\");" + NEWLINE
          + "Dashboards.setParameterViewMode(\"param1\", \"unused\");" + NEWLINE, dashboardResult.toString() );
    } catch ( ThingWriteException e ) {
    }
  }
}
