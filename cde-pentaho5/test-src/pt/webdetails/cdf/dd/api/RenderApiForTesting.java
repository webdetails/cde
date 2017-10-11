/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.DashboardManagerForTests;
import pt.webdetails.cdf.dd.ICdeEnvironment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RenderApiForTesting extends RenderApi {
  private DashboardManager dashboardManagerForTests;
  static String cdfRequireContextConfiguration = "";
  static String cdfRequireContext = "";
  static String cdfContext = "";

  public RenderApiForTesting( ICdeEnvironment cdeEnvironment ) {
    this.privateEnviroment = cdeEnvironment;
  }

  @Override
  protected DashboardManager getDashboardManager() {
    if ( dashboardManagerForTests == null ) {
      dashboardManagerForTests = new DashboardManagerForTests( this.privateEnviroment );
    }
    return dashboardManagerForTests;
  }

  @Override
  protected void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
  }

  @Override
  protected boolean hasSystemOrUserReadAccess( String path ) {
    return true;
  }

  @Override
  protected String getCdfRequireConfig( String filePath, IParameterProvider requestParams ) throws Exception {
    return cdfRequireContextConfiguration;
  }

  @Override
  protected String getCdfRequireContext( String filePath, IParameterProvider requestParams ) throws Exception {
    return cdfRequireContext;
  }

  @Override
  protected String getCdfContext( String filePath, String action, String view, IParameterProvider requestParams )
    throws Exception {
    return cdfContext;
  }
}
