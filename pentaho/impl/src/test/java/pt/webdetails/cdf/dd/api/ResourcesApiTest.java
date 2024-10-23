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

package pt.webdetails.cdf.dd.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;

import jakarta.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ResourcesApiTest {

  private ResourcesApi resourcesApi;
  private final static String ALLOWED_EXTENSIONS = "css";
  private final static String RESOURCE_PATH = "public/filePath/file.css";
  private final static String IF_NONE_MATCH = "123456789";
  private final static long LAST_MODIFIED_TIME = 123456789;
  private IBasicFile file;

  private MockedStatic<PentahoSystem> pentahoSystemMockedStatic;
  private MockedStatic<Utils> utilsMockedStatic;

  @Before
  public void setup() {
    pentahoSystemMockedStatic = mockStatic( PentahoSystem.class );
    utilsMockedStatic = mockStatic( Utils.class );
    file = mock( IBasicFile.class );
    IPluginResourceLoader pluginResourceLoader = mock( IPluginResourceLoader.class );

    when( PentahoSystem.get( IPluginResourceLoader.class, null ) ).thenReturn( pluginResourceLoader );
    when( file.getExtension() ).thenReturn( ALLOWED_EXTENSIONS );

    doReturn( ALLOWED_EXTENSIONS ).when( pluginResourceLoader )
      .getPluginSetting( ResourcesApi.class, CdeConstants.PLUGIN_SETTINGS_DOWNLOADABLE_FORMATS );
  }

  @After
  public void afterEach() {
    pentahoSystemMockedStatic.close();
    utilsMockedStatic.close();
  }

  @Test
  public void getCssResourceIfNoneMatchNullTest() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( any() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( any() );

    when( Utils.getFileViaAppropriateReadAccess( any() ) ).thenReturn( file );
    //Added to set allowed extensions
    ResourcesApi.setAllowedExtensions( Collections.singletonList( file.getExtension() ) );
    Response response = resourcesApi.getCssResource( RESOURCE_PATH, RESOURCE_PATH, null );

    assertEquals( 200, response.getStatus() );
    assertEquals( "max-age=0", response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( LAST_MODIFIED_TIME ), response.getHeaderString( "Etag" ) );
  }

  @Test
  public void getCssResourceIfNoneMatchNotNullTest() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( any() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( any() );

    when( Utils.getFileViaAppropriateReadAccess( any() ) ).thenReturn( file );
    //Added to set allowed extensions
    ResourcesApi.setAllowedExtensions( Collections.singletonList( file.getExtension() ) );
    Response response = resourcesApi.getCssResource( RESOURCE_PATH, RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 304, response.getStatus() );
    assertNull( response.getHeaderString( "Cache-Control" ) );
    assertEquals( Long.toString( LAST_MODIFIED_TIME ), response.getHeaderString( "Etag" ) );
  }

  @Test
  public void checkResourceIsNotFoundAndReturnsHttp500() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( any() );

    when( Utils.getFileViaAppropriateReadAccess( any() ) ).thenReturn( null );
    //Added to set allowed extensions
    ResourcesApi.setAllowedExtensions( Collections.singletonList( file.getExtension() ) );
    Response response = resourcesApi.resource( RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 500, response.getStatus() );
  }

  @Test
  public void checkResourceIsFoundAndReturnsHttp304() throws Exception {

    resourcesApi = spy( new ResourcesApi() );

    doReturn( RESOURCE_PATH ).when( resourcesApi ).decodeAndEscape( any() );
    doReturn( LAST_MODIFIED_TIME ).when( resourcesApi ).getLastModifiedTime( any() );

    when( Utils.getFileViaAppropriateReadAccess( any() ) ).thenReturn( file );
    //Added to set allowed extensions
    ResourcesApi.setAllowedExtensions( Collections.singletonList( file.getExtension() ) );
    Response response = resourcesApi.resource( RESOURCE_PATH, IF_NONE_MATCH );

    assertEquals( 304, response.getStatus() );
  }

}
