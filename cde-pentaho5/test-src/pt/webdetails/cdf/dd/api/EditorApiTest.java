/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company. All rights reserved.
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

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import org.mockito.Mockito;

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
    mockHelper = Mockito.mock( XSSHelper.class );
    Mockito.when( mockHelper.escape( Mockito.anyString() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[ 0 ];
      }
    } );
    XSSHelper.setInstance( mockHelper );
  }

  @AfterClass
  public static void tearDown() {
    XSSHelper.setInstance( originalHelper );
  }

  @After
  public void afterEach() {
    Mockito.reset( mockHelper );
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

    Assert.assertEquals( MESSAGE_OK, hasAccessSaveTrue );
    Assert.assertEquals( MESSAGE_ERROR, hasAccessSaveFalse );
    Assert.assertEquals( MESSAGE_NO_PERMISSIONS, noAccessSaveTrue );
    Assert.assertEquals( MESSAGE_NO_PERMISSIONS, noAccessSaveFalse );
    Mockito.verify( mockHelper, Mockito.atLeastOnce() ).escape( Mockito.anyString() );
  }

}
