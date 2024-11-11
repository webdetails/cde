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


package org.pentaho.ctools.cde.datasources.manager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataSourceManagerTest {
  private DataSourceManager dataSourceManager;

  @Before
  public void setUp() {
    dataSourceManager = new DataSourceManager();
  }

  @After
  public void tearDown() {
    dataSourceManager = null;
  }

  @Test
  public void testGetProviderJsDefinitionEmptyProviderId() {
    assertNull( dataSourceManager.getProviderJsDefinition( "" ) );
  }

  @Test
  public void testGetProviderJsDefinitionEmptyProviderIdBypassCache() {
    assertNull( dataSourceManager.getProviderJsDefinition( "", true ) );
  }

  @Test
  public void testGetProviderJsDefinitionEmptyProviderIdBypassCacheFalse() {
    assertNull( dataSourceManager.getProviderJsDefinition( "", false ) );
  }

  @Test
  public void testGetProviderJsDefinitionNullProviderId() {
    assertNull( dataSourceManager.getProviderJsDefinition( null ) );
  }

  @Test
  public void testGetProviderJsDefinitionNullProviderIdBypassCache() {
    assertNull( dataSourceManager.getProviderJsDefinition( null, true ) );
  }

  @Test
  public void testGetProviderJsDefinitionNullProviderIdBypassCacheFalse() {
    assertNull( dataSourceManager.getProviderJsDefinition( null, false ) );
  }

  @Test
  public void testGetProviderNullId() {
    assertNull( dataSourceManager.getProvider( null ) );
  }

  @Test
  public void testGetProviderEmptyId() {
    assertNull( dataSourceManager.getProvider( "" ) );
  }

  @Test
  public void testGetProviders() {
    List<IDataSourceProvider> expected = Collections.emptyList();
    assertEquals( expected, dataSourceManager.getProviders() );
  }

  @Test
  public void testRefresh() {
    dataSourceManager.refresh();
    List<IDataSourceProvider> expected = Collections.emptyList();
    assertEquals( expected, dataSourceManager.getProviders() );
    assertNull( dataSourceManager.getProvider( null ) );
    assertNull( dataSourceManager.getProvider( "" ) );
  }
}
