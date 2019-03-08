/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngineForTests;
import pt.webdetails.cdf.dd.CdeEnvironmentForTests;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.messaging.MockHttpServletRequest;
import pt.webdetails.cpf.messaging.MockHttpServletResponse;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpf.utils.CharsetHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockito.Mockito.*;

public class RenderApiTest {

  private static RenderApi renderApi;
  private static final String USER_DIR = System.getProperty( "user.dir" );
  private static final String TEST_RESOURCES = USER_DIR + File.separator + "src" + File.separator + "test" + File.separator + "resources";
  private static final String DUMMY_WCDF =
    TEST_RESOURCES + File.separator + "dummyDashboard" + File.separator + "dummy.wcdf";
  private static final String DUMMY_REQUIRE_WCDF =
    TEST_RESOURCES + File.separator + "dummyDashboard" + File.separator + "dummyRequire.wcdf";
  private static final String PROPERTY_NAME =
    TEST_RESOURCES + File.separator + "resources" + File.separator + "properties"
      + File.separator + "Name.xml";
  private static final String STYLE_CLEAN =
    TEST_RESOURCES + File.separator + "resources" + File.separator + "styles" + File.separator + "Clean.html";
  private static final String DEFAULT_ROOT = "http://localhost:8080/";

  private static CdeEnvironmentForTests cdeEnvironmentForTests;
  private static IUserContentAccess mockedUserContentAccess;
  private static HttpServletRequest mockedHttpServletRequest;
  private static XSSHelper originalHelper;
  private static XSSHelper mockHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    final File propertyName = new File( PROPERTY_NAME );
    List<IBasicFile> properties = new ArrayList<IBasicFile>();
    properties.add( getBasicFileFromFile( propertyName ) );

    //mock IUserContentAccess
    mockedUserContentAccess = mock( IUserContentAccess.class );
    when( mockedUserContentAccess.fileExists( anyString() ) ).thenReturn( true );
    when( mockedUserContentAccess.fetchFile( anyString() ) )
      .thenAnswer( new Answer<IBasicFile>() {
        @Override
        public IBasicFile answer( InvocationOnMock invocationOnMock ) throws Throwable {
          File file = new File( (String) invocationOnMock.getArguments()[ 0 ] );
          return getBasicFileFromFile( file );
        }
      } );
    when( mockedUserContentAccess.hasAccess( anyString(), any( FileAccess.class ) ) )
      .thenReturn( true );
    when( mockedUserContentAccess.getFileInputStream( anyString() ) )
      .thenAnswer( new Answer<InputStream>() {
        @Override
        public InputStream answer( InvocationOnMock invocationOnMock ) throws Throwable {
          return getInputStreamFromFileName( (String) invocationOnMock.getArguments()[ 0 ] );
        }
      } );

    //mock IReadAccess
    IReadAccess mockedReadAccess = mock( IReadAccess.class );
    when( mockedReadAccess.listFiles( anyString(), any( IBasicFileFilter.class ), anyInt() ) )
      .thenReturn( properties );
    when( mockedReadAccess.getFileInputStream( anyString() ) ).thenAnswer( new Answer<InputStream>() {
      @Override
      public InputStream answer( InvocationOnMock invocationOnMock ) throws Throwable {
        return getInputStreamFromFileName( (String) invocationOnMock.getArguments()[ 0 ] );
      }
    } );

    //mock IRWAccess
    IRWAccess mockedRWAccess = mock( IRWAccess.class );

    //mock IPluginResourceLocationManager
    IPluginResourceLocationManager mockedPluginResourceLocationManager =
      mock( IPluginResourceLocationManager.class );
    when( mockedPluginResourceLocationManager.getStyleResourceLocation( anyString() ) )
      .thenReturn( STYLE_CLEAN );

