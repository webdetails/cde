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

package pt.webdetails.cdf.dd.util;

import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static junit.framework.Assert.assertEquals;


public class UtilsTest {


  private ICdeEnvironment environment;
  private IContentAccessFactory factory;

  private IUserContentAccess userContentAccess;
  private IReadAccess systemReadAccess;
  private IRWAccess systemWriteAccess;
  private IBasicFile basicFile;

  private static final String SYSTEM_DIR = "system";
  private static final String REPOS_DIR = "/public/cde";
  private static final String PLUGIN_ID = "pentaho-cdf-dd";
  private static final String OTHER_PLUGIN_ID = "otherPlugin";


  private static final String STATIC_PATH = "/staticPath/file.css";
  private static final String SYSTEM_PLUGIN_PATH = "/system/pentaho-cdf-dd/file.css";
  private static final String SYSTEM_OTHER_PLUGIN_PATH = "/system/otherPlugin/file.css";
  private static final String REPOS_PATH = "/public/reposPath/file.css";

  @Before
  public void setUp() throws Exception {

    environment = mock( ICdeEnvironment.class );
    factory = mock( IContentAccessFactory.class );
    systemReadAccess = mock( IReadAccess.class );
    systemWriteAccess = mock( IRWAccess.class );
    userContentAccess = mock( IUserContentAccess.class );
    basicFile = mock( IBasicFile.class );

    //mocking environment calls
    doReturn( factory ).when( environment ).getContentAccessFactory();
    doReturn( SYSTEM_DIR ).when( environment ).getSystemDir();
    doReturn( REPOS_DIR ).when( environment ).getPluginRepositoryDir();
    doReturn( PLUGIN_ID ).when( environment ).getPluginId();

    //mocking factory and readAccess calls
    doReturn( userContentAccess ).when( factory ).getUserContentAccess( any() );
    doReturn( basicFile ).when( userContentAccess ).fetchFile( any() );

    doReturn( systemReadAccess ).when( factory ).getPluginSystemReader( any() );
    doReturn( systemReadAccess ).when( factory )
      .getOtherPluginSystemReader( any(), any() );
    doReturn( basicFile ).when( systemReadAccess ).fetchFile( any() );

    doReturn( systemWriteAccess ).when( factory ).getPluginSystemWriter( any() );
    doReturn( systemWriteAccess ).when( factory )
      .getOtherPluginSystemWriter( any(), any() );

  }

  /* Utils.getSystemReadAccess Tests */

