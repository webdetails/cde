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
package pt.webdetails.cdf.dd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pt.webdetails.cdf.dd.api.ResourcesApi;
import pt.webdetails.cdf.dd.api.XSSHelper;
import pt.webdetails.cdf.dd.testUtils.StringHeaderHttpServletResponseForTests;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class, Utils.class, XSSHelper.class, CdeEnvironment.class } )
public class DashboardDesignerContentGeneratorTest {

  private static final String PLUGIN_NAME = "pentaho-cdf-dd";
  private static final String COMMAND = "test/path/file.json";
  private ByteArrayOutputStream byteStream;
  private DashboardDesignerContentGenerator contentGenerator;
  private HttpServletResponse servletResponse;
  private IPluginResourceLoader iPluginResourceLoader;
  private XSSHelper xssHelper;

  @Before
  public void setUp() {
    mockStatic( PentahoSystem.class );
    mockStatic( XSSHelper.class );
    mockStatic( Utils.class );
    mockStatic( Long.class );
    mockStatic( CdeEnvironment.class );

    xssHelper = mock( XSSHelper.class );
    iPluginResourceLoader = mock( IPluginResourceLoader.class );
    contentGenerator = spy( new DashboardDesignerContentGenerator() );
    byteStream = new ByteArrayOutputStream();

    servletResponse = new StringHeaderHttpServletResponseForTests( byteStream );
  }

  @Test
  public void createJsonResourceContent() throws Exception {
    contentGenerator.setParameterProviders( configureParameterProviders() );
    doReturn( PLUGIN_NAME ).when( contentGenerator ).getPluginName();
    contentGenerator.setResource( true );

    String expectedJsonContents = "jsonBufferedValues";
    setUpJsonResourceStatements( expectedJsonContents );
    contentGenerator.createContent();

    // The expectedJsonContents string is an input into the Response object, then it gets translated over to the
    // servletResponse's object. this byteStream check verifies the output the servletResponse writes to.
    assertEquals( expectedJsonContents, byteStream.toString() );

    assertEquals( "inline; filename=\"null\"", servletResponse.getHeader( "content-disposition" ) );
    assertEquals( "application/unknown", servletResponse.getHeader( "Content-Type" ) );
    assertEquals( 2, servletResponse.getHeaderNames().size() );
  }

  private Map<String, IParameterProvider> configureParameterProviders() {
    Map<String, IParameterProvider> parameterProviders = new HashMap<>();
    SimpleParameterProvider pathProvider = new SimpleParameterProvider();
    pathProvider.setParameter( "cmd", COMMAND );
    pathProvider.setParameter( "httpresponse", servletResponse );
    parameterProviders.put( "request", new HttpRequestParameterProvider( mock( HttpServletRequest.class ) ) );
    parameterProviders.put( "path", pathProvider );
    return parameterProviders;
  }

  private void setUpJsonResourceStatements( String expectedJsonContents ) throws Exception {
    IUserContentAccess userContentAccess = mock( IUserContentAccess.class );
    IRWAccess readWriteAccess = mock( IRWAccess.class );
    IBasicFile file = mock( IBasicFile.class );

    when( XSSHelper.getInstance() ).thenReturn( xssHelper );
    when( CdeEnvironment.getUserContentAccess() ).thenReturn( userContentAccess );
    when( Utils.getAppropriateWriteAccess( COMMAND ) ).thenReturn( readWriteAccess );
    when( Utils.getFileViaAppropriateReadAccess( COMMAND ) ).thenReturn( file );
    when( Utils.getURLDecoded( COMMAND ) ).thenReturn( COMMAND );

    doReturn( 0L ).when( userContentAccess ).getLastModified( COMMAND );
    when( Long.toString( anyLong() ) ).thenReturn( "0" );
    doReturn( ".json" ).when( file ).getExtension();
    doReturn( COMMAND ).when( xssHelper ).escape( COMMAND );

    when( PentahoSystem.get( IPluginResourceLoader.class, null ) ).thenReturn( iPluginResourceLoader );
    doReturn( "json" ).when( iPluginResourceLoader ).getPluginSetting( ResourcesApi.class,
      "settings/resources/downloadable-formats" );
    doReturn( null ).when( iPluginResourceLoader ).getPluginSetting( ResourcesApi.class,
      "max-age" );

    doReturn( new ByteArrayInputStream( expectedJsonContents.getBytes() ) ).when( file ).getContents();
  }

}
