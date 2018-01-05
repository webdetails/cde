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

package pt.webdetails.cdf.dd.structure;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;

public class DashboardWcdfDescriptorTest extends TestCase {

  private DashboardWcdfDescriptorForTest wcdf;


  private class DashboardWcdfDescriptorForTest extends DashboardWcdfDescriptor {

    private HashMap<String, Object> parameters;

    public DashboardWcdfDescriptorForTest() {
      super();
      this.parameters = new HashMap<String, Object>();
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

    Assert.assertTrue( "Should continue empty", wcdf.getWidgetParameters().length == 0 );
  }

  @Test
  public void testUpdateWidgetParamsInsert() {
    wcdf = new DashboardWcdfDescriptorForTest();
    wcdf.addParameter( "widgetParameters", new String[] { "param1" } );
    wcdf.update( wcdf.getParameters() );

    Assert.assertTrue( "Should have widget parameter 'param1'", wcdf.getWidgetParameters()[ 0 ].equals( "param1" ) );
  }

  @Test
  public void testUpdateWidgetParamsRemove() {
    wcdf = new DashboardWcdfDescriptorForTest( new String[] { "param1" } );

    wcdf.update( wcdf.getParameters() );
    Assert.assertTrue( "'param1' should have been removed", wcdf.getWidgetParameters().length == 0 );
  }
}
