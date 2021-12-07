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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CdfRunJsParameterComponentWriterTest {

  private static final String NEWLINE = System.getProperty( "line.separator" );
  private static final String INDENT = "\t";

  private static CdfRunJsParameterComponentWriter parameterComponentWriter;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDashboardWriteOptions options;
  private static ParameterComponent parameterComponent;

  @Before
  public void setUp() throws Exception {
    parameterComponentWriter = new CdfRunJsParameterComponentWriter();
    context = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );
    parameterComponent = Mockito.mock( ParameterComponent.class );
  }

  @After
  public void tearDown() throws Exception {
    parameterComponentWriter = null;
    context = null;
    options = null;
    parameterComponent = null;
  }

  @Test
  public void testParameterComponentWrite() {

    when( parameterComponent.tryGetPropertyValue( "propertyValue", "" ) ).thenReturn( "1" );
    when( parameterComponent.tryGetPropertyValue( "parameterViewRole", "unused" ) ).thenReturn( "unused" );
    when( parameterComponent.tryGetPropertyValue( "bookmarkable", null ) ).thenReturn( "true" );

    when( parameterComponent.getId() ).thenReturn( "param1" );

    when( options.getAliasPrefix() ).thenReturn( "" );
    when( context.getOptions() ).thenReturn( options );

    StringBuilder out = new StringBuilder();

    try {

      parameterComponentWriter.write( out, context, parameterComponent );

    } catch ( ThingWriteException e ) {
      e.printStackTrace();
    }

    StringBuilder dashboardResult = new StringBuilder();

    dashboardResult.append( "dashboard.addParameter(\"param1\", \"1\");" ).append( NEWLINE )
        .append( "dashboard.setBookmarkable(\"param1\");" ).append( NEWLINE )
        .append( "dashboard.setParameterViewMode(\"param1\", \"unused\");" ).append( NEWLINE );

    assertEquals( out.toString(), dashboardResult.toString() );
  }
}
