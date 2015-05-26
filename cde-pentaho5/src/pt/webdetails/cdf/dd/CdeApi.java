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

package pt.webdetails.cdf.dd;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;

import pt.webdetails.cdf.dd.embed.EmbeddedHeadersGenerator;
import pt.webdetails.cpf.utils.PluginIOUtils;

public class CdeApi {

  private static final Log logger = LogFactory.getLog( CdeApi.class );

  public void getCdeEmbeddedContext( HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse ) throws Exception {
    try {
      EmbeddedHeadersGenerator embeddedHeadersGenerator =
          new EmbeddedHeadersGenerator( buildFullServerUrl( servletRequest ) );
      String locale = servletRequest.getParameter( "locale" );
      if ( !StringUtils.isEmpty( locale ) ) {
        embeddedHeadersGenerator.setLocale( new Locale( locale ) );
      }
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), embeddedHeadersGenerator.generate() );
    } catch ( IOException ex ) {
      logger.error( "getCdeEmbeddedContext: " + ex.getMessage(), ex );
      throw ex;
    }
  }

  protected String buildFullServerUrl( HttpServletRequest servletRequest ) {
    if ( !StringUtils.isEmpty( servletRequest.getProtocol() ) ) {
      return servletRequest.getProtocol().split( "/" )[0].toLowerCase() + "://" + servletRequest.getServerName() + ":"
        + servletRequest.getServerPort() + PentahoRequestContextHolder.getRequestContext().getContextPath();
    }
    return "http://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort()
      + PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

}
