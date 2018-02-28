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

package org.pentaho.ctools.cde.cache.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pt.webdetails.cdf.dd.DashboardCacheKey;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IReadAccess;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheTest {
  private static final String EHCACHE_FILE_PATH = "src/main/resources/ehcache.xml";
  private static IReadAccess mockedReadAccess;
  private static Cache cache;
  private DashboardCacheKey key;
  private CdfRunJsDashboardWriteResult value;

  @BeforeClass
  public static void beforeAll() throws Exception {
    mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.getFileInputStream( anyString() ) )
      .thenReturn( new FileInputStream( new File( EHCACHE_FILE_PATH.replace( "/", File.separator ) ) ) );
    cache = new Cache( mockedReadAccess );
  }

  @AfterClass
  public static void afterAll() {
    cache = null;
    mockedReadAccess = null;
  }

  @Before
  public void setUp() {
    this.cache.removeAll();
    key = new DashboardCacheKey("mock", "", false, false, "", "" );
    value = new CdfRunJsDashboardWriteResult.Builder().build();
  }

  @After
  public void tearDown() {
    value = null;
    key = null;
  }

  @Test
  public void testInitializationFailLoadConfiguration() {
    try{
      when( mockedReadAccess.getFileInputStream( anyString() ) )
        .thenThrow( new IOException( "mocked IOException" ) );
      new Cache( mockedReadAccess );
      Assert.fail( "InitializationException not thrown" );
    } catch ( InitializationException e ) {
      Assert.assertEquals( "Failed to load the cache configuration file: ehcache.xml", e.getMessage() );
    } catch ( Exception e ) {
      Assert.fail( "InitializationException not thrown" );
    }
  }

  @Test
  public void testInitializationFailLoadCacheManager() {
    try {
      when( mockedReadAccess.getFileInputStream( anyString() ) )
        .thenReturn( null );
      new Cache( mockedReadAccess );
      Assert.fail( "InitializationException not thrown" );
    } catch ( InitializationException e ) {
      Assert.assertEquals( "Failed to create the cache manager.", e.getMessage() );
    } catch ( Exception e ) {
      Assert.fail( "InitializationException not thrown" );
    }
  }

  @Test
  public void testPut() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
  }

  @Test
  public void testGet() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
  }

  @Test
  public void testGetKeys() {
    cache.put( key, value );
    assertEquals( 1, cache.getKeys().size() );
  }

  @Test
  public void testRemove() {
    cache.put( key, value );
    assertEquals( value, cache.get( key ) );
    cache.remove( key );
    assertEquals( null, cache.get( key ) );
  }

  @Test
  public void testRemoveAll() {
    cache.put( key, value );
    assertEquals( 1, cache.getKeys().size() );
    cache.removeAll();
    assertEquals( 0, cache.getKeys().size() );
  }
}
