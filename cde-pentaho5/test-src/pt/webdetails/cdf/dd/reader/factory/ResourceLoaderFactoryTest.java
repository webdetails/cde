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

import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.Assert;

public class ResourceLoaderFactoryTest {

  private static ResourceLoaderFactoryForTesting rlfft;
  private final String SYS_PATH = "/system/pentaho-cdf-dd/etc/";
  private final String SOL_PATH = "/path/etc/";

  @BeforeClass
  public static void setUp() throws Exception {
    rlfft = new ResourceLoaderFactoryForTesting();
  }

  @Test
  public void testResourceFactory() {
    IResourceLoader sys = rlfft.getResourceLoader( SYS_PATH );
    IResourceLoader sol = rlfft.getResourceLoader( SOL_PATH );

    Assert.assertEquals( sys.getClass(), SystemResourceLoader.class );
    Assert.assertEquals( sol.getClass(), SolutionResourceLoader.class );

  }
}
