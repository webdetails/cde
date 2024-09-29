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


package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.meta.GenericComponentType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.INDENT1;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public class CdfRunJsGenericComponentWriterTest {

  private static CdfRunJsGenericComponentWriter genericComponentWriter;
  private static CdfRunJsDashboardWriteContext context;
  private static CdfRunJsDashboardWriteOptions options;
  private static GenericComponent genericComponent;
  private static GenericComponentType genericComponentType;

  @Before
  public void setUp() throws Exception {
    genericComponentWriter = new CdfRunJsGenericComponentWriter();
    context = Mockito.mock( CdfRunJsDashboardWriteContext.class );
    options = Mockito.mock( CdfRunJsDashboardWriteOptions.class );
    genericComponent = Mockito.mock( GenericComponent.class );
    genericComponentType = Mockito.mock( GenericComponentType.class );
  }

  @After
  public void tearDown() throws Exception {
    genericComponentWriter = null;
    context = null;
    options = null;
    genericComponent = null;
    genericComponentType = null;
  }

  @Test
  public void testGenericComponentWrite() {

    StringBuilder out = new StringBuilder();

    when( genericComponent.getMeta() ).thenReturn( genericComponentType );
    when( genericComponent.getId() ).thenReturn( "test" );

    when( options.getAliasPrefix() ).thenReturn( "" );
    when( context.getOptions() ).thenReturn( options );

    try {

      genericComponentWriter.write( out, context, genericComponent, "TestComponent" );

    } catch ( ThingWriteException e ) {
      e.printStackTrace();
    }

    StringBuilder expectedReturnValue = new StringBuilder();
    expectedReturnValue.append( "var test = new TestComponent({" ).append( NEWLINE )
        .append( INDENT1 ).append( "type: \"TestComponent\"," ).append( NEWLINE )
        .append( INDENT1 ).append( "name: \"test\"" ).append( NEWLINE )
        .append( "});" ).append( NEWLINE );

    assertEquals( expectedReturnValue.toString(), out.toString() );
  }
}
