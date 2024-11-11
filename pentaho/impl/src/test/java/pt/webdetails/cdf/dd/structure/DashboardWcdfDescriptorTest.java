/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.structure;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class DashboardWcdfDescriptorTest {

  private DashboardWcdfDescriptorForTest wcdf;

  private static class DashboardWcdfDescriptorForTest extends DashboardWcdfDescriptor {

    private final HashMap<String, Object> parameters;

    public DashboardWcdfDescriptorForTest() {
      super();
      this.parameters = new HashMap<>();
    }

    public DashboardWcdfDescriptorForTest( String[] params ) {
      this();
      setWidgetParameters( params );
    }

    private void addParameter( String key, Object value ) {
      this.parameters.put( key, value );
    }

    private HashMap<String, Object> getParameters() {
      return this.parameters;
    }
  }

  @Test
  public void testUpdateWidgetParamsSimple() {
    wcdf = new DashboardWcdfDescriptorForTest();
    wcdf.update( wcdf.getParameters() );

    assertEquals( "Should continue empty", 0, wcdf.getWidgetParameters().length );
  }

  @Test
  public void testUpdateWidgetParamsInsert() {
    wcdf = new DashboardWcdfDescriptorForTest();
    wcdf.addParameter( "widgetParameters", new String[] { "param1" } );
    wcdf.update( wcdf.getParameters() );

    assertEquals( "Should have widget parameter 'param1'", "param1", wcdf.getWidgetParameters()[ 0 ] );
  }

  @Test
  public void testUpdateWidgetParamsRemove() {
    wcdf = new DashboardWcdfDescriptorForTest( new String[] { "param1" } );

    wcdf.update( wcdf.getParameters() );
    assertEquals( "'param1' should have been removed", 0, wcdf.getWidgetParameters().length );
  }
}
