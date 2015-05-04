/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;

public class SystemResourceLoaderTest {

  private static SystemResourceLoader mockLoader;
  private IACAccess mockAccess = null;

  @Before
  public void setUp() throws Exception {
    mockLoader = Mockito.mock( SystemResourceLoader.class );
  }

  @After
  public void tearDown() throws Exception {
    mockLoader = null;
    mockAccess = null;
  }

  @Test
  public void testSystemResourceLoaderIsAdmin() {

    final boolean isAdmin = true;

    mockAccess = new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        return isAdmin;
      }
    };

    Mockito.when( mockLoader.getAccessControl() ).thenReturn( mockAccess );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.READ ), true );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.WRITE ), true );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.EXECUTE ), true );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.DELETE ), true );

  }

  @Test
  public void testSystemResourceLoaderUserNotAdmin() {

    final boolean isAdmin = false;

    mockAccess = new IACAccess() {
      public boolean hasAccess( String file, FileAccess access ) {
        return isAdmin;
      }
    };

    Mockito.when( mockLoader.getAccessControl() ).thenReturn( mockAccess );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.READ ), false );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.WRITE ), false );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.EXECUTE ), false );
    Assert.assertEquals( mockLoader.getAccessControl().hasAccess( "", FileAccess.DELETE ), false );
  }
























}
