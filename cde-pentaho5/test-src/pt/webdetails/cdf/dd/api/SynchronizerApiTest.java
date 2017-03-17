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
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import pt.webdetails.cpf.messaging.MockHttpServletRequest;
import pt.webdetails.cpf.messaging.MockHttpServletResponse;
import pt.webdetails.cpf.utils.CharsetHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynchronizerApiTest {

  private SyncronizerApi synchronizerApi;
  private MockHttpServletRequest servletRequest;
  private MockHttpServletResponse servletResponse;

  private String file;
  private String path;
  private static final String title = "MOCK_TITLE";
  private static final String author = "MOCK_AUTHOR";
  private static final String description = "MOCK_DESCRIPTION";
  private static final String style = "MOCK_STYLE";
  private static final String widgetName = "MOCK_WIDGET_NAME";
  private static final boolean widget = false;
  private static final boolean require = false;
  private static final String rendererType = "MOCK_RENDERED_TYPE";
  private static final List<String> widgetParams = new ArrayList<String>();
  private static final String cdfStructure = "MOCK_CDF_STRUCTURE";
  private String operation = "savesettings";
  private static XSSHelper originalHelper;
  private static XSSHelper mockHelper;

  @BeforeClass
  public static void setUp() {
    originalHelper = XSSHelper.getInstance();
  }

  @AfterClass
  public static void afterAll() {
    XSSHelper.setInstance( originalHelper );
  }

  @Before
  public void beforeEach() throws Exception {
    synchronizerApi = Mockito.spy( new SynchronizerApiForTesting() );
    servletRequest = new MockHttpServletRequest( "/pentaho-cdf/api/views", (Map) new HashMap<String, String[]>() );
    servletResponse = new MockHttpServletResponse( new ObjectOutputStream( new ByteArrayOutputStream() ) );
    servletResponse.setContentType( null );
    servletResponse.setCharacterEncoding( null );
    path = StringUtils.EMPTY;
    file = StringUtils.EMPTY;
    mockHelper = Mockito.mock( XSSHelper.class );
    Mockito.when( mockHelper.escape( Matchers.anyString() ) ).thenAnswer( new Answer<Object>() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[ 0 ];
      }
    } );
    XSSHelper.setInstance( mockHelper );
  }

  @After
  public void tearDown() {
    synchronizerApi = null;
    servletRequest = null;
    servletResponse = null;
    file = null;
    path = null;
    Mockito.reset( mockHelper );
  }

  @Test
  public void saveSettingsWithoutDoingInitialDashboardSaveTest() throws Exception {
    path = StringUtils.EMPTY;
    file = StringUtils.EMPTY; // no file sent, therefore no initial dashboard save has been done

    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    Mockito.when( mockRequest.getParameterMap() ).thenReturn( new HashMap<String, Object>() );

    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );

    String result = new SynchronizerApiForTesting()
      .syncronize( file, path, title, author, description, style, widgetName, widget, rendererType, widgetParams,
        cdfStructure, operation, require, mockRequest, mockResponse );

    JSONObject jsonObj = new JSONObject( result );

    Assert.assertTrue( jsonObj.getString( "status" ) != null );
    Assert.assertTrue( jsonObj.getString( "result" ) != null );
    Assert.assertTrue( "false".equals( jsonObj.getString( "status" ) ) );
    Assert.assertTrue( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST".equals( jsonObj.getString( "result" ) ) );
    Mockito.verify( mockHelper, Mockito.atLeastOnce() ).escape( Matchers.anyString() );
  }

  @Test
  public void listViewsTest() throws Exception {
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    synchronizerApi.syncronize( file, path, title, author, description, style, widgetName, widget, rendererType,
      widgetParams, cdfStructure, operation, require, servletRequest, servletResponse );

    Assert.assertTrue( servletResponse.getContentType().equals( MediaType.APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
    Mockito.verify( mockHelper, Mockito.atLeastOnce() ).escape( Matchers.anyString() );
  }

  @Test
  public void syncTemplatesTest() throws Exception {
    operation = StringUtils.EMPTY;
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    synchronizerApi.syncTemplates( operation, file, cdfStructure, rendererType, servletResponse );

    Assert.assertTrue( servletResponse.getContentType().equals( MediaType.APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
    Mockito.verify( mockHelper, Mockito.atLeastOnce() ).escape( Matchers.anyString() );
  }

  @Test
  public void syncStylesTest() throws Exception {
    operation = StringUtils.EMPTY;
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    synchronizerApi.syncStyles( servletResponse );

    Assert.assertTrue( servletResponse.getContentType().equals( MediaType.APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
  }

  @Test
  public void saveDashboardTest() throws Exception {
    operation = StringUtils.EMPTY;
    Assert.assertEquals( servletResponse.getContentType(), null );
    Assert.assertEquals( servletResponse.getCharacterEncoding(), null );

    synchronizerApi.saveDashboard( file, title, description, cdfStructure, operation, servletResponse );

    Assert.assertTrue( servletResponse.getContentType().equals( MediaType.APPLICATION_JSON ) );
    Assert.assertTrue( servletResponse.getCharacterEncoding().equals( CharsetHelper.getEncoding() ) );
    Mockito.verify( mockHelper, Mockito.atLeastOnce() ).escape( Matchers.anyString() );
  }
}
