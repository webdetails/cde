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


package pt.webdetails.cdf.dd.reader.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourceLoaderFactoryTest {

  private static ResourceLoaderFactoryForTesting rlfft;
  private static final String SYS_PATH = "/system/pentaho-cdf-dd/etc/";
  private static final String SOL_PATH = "/public/cde/etc/";
  private static final String STATIC_PATH = "/path/etc/";
  private static final String EMPTY_PATH = "";

  @Before
  public void setUp() throws Exception {
    rlfft = new ResourceLoaderFactoryForTesting();
  }

  @After
  public void tearDown() throws Exception {
    rlfft = null;
  }

  @Test
  public void testResourceFactorySystem() {
    IResourceLoader sys = rlfft.getResourceLoader( SYS_PATH );
    assertEquals( sys.getClass(), SystemResourceLoader.class );
  }

  @Test
  public void testResourceFactoryRepos() {
    IResourceLoader sol = rlfft.getResourceLoader( SOL_PATH );
    assertEquals( sol.getClass(), SolutionResourceLoader.class );
  }

  @Test
  public void testResourceFactoryStaticSystem() {
    rlfft.setSystemStatic( true );
    IResourceLoader sys = rlfft.getResourceLoader( STATIC_PATH );
    assertEquals( sys.getClass(), SystemResourceLoader.class );
  }

  @Test
  public void testResourceFactoryStaticRepos() {
    rlfft.setRepositoryStatic( true );
    IResourceLoader sol = rlfft.getResourceLoader( STATIC_PATH );
    assertEquals( sol.getClass(), SolutionResourceLoader.class );
  }

  @Test
  public void testResourceFactoryEmptyPath() {
    IResourceLoader sol = rlfft.getResourceLoader( EMPTY_PATH );
    assertEquals( sol.getClass(), SolutionResourceLoader.class );
  }

}
