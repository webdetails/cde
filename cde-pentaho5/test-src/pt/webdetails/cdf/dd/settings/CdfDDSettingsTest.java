/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.settings;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cpf.repository.api.IRWAccess;

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
    Assert.assertTrue( paths != null );
    Assert.assertTrue( paths.length == 1 );

    // should have the value at SYSTEM_PATH_IN_SETTINGS_XML
    Assert.assertTrue( paths[ 0 ].equals( SYSTEM_PATH_IN_SETTINGS_XML ) );
  }

  @Test
  public void testReadFilePickerHiddenRepoFoldersSettings() {

    String[] paths = cdfDDSettings.getFilePickerHiddenFoldersByType( CdeSettings.FolderType.REPO );

    // we should have found 1 item
    Assert.assertTrue( paths != null );
    Assert.assertTrue( paths.length == 1 );

    // should have the value at REPO_PATH_IN_SETTINGS_XML
    Assert.assertTrue( paths[ 0 ].equals( REPO_PATH_IN_SETTINGS_XML ) );
  }

  @After
  public void tearDown() throws Exception {
    cdfDDSettings = null;
  }
}
