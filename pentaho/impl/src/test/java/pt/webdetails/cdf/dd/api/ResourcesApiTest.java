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
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.utils.PluginIOUtils;

import javax.servlet.http.HttpServletResponse;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, Utils.class, PluginIOUtils.class } )
public class ResourcesApiTest {

  private ResourcesApi resourcesApi;
  private String allowedExtensions = "css";

  @Before
  public void setup() {
    mockStatic( PentahoSystem.class );
    mockStatic( Utils.class );
    mockStatic( PluginIOUtils.class );
    IPluginResourceLoader pluginResourceLoader = mock( IPluginResourceLoader.class );
    when( PentahoSystem.get( IPluginResourceLoader.class, null ) ).thenReturn( pluginResourceLoader  );
    doReturn( allowedExtensions ).when( pluginResourceLoader ).getPluginSetting( ResourcesApi.class, CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
  }

  @Test
  public void getCssResourceIfNoneMatchNullTest() throws Exception {
    String path = "public/filePath/file.css";
    long lastModifiedTime = 123456789;
    IBasicFile file = mock( IBasicFile.class );
    HttpServletResponse response = mock( HttpServletResponse.class );

    resourcesApi = spy( new ResourcesApi() );
    doReturn( path ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( lastModifiedTime ).when( resourcesApi ).getLastModifiedTime( anyString() );
    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );
    when( file.getExtension() ).thenReturn( "css" );

    resourcesApi.getCssResource( path, path, response, null );

    verify( response, Mockito.times( 1 ) ).setStatus( response.SC_OK );
    verify( response, Mockito.times( 1 ) ).setHeader( "Cache-Control", "max-age=0" );
    verify( response, Mockito.times( 1 ) ).setHeader( "Etag", Long.toString( lastModifiedTime ) );
  }

  @Test
  public void getCssResourceIfNoneMatchNotNullTest() throws Exception {
    String path = "public/filePath/file.css";
    String ifNoneMatch = "123456789";
    long lastModifiedTime = 123456789;
    IBasicFile file = mock( IBasicFile.class );
    HttpServletResponse response = mock( HttpServletResponse.class );

    resourcesApi = spy( new ResourcesApi() );
    doReturn( path ).when( resourcesApi ).decodeAndEscape( anyString() );
    doReturn( lastModifiedTime ).when( resourcesApi ).getLastModifiedTime( anyString() );
    when( Utils.getFileViaAppropriateReadAccess( anyString() ) ).thenReturn( file );
    when( file.getExtension() ).thenReturn( "css" );

    resourcesApi.getCssResource( path, path, response, ifNoneMatch );

    verify( response, Mockito.times( 1 ) ).setStatus( response.SC_NOT_MODIFIED );
    verify( response, Mockito.times( 0 ) ).setHeader( "Cache-Control", "max-age=0" );
    verify( response, Mockito.times( 1 ) ).setHeader( "Etag", Long.toString( lastModifiedTime ) );
  }
}
