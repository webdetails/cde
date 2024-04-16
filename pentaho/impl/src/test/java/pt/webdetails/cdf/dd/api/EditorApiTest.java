/*!
 * Copyright 2002 - 2024 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.api;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditorApiTest {

  private static EditorApiForTesting editorApi;
  private final String FAKE_PATH = "/fake/path/";
  private final String FAKE_FILE = "fakeFile.css";
  private static XSSHelper originalHelper;
  private static XSSHelper mockHelper;

  @BeforeClass
  public static void setUp() {
    originalHelper = XSSHelper.getInstance();
  }

  @Before
  public void beforeEach() throws Exception {
    editorApi = new EditorApiForTesting();
    mockHelper = mock( XSSHelper.class );
    when( mockHelper.escape( any() ) ).thenAnswer( invocation -> invocation.getArguments()[ 0 ] );
    XSSHelper.setInstance( mockHelper );
  }

  @AfterClass
  public static void tearDown() {
    XSSHelper.setInstance( originalHelper );
  }

  @After
  public void afterEach() {
    reset( mockHelper );
  }

  @Test
  public void testCreateFile() throws IOException {

    String MESSAGE_OK = "file '" + FAKE_PATH + FAKE_FILE + "' saved ok";
    String MESSAGE_ERROR = "error saving file " + FAKE_PATH + FAKE_FILE;
    String MESSAGE_NO_PERMISSIONS = "no permissions to write file " + FAKE_PATH + FAKE_FILE;

    editorApi.initMockResourceLoader();

    editorApi.setHasAccess( true );
    editorApi.setSavedFile( true );
    String hasAccessSaveTrue = editorApi.createFile( FAKE_PATH + FAKE_FILE, "", null );

    editorApi.setHasAccess( true );
    editorApi.setSavedFile( false );
    String hasAccessSaveFalse = editorApi.createFile( FAKE_PATH + FAKE_FILE, "", null );

    editorApi.setHasAccess( false );
    editorApi.setSavedFile( true );
    String noAccessSaveTrue = editorApi.createFile( FAKE_PATH + FAKE_FILE, "", null );

    editorApi.setHasAccess( false );
    editorApi.setSavedFile( false );
    String noAccessSaveFalse = editorApi.createFile( FAKE_PATH + FAKE_FILE, "", null );

    assertEquals( MESSAGE_OK, hasAccessSaveTrue );
    assertEquals( MESSAGE_ERROR, hasAccessSaveFalse );
    assertEquals( MESSAGE_NO_PERMISSIONS, noAccessSaveTrue );
    assertEquals( MESSAGE_NO_PERMISSIONS, noAccessSaveFalse );
    verify( mockHelper, atLeastOnce() ).escape( any() );
  }

}
