/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

import java.text.MessageFormat;

public class ResourceCodeRender extends ResourceRender {
  private static final String STYLE = "<style>\n<!--\n{0}\n-->\n</style>";
  private static final String SCRIPT_SOURCE =
    "<script language=\"javascript\" type=\"text/javascript\">\n{0}\n</script>";

  private static final String RESOURCE_CODE = "resourceCode";

  public ResourceCodeRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderStart() {
    String resourceType = getPropertyString( RESOURCE_TYPE );

    if ( resourceType.equals( CSS ) ) {
      return MessageFormat.format( STYLE, getPropertyString( RESOURCE_CODE ) );
    } else if ( resourceType.equals( JAVASCRIPT ) ) {
      return MessageFormat.format( SCRIPT_SOURCE, getPropertyString( RESOURCE_CODE ) );
    }

    logger.error( "Resource not rendered" );

    return "";
  }
}
