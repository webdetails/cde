///*!
// * Copyright 2019-2024 Webdetails, a Hitachi Vantara company. All rights reserved.
// *
// * This software was developed by Webdetails and is provided under the terms
// * of the Mozilla Public License, Version 2.0, or any later version. You may not use
// * this file except in compliance with the license. If you need a copy of the license,
// * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
// *
// * Software distributed under the Mozilla Public License is distributed on an "AS IS"
// * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
// * the license for the specific language governing your rights and limitations.
// */
//package pt.webdetails.cdf.dd;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.MockedStatic;
//import org.pentaho.platform.api.engine.IParameterProvider;
//import org.pentaho.platform.api.engine.IPluginResourceLoader;
//import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
//import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
//import org.springframework.mock.web.MockHttpServletResponse;
//import pt.webdetails.cdf.dd.api.ResourcesApi;
//import pt.webdetails.cdf.dd.api.XSSHelper;
//import pt.webdetails.cdf.dd.util.CdeEnvironment;
//import pt.webdetails.cdf.dd.util.Utils;
//import pt.webdetails.cpf.repository.api.IBasicFile;
//import pt.webdetails.cpf.repository.api.IRWAccess;
//import pt.webdetails.cpf.repository.api.IUserContentAccess;
//
//import java.io.ByteArrayInputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.mockStatic;
//import static org.mockito.Mockito.spy;
//import static org.mockito.Mockito.when;
//
//
//public class DashboardDesignerContentGeneratorTest {
//
//  private static final String PLUGIN_NAME = "pentaho-cdf-dd";
//  private static final String COMMAND = "test/path/file.json";
//  private DashboardDesignerContentGenerator contentGenerator;
//  private MockHttpServletResponse servletResponse;
//  private IPluginResourceLoader iPluginResourceLoader;
//  private XSSHelper xssHelper;
//
//  private MockedStatic<XSSHelper> xssHelperMockedStatic;
//  private MockedStatic<Utils> utilsMockedStatic;
//  private MockedStatic<Long> longMockedStatic;
//  private MockedStatic<CdeEnvironment> cdeEnvironmentMockedStatic;
//
//  @Before
//  public void setUp() {
//    xssHelperMockedStatic = mockStatic( XSSHelper.class );
//    utilsMockedStatic = mockStatic( Utils.class );
//    longMockedStatic = mockStatic( Long.class );
//    cdeEnvironmentMockedStatic = mockStatic( CdeEnvironment.class );
//
//    xssHelper = mock( XSSHelper.class );
//    iPluginResourceLoader = mock( IPluginResourceLoader.class );
//    contentGenerator = spy( new DashboardDesignerContentGenerator() );
//
//    servletResponse = new MockHttpServletResponse();
//
//  }
//
//  @After
//  public void afterEach() {
//    xssHelperMockedStatic.close();
//    utilsMockedStatic.close();
//    longMockedStatic.close();
//    cdeEnvironmentMockedStatic.close();
//  }
//
//  @Ignore
//  @Test
//  public void createJsonResourceContent() throws Exception {
//    contentGenerator.setParameterProviders( configureParameterProviders() );
//    doReturn( PLUGIN_NAME ).when( contentGenerator ).getPluginName();
//    contentGenerator.setResource( true );
//
//    String expectedJsonContents = "jsonBufferedValues";
//    setUpJsonResourceStatements( expectedJsonContents );
//    contentGenerator.createContent();
//
//    // The expectedJsonContents string is an input into the Response object, then it gets translated over to the
//    // servletResponse's object. this byteStream check verifies the output the servletResponse writes to.
//    assertEquals( expectedJsonContents, servletResponse.getContentAsString() );
//
//    assertEquals( "inline; filename=\"null\"", servletResponse.getHeader( "content-disposition" ) );
//    assertEquals( "application/unknown", servletResponse.getHeader( "Content-Type" ) );
//    assertEquals( 2, servletResponse.getHeaderNames().size() );
//  }
//
//  private Map<String, IParameterProvider> configureParameterProviders() {
//    Map<String, IParameterProvider> parameterProviders = new HashMap<>();
//    SimpleParameterProvider pathProvider = new SimpleParameterProvider();
//    pathProvider.setParameter( "cmd", COMMAND );
//    pathProvider.setParameter( "httpresponse", servletResponse );
//    //parameterProviders.put( "request", new HttpRequestParameterProvider( mock( HttpServletRequest.class ) ) );
//    parameterProviders.put( "path", pathProvider );
//    return parameterProviders;
//  }
//
//  private void setUpJsonResourceStatements( String expectedJsonContents ) throws Exception {
//    IUserContentAccess userContentAccess = mock( IUserContentAccess.class );
//    IRWAccess readWriteAccess = mock( IRWAccess.class );
//    IBasicFile file = mock( IBasicFile.class );
//
//    when( XSSHelper.getInstance() ).thenReturn( xssHelper );
//    when( CdeEnvironment.getUserContentAccess() ).thenReturn( userContentAccess );
//    when( Utils.getAppropriateWriteAccess( COMMAND ) ).thenReturn( readWriteAccess );
//    when( Utils.getFileViaAppropriateReadAccess( COMMAND ) ).thenReturn( file );
//    when( Utils.getURLDecoded( COMMAND ) ).thenReturn( COMMAND );
//
//    when( userContentAccess.getLastModified( COMMAND )).thenReturn( Long.valueOf( "0" ) );
//    doReturn( ".json" ).when( file ).getExtension();
//    doReturn( COMMAND ).when( xssHelper ).escape( COMMAND );
//
//    //Added to set json as an allowed extension for test after removing PentahoSystem mock
//    List<String> allowedExtensions = new ArrayList<>();
//    allowedExtensions.add( "json" );
//    ResourcesApi.setAllowedExtensions( allowedExtensions );
//    doReturn( "json" ).when( iPluginResourceLoader ).getPluginSetting( ResourcesApi.class,
//      "settings/resources/downloadable-formats" );
//    doReturn( null ).when( iPluginResourceLoader ).getPluginSetting( ResourcesApi.class,
//      "max-age" );
//
//    doReturn( new ByteArrayInputStream( expectedJsonContents.getBytes() ) ).when( file ).getContents();
//  }
//
//}
