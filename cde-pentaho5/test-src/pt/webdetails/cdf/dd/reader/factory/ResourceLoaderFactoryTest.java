/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.reader.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

public class ResourceLoaderFactoryTest {

  private static ResourceLoaderFactoryForTesting rlfft;
  private final String SYS_PATH = "/system/pentaho-cdf-dd/etc/";
  private final String SOL_PATH = "/public/cde/etc/";
  private final String STATIC_PATH = "/path/etc/";

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
    Assert.assertEquals( sys.getClass(), SystemResourceLoader.class );
  }

  @Test
  public void testResourceFactoryRepos() {
    IResourceLoader sol = rlfft.getResourceLoader( SOL_PATH );
    Assert.assertEquals( sol.getClass(), SolutionResourceLoader.class );
  }

  @Test
  public void testResourceFactoryStaticSystem() {
    rlfft.setSystemStatic( true );
    IResourceLoader sys = rlfft.getResourceLoader( STATIC_PATH );
    Assert.assertEquals( sys.getClass(), SystemResourceLoader.class );
  }

  @Test
  public void testResourceFactoryStaticRepos() {
    rlfft.setRepositoryStatic( true );
    IResourceLoader sol = rlfft.getResourceLoader( STATIC_PATH );
    Assert.assertEquals( sol.getClass(), SolutionResourceLoader.class );
  }

}
