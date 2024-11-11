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


package pt.webdetails.cdf.dd.settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cpf.repository.api.IRWAccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CdfDDSettingsTest {

  private static final String SYSTEM_PATH_IN_SETTINGS_XML = "/this/is/a/system/path";
  private static final String REPO_PATH_IN_SETTINGS_XML = "/this/is/a/repo/path";

  CdeSettings.CdfDDSettings cdfDDSettings;

  @Before
  public void setUp() throws Exception {
    cdfDDSettings = new CdfDDSettingsForTesting( Mockito.mock( IRWAccess.class ) );
  }

  @Test
  public void testReadFilePickerHiddenStaticFoldersSettings() {

    String[] paths = cdfDDSettings.getFilePickerHiddenFoldersByType( CdeSettings.FolderType.STATIC );

    // we should have found 1 item
    assertNotNull( paths );
    assertEquals( 1, paths.length );

    // should have the value at SYSTEM_PATH_IN_SETTINGS_XML
    assertEquals( SYSTEM_PATH_IN_SETTINGS_XML, paths[ 0 ] );
  }

  @Test
  public void testReadFilePickerHiddenRepoFoldersSettings() {

    String[] paths = cdfDDSettings.getFilePickerHiddenFoldersByType( CdeSettings.FolderType.REPO );

    // we should have found 1 item
    assertNotNull( paths );
    assertEquals( 1, paths.length );

    // should have the value at REPO_PATH_IN_SETTINGS_XML
    assertEquals( REPO_PATH_IN_SETTINGS_XML, paths[ 0 ] );
  }

  @After
  public void tearDown() throws Exception {
    cdfDDSettings = null;
  }
}
