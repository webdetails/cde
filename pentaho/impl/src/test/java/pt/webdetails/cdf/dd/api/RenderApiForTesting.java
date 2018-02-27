/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cdf.dd.cache.api.ICache;

public class RenderApiForTesting extends RenderApi {
  private DashboardManager dashboardManager;
  static String cdfRequireContextConfiguration = "";
  static String cdfRequireContext = "";
  static String cdfContext = "";

  public RenderApiForTesting( ICdeEnvironment cdeEnvironment ) {
    this.privateEnviroment = cdeEnvironment;
  }

  @Override
  public DashboardManager getDashboardManager() {
    dashboardManager = DashboardManager.getInstance();
    dashboardManager.init();
    dashboardManager.setCache( Mockito.mock( ICache.class ) );
    return dashboardManager;
  }

  @Override
  protected void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) { }

  @Override
  protected boolean hasSystemOrUserReadAccess( String path ) {
    return true;
  }

  @Override
  protected String getCdfRequireConfig( String filePath, IParameterProvider requestParams ) {
    return cdfRequireContextConfiguration;
  }

  @Override
  protected String getCdfRequireContext( String filePath, IParameterProvider requestParams ) {
    return cdfRequireContext;
  }

  @Override
  protected String getCdfContext( String filePath, String action, String view, IParameterProvider requestParams ) {
    return cdfContext;
  }
}
