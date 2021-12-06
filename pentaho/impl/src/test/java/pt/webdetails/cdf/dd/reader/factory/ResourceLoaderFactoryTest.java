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
