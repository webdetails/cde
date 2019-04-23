/*!
 * Copyright 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import org.junit.BeforeClass;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;

import javax.ws.rs.core.Response;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, Utils.class } )
public class ResourcesApiTest {

  private ResourcesApi resourcesApi;
  private String allowedExtensions = "css";

  @Before
  public void setup() {
    mockStatic( PentahoSystem.class );
    mockStatic( Utils.class );
    IPluginResourceLoader pluginResourceLoader = mock( IPluginResourceLoader.class );
    when( PentahoSystem.get( IPluginResourceLoader.class, null ) ).thenReturn( pluginResourceLoader  );
    doReturn( allowedExtensions ).when( pluginResourceLoader ).getPluginSetting( ResourcesApi.class, CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
  }

  @Test
  public void getCssResourceIfNoneMatchNullTest() throws Exception {
    String path = "public/filePath/file.css";
    long lastModifiedTime = 123456789;
    IBasicFile file = mock( IBasicFile.class );

    resourcesApi = spy( new ResourcesApi() );
    doReturn( path ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( lastModifiedTime ).when( resourcesApi ).getLastModifiedTime( anyString() );
    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );
    when( file.getExtension() ).thenReturn( "css" );

    Response response = resourcesApi.getCssResource( path, path, null );

    assertEquals( 200, response.getStatus() );
    assertEquals( "max-age=0", response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( lastModifiedTime ), response.getHeaderString( "Etag" ) );
  }

  @Test
  public void getCssResourceIfNoneMatchNotNullTest() throws Exception {
    String path = "public/filePath/file.css";
    String ifNoneMatch = "123456789";
    long lastModifiedTime = 123456789;
    IBasicFile file = mock( IBasicFile.class );

    resourcesApi = spy( new ResourcesApi() );
    doReturn( path ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( lastModifiedTime ).when( resourcesApi ).getLastModifiedTime( anyString() );
    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );
    when( file.getExtension() ).thenReturn( "css" );

    Response response = resourcesApi.getCssResource( path, path, ifNoneMatch );

    assertEquals( 304, response.getStatus() );
    assertNull( response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( lastModifiedTime ), response.getHeaderString( "Etag" ) );
  }
}