    JSONObject dataSourceDefinition = new JSONObject( "{ \"scriptable_scripting\": {"
      + "\"metadata\": {"
      + "\"name\": \"scriptable over scripting\","
      + "\"conntype\": \"scripting.scripting\","
      + "\"datype\": \"scriptable\","
      + "\"group\": \"SCRIPTING\","
      + "\"groupdesc\": \"SCRIPTING Queries\"},"
      + "\"definition\": {"
      + "\"connection\": {"
      + "\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},"
      + "\"language\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},"
      + "\"initscript\": {\"type\": \"STRING\", \"placement\": \"CHILD\"}},"
      + "\"dataaccess\": {"
      + "\"id\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},"
      + "\"access\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},"
      + "\"parameters\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},"
      + "\"output\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},"
      + "\"columns\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"},"
      + "\"query\": {\"type\": \"STRING\", \"placement\": \"CHILD\"},"
      + "\"connection\": {\"type\": \"STRING\", \"placement\": \"ATTRIB\"},"
      + "\"cache\": {\"type\": \"BOOLEAN\", \"placement\": \"CHILD\"},"
      + "\"cacheDuration\": {\"type\": \"NUMERIC\", \"placement\": \"ATTRIB\"},"
      + "\"cacheKeys\": {\"type\": \"ARRAY\", \"placement\": \"CHILD\"}}}}}" );
    //mock IDataSourceProvider
    IDataSourceProvider ds = mock( IDataSourceProvider.class );
    when( ds.getId() ).thenReturn( "cda" );
    List<IDataSourceProvider> dataSourceProviders = new ArrayList<IDataSourceProvider>();
    dataSourceProviders.add( ds );

    //mock IDataSourceManager
    IDataSourceManager mockedDataSourceManager = mock( IDataSourceManager.class );
    when( mockedDataSourceManager.getProviders() ).thenReturn( dataSourceProviders );
    when( mockedDataSourceManager.getProviderJsDefinition( anyString() ) ).thenReturn( dataSourceDefinition );

    //mock IUrlProvider
    IUrlProvider mockedUrlProvider = mock( IUrlProvider.class );
    when( mockedUrlProvider.getWebappContextRoot() ).thenReturn( DEFAULT_ROOT );

    //mock IUserSession
    IUserSession mockedUserSession = mock( IUserSession.class );
    when( mockedUserSession.isAdministrator() ).thenReturn( false );

    // mock ICdeApiPathProvider
    ICdeApiPathProvider mockedCdeApiPathProvider = mock( ICdeApiPathProvider.class );
    when( mockedCdeApiPathProvider.getPluginStaticBaseUrl() ).thenReturn( "/" );

    cdeEnvironmentForTests = new CdeEnvironmentForTests();
    cdeEnvironmentForTests.setMockedContentAccess( mockedUserContentAccess );
    cdeEnvironmentForTests.setMockedReadAccess( mockedReadAccess );
    cdeEnvironmentForTests.setMockedRWAccess( mockedRWAccess );
    cdeEnvironmentForTests.setMockedPluginResourceLocationManager( mockedPluginResourceLocationManager );
    cdeEnvironmentForTests.setMockedDataSourceManager( mockedDataSourceManager );
    cdeEnvironmentForTests.setMockedUrlProvider( mockedUrlProvider );
    cdeEnvironmentForTests.setMockedUserSession( mockedUserSession );
    cdeEnvironmentForTests.setMockedCdeApiPathProvider( mockedCdeApiPathProvider );

    renderApi = new RenderApiForTesting( cdeEnvironmentForTests );
    new CdeEngineForTests( cdeEnvironmentForTests );

    mockedHttpServletRequest = mock( HttpServletRequest.class );
    when( mockedHttpServletRequest.getParameterMap() ).thenReturn( new HashMap() );
    originalHelper = XSSHelper.getInstance();
  }

  @AfterClass
  public static void tearDown() {
    XSSHelper.setInstance( originalHelper );
  }

  @Before
  public void beforeEach() {
    mockHelper = mock( XSSHelper.class );
    when( mockHelper.escape( any() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[ 0 ];
      }
    } );
    XSSHelper.setInstance( mockHelper );
  }

  @After
  public void afterEach() {
    reset( mockHelper );
  }

  @Test
  public void testGetHeaders() throws IOException, ThingWriteException {
    //case1 -> absolute=true&root=(empty)
    String case1 =
      doGetHeadersCase( "", true, "http" );

    //case2 -> absolute=false&root=localhost:8080
    String case2 =
      doGetHeadersCase( "testRoot", false, "http" );

    //case3 -> absolute=true&root=localhost:8080
    String case3 =
      doGetHeadersCase( "testRoot", true, "http" );

    //case4 -> absolute=false&root=(empty)
    String case4 =
      doGetHeadersCase( "", false, "http" );

    //case5 -> absolute=true&root=localhost:8080&scheme=https
    String case5 =
      doGetHeadersCase( "testRoot", true, "https" );

    Assert.assertTrue( case1.contains( "http://localhost:8080/js/CDF.js" )
      && case1.contains( "http://localhost:8080/css/CDF-CSS.css" ) );
    Assert.assertTrue( case2.contains( "/js/CDF.js" ) && case2.contains( "/css/CDF-CSS.css" ) );
    Assert.assertTrue( case3.contains( "http://testRoot/js/CDF.js" )
      && case3.contains( "http://testRoot/css/CDF-CSS.css" ) );
    Assert.assertTrue( case4.contains( "/js/CDF.js" ) && case4.contains( "/css/CDF-CSS.css" ) );
    Assert.assertTrue( case5.contains( "https://testRoot/js/CDF.js" )
      && case5.contains( "https://testRoot/css/CDF-CSS.css" ) );
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testContextConfigurationsInjectionInRender() throws IOException, ThingWriteException {

    // with requirejs dashboards

    String reqCase1 = doRenderCase( DUMMY_REQUIRE_WCDF );
    Assert.assertTrue( reqCase1.contains( "new Dashboard({})" ) );

    String requireContextConfiguration = "{\"context\": \"test\"}";
    RenderApiForTesting.cdfRequireContextConfiguration = requireContextConfiguration;
    String reqCase2 = doRenderCase( DUMMY_REQUIRE_WCDF );
    Assert.assertTrue( reqCase2.contains( "new Dashboard(" + requireContextConfiguration + ")" ) );

    // with legacy dashboards

    String legacyCase1 = doRenderCase( DUMMY_WCDF );
    Assert.assertTrue( legacyCase1.contains( "Dashboards.init();" ) );

    String cdfContextConfiguration = "{\"context\": \"test\"}";
    RenderApiForTesting.cdfContext = cdfContextConfiguration;
    String legacyCase2 = doRenderCase( DUMMY_WCDF );
    Assert.assertTrue( legacyCase2.contains( cdfContextConfiguration ) );

    resetContextConfigurations();
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testContextConfigurationsInjectionInGetDashboard() throws IOException, ThingWriteException {
    String alias = "test_alias";
    String case1 = doGetDashboardCase( "" );
    String case2 = doGetDashboardCase( alias );
    Assert.assertTrue( case1.contains( "$.extend(extendedOpts, {}, opts);" ) );
    Assert.assertTrue( case2.contains( "$.extend(extendedOpts, {}, opts);" ) );
    // empty alias, so layout will contain the alias tag to be replaced
    Assert.assertTrue( case1.contains( CdeConstants.DASHBOARD_ALIAS_TAG ) );
    // alias provided, layout will not contain the alias tag, it will instead contain the alias provided
    Assert.assertFalse( case2.contains( CdeConstants.DASHBOARD_ALIAS_TAG ) );
    Assert.assertTrue( case2.contains( alias ) );

    String requireContextConfiguration = "{\"context\": \"test\"}";
    RenderApiForTesting.cdfRequireContextConfiguration = requireContextConfiguration;
    String case3 = doGetDashboardCase( "" );
    String case4 = doGetDashboardCase( alias );
    Assert.assertTrue( case3.contains( "$.extend(extendedOpts, " + requireContextConfiguration + ", opts);" ) );
    Assert.assertTrue( case4.contains( "$.extend(extendedOpts, " + requireContextConfiguration + ", opts);" ) );

    String cdfRequireContext = "cdf-require-context-for-tests";
    RenderApiForTesting.cdfRequireContext = cdfRequireContext;
    String case5 = doGetDashboardCase( "" );
    String case6 = doGetDashboardCase( alias );
    // cdfRequireContext will not be injected in getDashboard
    Assert.assertEquals( case3, case5 );
    Assert.assertEquals( case4, case6 );

    resetContextConfigurations();
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testGetDashboardParameters() throws IOException {
    MockHttpServletRequest servletRequest =
      new MockHttpServletRequest( "pentaho-cdf-dd/api/renderer", (Map) new HashMap<String, String[]>() );
    MockHttpServletResponse servletResponse =
      new MockHttpServletResponse( new ObjectOutputStream( new ByteArrayOutputStream() ) );
    servletResponse.setContentType( null );
    servletResponse.setCharacterEncoding( null );
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    String parameters = renderApi.getDashboardParameters( DUMMY_WCDF, false, false, servletRequest, servletResponse );
    String expected = "{\"parameters\":[\"dummyComponent\"]}";
    Assert.assertEquals( "Dummy Dashboard has a SimpleParameter - dummyComponent",
      expected, parameters.replace( " ", "" ).replace( "\n", "" ) );

    Assert.assertTrue( servletResponse.getContentType().equals( APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testGetDashboardDataSources() throws IOException, JSONException {
    MockHttpServletRequest servletRequest =
      new MockHttpServletRequest( "pentaho-cdf-dd/api/renderer", (Map) new HashMap<String, String[]>() );
    MockHttpServletResponse servletResponse =
      new MockHttpServletResponse( new ObjectOutputStream( new ByteArrayOutputStream() ) );
    servletResponse.setContentType( null );
    servletResponse.setCharacterEncoding( null );
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    String parameters = renderApi.getDashboardDatasources( DUMMY_WCDF, false, servletRequest, servletResponse );
    String expected = "{\"dataSources\":[\"dummyDatasource\"]}";
    Assert.assertEquals( "Dummy Dashboard has a data source - dummyDatasource",
      expected, parameters.replace( " ", "" ).replace( "\n", "" ) );

    Assert.assertTrue( servletResponse.getContentType().equals( APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testEditDashboardFailPermissions() throws Exception {
    cdeEnvironmentForTests.setCanCreateContent( false );
    String expected = "This functionality is limited to users with permission 'Create Content'";
    Assert.assertEquals( expected, renderApi.edit( "", "/path/to/dashboard.wcdf", "", true, true, null, null ) );

    IUserContentAccess testPermContentAccess = mock( IUserContentAccess.class );
    when( testPermContentAccess.fileExists( anyString() ) ).thenReturn( true );
    when( testPermContentAccess.hasAccess( anyString(), any( FileAccess.class ) ) ).thenReturn( false );
    cdeEnvironmentForTests.setMockedContentAccess( testPermContentAccess );
    cdeEnvironmentForTests.setCanCreateContent( true );
    expected = "Access Denied or file not found - /path/to/dashboard.wcdf";
    Assert.assertEquals( expected, renderApi.edit( "", "/path/to/dashboard.wcdf", "", true, true, null, null ) );

    cdeEnvironmentForTests.setMockedContentAccess( mockedUserContentAccess );
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  @Test
  public void testNewDashboardFailPermissions() throws Exception {
    cdeEnvironmentForTests.setCanCreateContent( false );
    String expected = "This functionality is limited to users with permission 'Create Content'";
    Assert.assertEquals( expected, renderApi.newDashboard( "", false, false, null, null ) );
    verify( mockHelper, atLeastOnce() ).escape( anyString() );
  }

  private String doGetHeadersCase( String root,
                                   boolean absolute, String scheme )
    throws IOException, ThingWriteException {
    return renderApi
      .getHeaders( "", "", DUMMY_WCDF, false, root, absolute, true, false, scheme, null, null );
  }

  private String doRenderCase( String wcdf ) throws IOException, ThingWriteException {
    return renderApi.render( "", "", wcdf, false, "", false, true, false, "http", "", "", mockedHttpServletRequest );
  }

  private String doGetDashboardCase( String alias )
    throws IOException, ThingWriteException {
    return renderApi.getDashboard( DUMMY_REQUIRE_WCDF, false, "", false, true, false, "http", "", "", alias,
      mockedHttpServletRequest );
  }

  private void resetContextConfigurations() {
    RenderApiForTesting.cdfRequireContextConfiguration = "";
    RenderApiForTesting.cdfRequireContext = "";
    RenderApiForTesting.cdfContext = "";
  }

  public static InputStream getInputStreamFromFileName( String fileName ) throws FileNotFoundException {
    File file = new File( fileName );
    if ( !file.isAbsolute() ) {
      file = new File( TEST_RESOURCES + File.separator + file.getPath() );
    }
    return new FileInputStream( file );
  }

  public static IBasicFile getBasicFileFromFile( final File file ) {
    return new IBasicFile() {
      @Override
      public InputStream getContents() throws IOException {
        return new FileInputStream( file );
      }

      @Override
      public String getName() {
        return file.getName();
      }

      @Override
      public String getFullPath() {
        return file.getAbsolutePath();
      }

      @Override
      public String getPath() {
        return file.getPath();
      }

      @Override
      public String getExtension() {
        if ( !file.isDirectory() ) {
          String name = file.getName();
          int extBegin = name.lastIndexOf( "." ) + 1;
          return name.substring( extBegin );
        }
        return "";
      }

      @Override
      public boolean isDirectory() {
        return file.isDirectory();
      }
    };
  }
}
