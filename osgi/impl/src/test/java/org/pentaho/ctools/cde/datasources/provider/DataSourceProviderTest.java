/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package org.pentaho.ctools.cde.datasources.provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class DataSourceProviderTest {
  private final String PLUGIN_ID = "mockedPluginId";
  private  DataSourceProvider dataSourceProvider;

  @Before
  public void setUp() {
    dataSourceProvider = new DataSourceProvider( PLUGIN_ID );
  }

  @After
  public void tearDown() {
    dataSourceProvider = null;
  }

  @Test
  public void testInitialization() {
    dataSourceProvider = null;
    try {
      dataSourceProvider = new DataSourceProvider( null );
      fail( "null plugin id is invalid" );
    } catch ( AssertionError e ) {
      assertNull( dataSourceProvider );
    }
  }

  @Test
  public void testGetDataSourceDefinitions() {
    assertNull( dataSourceProvider.getDataSourceDefinitions( true ) );
    assertNull( dataSourceProvider.getDataSourceDefinitions( false ) );
  }

  @Test
  public void getId() {
    assertEquals( PLUGIN_ID, dataSourceProvider.getId() );
  }

  @Test
  public void testToString() {
    final String expected = String.format( "DataSourceProvider [pluginId=%s]", PLUGIN_ID );
    assertEquals( expected, dataSourceProvider.toString() );
  }
}
