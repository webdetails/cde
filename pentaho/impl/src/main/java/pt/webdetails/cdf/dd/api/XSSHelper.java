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

import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.http.api.resources.utils.EscapeUtils;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.util.CorsUtil;

public class XSSHelper {

  private static XSSHelper instance = new XSSHelper();

  public static XSSHelper getInstance() {
    return instance;
  }


  static void setInstance( final XSSHelper newInstance ) {
    instance = newInstance;
  }

  public String escape( final String userInput ) {
    if ( StringUtil.isEmpty( userInput ) ) {
      return userInput;
    }
    if ( "false".equals( getPluginSetting() ) ) {
      return userInput;
    }
    return EscapeUtils.escapeJsonOrRaw( userInput );
  }

  protected String getPluginSetting() {
    return CdeEngine.getInstance().getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
      CdeConstants.PARAMETER_XSS_ESCAPING );
  }

}
