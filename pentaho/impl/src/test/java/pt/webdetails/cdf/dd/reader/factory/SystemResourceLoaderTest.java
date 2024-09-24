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

package pt.webdetails.cdf.dd.reader.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IACAccess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    assertTrue( mockLoader.getAccessControl().hasAccess( "", FileAccess.READ ) );
    assertTrue( mockLoader.getAccessControl().hasAccess( "", FileAccess.WRITE ) );
    assertTrue( mockLoader.getAccessControl().hasAccess( "", FileAccess.EXECUTE ) );
    assertTrue( mockLoader.getAccessControl().hasAccess( "", FileAccess.DELETE ) );
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
    assertFalse( mockLoader.getAccessControl().hasAccess( "", FileAccess.READ ) );
    assertFalse( mockLoader.getAccessControl().hasAccess( "", FileAccess.WRITE ) );
    assertFalse( mockLoader.getAccessControl().hasAccess( "", FileAccess.EXECUTE ) );
    assertFalse( mockLoader.getAccessControl().hasAccess( "", FileAccess.DELETE ) );
  }

}
