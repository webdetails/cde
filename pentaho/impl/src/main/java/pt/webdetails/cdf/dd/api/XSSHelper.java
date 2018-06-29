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

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.http.api.resources.utils.EscapeUtils;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeConstants.Writer;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.util.CorsUtil;
import pt.webdetails.cpf.resources.IResourceLoader;

public class XSSHelper {

  private static XSSHelper instance = new XSSHelper();

  private static DefaultPrettyPrinter prettyPrinter;

  public static XSSHelper getInstance() {
    return instance;
  }

  public String escape( final String userInput ) {
    final boolean isInputEmpty = StringUtil.isEmpty( userInput );
    final boolean isXssEscapeDisabled = "false".equals( getXssEscapingPluginSetting() );

    if ( isInputEmpty || isXssEscapeDisabled ) {
      return userInput;
    }

    return EscapeUtils.escapeJsonOrRaw( userInput, getPrettyPrinter() );
  }

  private DefaultPrettyPrinter getPrettyPrinter() {
    if ( prettyPrinter != null ) {
      return prettyPrinter;
    }

    final DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
    final DefaultIndenter indent = new DefaultIndenter( Writer.INDENT1, Writer.NEWLINE );

    prettyPrinter = printer
      .withObjectIndenter( indent )
      .withArrayIndenter( indent );

    return prettyPrinter;
  }

  static void setInstance( final XSSHelper newInstance ) {
    instance = newInstance;
  }

  String getXssEscapingPluginSetting() {
    IResourceLoader cdeResourceLoader = CdeEngine.getInstance().getEnvironment().getResourceLoader();

    return cdeResourceLoader.getPluginSetting( CorsUtil.class, CdeConstants.PARAMETER_XSS_ESCAPING );
  }
}
