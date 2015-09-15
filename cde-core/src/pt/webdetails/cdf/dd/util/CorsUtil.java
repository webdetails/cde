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

package pt.webdetails.cdf.dd.util;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Duplicated class from CDF, move to CPF
public class CorsUtil {

  private static CorsUtil instance;

  /**
   *
   * @return
   */
  public static CorsUtil getInstance() {
    if ( instance == null ) {
      instance = new CorsUtil();
    }
    return instance;
  }
  /**
   *
   * @param request
   * @param response
   */
  public void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
    final String allowCrossDomainResources = getAllowCrossDomainResources();
    if ( allowCrossDomainResources != null && allowCrossDomainResources.equals( "true" ) ) {
      String origin = request.getHeader( "ORIGIN" );
      if ( origin != null ) {
        response.setHeader( "Access-Control-Allow-Origin", origin );
        response.setHeader( "Access-Control-Allow-Credentials", "true" );
      }
    }
  }

  /**
   *
   * @return
   */
  protected String getAllowCrossDomainResources() {
    return CdeEngine.getInstance().getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
      CdeConstants.PLUGIN_SETTINGS_ALLOW_CROSS_DOMAIN_RESOURCES );
  }
}
