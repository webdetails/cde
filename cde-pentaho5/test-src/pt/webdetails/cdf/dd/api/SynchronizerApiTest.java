/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SynchronizerApiTest {


  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void saveSettingsWithoutDoingInitialDashboardSaveTest() throws Exception {

    String file = StringUtils.EMPTY; // no file sent, therefore no initial dashboard save has been done
    String path = StringUtils.EMPTY;
    String title = "MOCK_TITLE";
    String author = "MOCK_AUTHOR";
    String description = "MOCK_DESCRIPTION";
    String style = "MOCK_STYLE";
    String widgetName = "MOCK_WIDGET_NAME";
    boolean widget = false;
    boolean require = false;
    String rendererType = "MOCK_RENDERED_TYPE";
    List<String> widgetParams = new ArrayList<String>();
    String cdfStructure = "MOCK_CDF_STRUCTURE";
    String operation = "savesettings";


    HttpServletRequest mockRequest = Mockito.mock( HttpServletRequest.class );
    Mockito.when( mockRequest.getParameterMap() ).thenReturn( new HashMap<String, Object>() );

    HttpServletResponse mockResponse = Mockito.mock( HttpServletResponse.class );

    String result = new SynchronizerApiForTesting()
      .syncronize( file, path, title, author, description, style, widgetName, widget, rendererType, widgetParams,
        cdfStructure, operation, require , mockRequest, mockResponse );

    JSONObject jsonObj = JSONObject.fromObject( result );

    Assert.assertTrue( jsonObj.getString( "status" ) != null );
    Assert.assertTrue( jsonObj.getString( "result" ) != null );
    Assert.assertTrue( "false".equals( jsonObj.getString( "status" ) ) );
    Assert.assertTrue( "CdfTemplates.ERROR_003_SAVE_DASHBOARD_FIRST".equals( jsonObj.getString( "result" ) ) );
  }

  @After
  public void tearDown() {

  }

}