  @Test
  public void testGetSystemReadAccessSystemPlugin() throws Exception {
    IReadAccess result = Utils.getSystemReadAccess( "", null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemReader( null );
    assertEquals( systemReadAccess, result );
  }

  @Test
  public void testGetSystemReadAccessOtherPluginPath() throws Exception {
    IReadAccess result = Utils.getSystemReadAccess( OTHER_PLUGIN_ID, null, environment );
    verify( factory, atLeastOnce() ).getOtherPluginSystemReader( OTHER_PLUGIN_ID, null );
    assertEquals( systemReadAccess, result );
  }

  /* Utils.getSystemRWAccess Tests */

  @Test
  public void testGetSystemRWAccessSystemPlugin() throws Exception {
    IRWAccess result = Utils.getSystemRWAccess( "", null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemWriter( null );
    assertEquals( systemWriteAccess, result );
  }

  @Test
  public void testGetSystemRWAccessOtherPluginPath() throws Exception {
    IRWAccess result = Utils.getSystemRWAccess( OTHER_PLUGIN_ID, null, environment );
    verify( factory, atLeastOnce() ).getOtherPluginSystemWriter( OTHER_PLUGIN_ID, null );
    assertEquals( systemWriteAccess, result );
  }

  /* Utils.getAppropriateReadAccess Tests */

  @Test
  public void testGetAppropriateReadAccessEmptyPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IReadAccess result = Utils.getAppropriateReadAccess( "", null, environment );
    assertEquals( null, result );
  }

  @Test
  public void testGetAppropriateReadAccessStaticPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IReadAccess result = Utils.getAppropriateReadAccess( STATIC_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemReader( null );
    assertEquals( systemReadAccess, result );
  }

  @Test
  public void testGetAppropriateReadAccessSystemPluginPath() throws Exception {
    IReadAccess result = Utils.getAppropriateReadAccess( SYSTEM_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemReader( null );
    assertEquals( systemReadAccess, result );
  }

  @Test
  public void testGetAppropriateReadAccessSystemOtherPluginPath() throws Exception {
    IReadAccess result = Utils.getAppropriateReadAccess( SYSTEM_OTHER_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getOtherPluginSystemReader( "otherPlugin", null );
    assertEquals( systemReadAccess, result );
  }

  @Test
  public void testGetAppropriateReadAccessReposPath() throws Exception {
    IReadAccess result = Utils.getAppropriateReadAccess( REPOS_PATH, null, environment );
    verify( factory, atLeastOnce() ).getUserContentAccess( null );
    assertEquals( userContentAccess, result );
  }

  /* Utils.getAppropriateWriteAccess Tests */

  @Test
  public void testGetAppropriateWriteAccessEmptyPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IRWAccess result = Utils.getAppropriateWriteAccess( "", null, environment );
    assertEquals( null, result );
  }

  @Test
  public void testGetAppropriateWriteAccessStaticPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IRWAccess result = Utils.getAppropriateWriteAccess( STATIC_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemWriter( null );
    assertEquals( systemWriteAccess, result );
  }

  @Test
  public void testGetAppropriateWriteAccessSystemPluginPath() throws Exception {
    IRWAccess result = Utils.getAppropriateWriteAccess( SYSTEM_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemWriter( null );
    assertEquals( systemWriteAccess, result );
  }

  @Test
  public void testGetAppropriateWriteAccessSystemOtherPluginPath() throws Exception {
    IRWAccess result = Utils.getAppropriateWriteAccess( SYSTEM_OTHER_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getOtherPluginSystemWriter( "otherPlugin", null );
    assertEquals( systemWriteAccess, result );
  }

  @Test
  public void testGetAppropriateWriteAccessReposPath() throws Exception {
    IRWAccess result = Utils.getAppropriateWriteAccess( REPOS_PATH, null, environment );
    verify( factory, atLeastOnce() ).getUserContentAccess( null );
    assertEquals( userContentAccess, result );
  }

  /* Utils.getFileViaAppropriateReadAccess Tests */

  @Test
  public void testGetFileViaAppropriateReadAccessEmptyPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( "", null, environment );
    assertEquals( null, result );
  }

  @Test
  public void testGetFileViaAppropriateReadAccessStaticPath() throws Exception {
    doReturn( true ).when( systemReadAccess ).fileExists( any( String.class ) );
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( STATIC_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemReader( null );
    verify( systemReadAccess, atLeastOnce() ).fetchFile( "staticPath/file.css" );
    assertEquals( basicFile, result );
  }

  @Test
  public void testGetFileViaAppropriateReadAccessSystemPluginPath() throws Exception {
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( SYSTEM_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getPluginSystemReader( null );
    verify( systemReadAccess, atLeastOnce() ).fetchFile( "file.css" );
    assertEquals( basicFile, result );
  }

  @Test
  public void testGetFileViaAppropriateReadAccessSystemOtherPluginPath() throws Exception {
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( SYSTEM_OTHER_PLUGIN_PATH, null, environment );
    verify( factory, atLeastOnce() ).getOtherPluginSystemReader( "otherPlugin", null );
    verify( systemReadAccess, atLeastOnce() ).fetchFile( "/file.css" );
    assertEquals( basicFile, result );
  }

  @Test
  public void testGetFileViaAppropriateReadAccessReposPath() throws Exception {
    doReturn( true ).when( userContentAccess ).fileExists( any( String.class ) );
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( REPOS_PATH, null, environment );
    verify( factory, atLeastOnce() ).getUserContentAccess( null );
    assertEquals( basicFile, result );
  }

  @Test
  public void testGetFileViaAppropriateReadAccessFileDoesNotExists() throws Exception {
    doReturn( false ).when( userContentAccess ).fileExists( any( String.class ) );
    IBasicFile result = Utils.getFileViaAppropriateReadAccess( REPOS_PATH, null, environment );
    verify( factory, atLeastOnce() ).getUserContentAccess( null );
    assertEquals( null, result );
  }

}
