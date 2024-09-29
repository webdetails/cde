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
