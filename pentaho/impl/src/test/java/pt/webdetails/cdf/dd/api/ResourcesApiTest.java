/*!
 * Copyright 2019-2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;

import javax.ws.rs.core.Response;

@PowerMockIgnore( "jdk.internal.reflect.*" )
@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, Utils.class } )
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class ResourcesApiTest {

  private ResourcesApi resourcesApi;
  private final static String ALLOWED_EXTENSIONS = "css";
  private final static String RESOURCE_PATH = "public/filePath/file.css";
  private final static String IF_NONE_MATCH = "123456789";
  private final static long LAST_MODIFIED_TIME = 123456789;
  private IBasicFile file;

  @Before
  public void setup() {
    mockStatic( PentahoSystem.class );
    mockStatic( Utils.class );
    file = mock( IBasicFile.class );
    IPluginResourceLoader pluginResourceLoader = mock( IPluginResourceLoader.class );

    when( PentahoSystem.get( IPluginResourceLoader.class, null ) ).thenReturn( pluginResourceLoader );
    when( file.getExtension() ).thenReturn( ALLOWED_EXTENSIONS );

    doReturn( ALLOWED_EXTENSIONS ).when( pluginResourceLoader )
      .getPluginSetting( ResourcesApi.class, CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
  }

  @Test
  public void getCssResourceIfNoneMatchNullTest() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( anyString() );

    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );

    Response response = resourcesApi.getCssResource( RESOURCE_PATH, RESOURCE_PATH, null );

    assertEquals( 200, response.getStatus() );
    assertEquals( "max-age=0", response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( LAST_MODIFIED_TIME ), response.getHeaderString( "Etag" ) );
  }

  @Test
  public void getCssResourceIfNoneMatchNotNullTest() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( anyString() );

    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );

    Response response = resourcesApi.getCssResource( RESOURCE_PATH, RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 304, response.getStatus() );
    assertNull( response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( LAST_MODIFIED_TIME ), response.getHeaderString( "Etag" ) );
  }

  @Test
  public void checkResourceIsNotFoundAndReturnsHttp500() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( anyString() );

    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( null );

    Response response = resourcesApi.resource( RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 500, response.getStatus() );
  }

  @Test
  public void checkResourceIsFoundAndReturnsHttp304() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( anyString() );

    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );

    Response response = resourcesApi.resource( RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 304, response.getStatus() );
  }

}
