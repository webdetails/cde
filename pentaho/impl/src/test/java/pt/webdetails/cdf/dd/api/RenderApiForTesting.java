/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
  protected String getCdfContext( String filePath, String action, String view, IParameterProvider requestParams ) {
    return cdfContext;
  }
}
