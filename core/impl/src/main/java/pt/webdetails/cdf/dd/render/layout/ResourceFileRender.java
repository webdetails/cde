/*!
 * Copyright 2002 - 2021 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.render.layout;

import java.text.MessageFormat;
import org.apache.commons.jxpath.JXPathContext;
import org.owasp.encoder.Encode;
import pt.webdetails.cdf.dd.CdeConstants;

public class ResourceFileRender extends ResourceRender {

  public ResourceFileRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderStart() {
    final String resourceType = getPropertyString( CdeConstants.RESOURCE_TYPE );

    if ( resourceType.equals( CdeConstants.CSS ) ) {
      return MessageFormat.format(
              CdeConstants.LINK,
              Encode.forHtmlAttribute( getPropertyString( CdeConstants.RESOURCE_FILE ) ) );
    } else if ( resourceType.equals( CdeConstants.JAVASCRIPT ) ) {
      return MessageFormat.format(
              CdeConstants.SCRIPT_FILE,
              Encode.forHtmlAttribute( getPropertyString( CdeConstants.RESOURCE_FILE ) ) );
    }

    logger.error( "Resource not rendered" );

    return "";
  }
}
