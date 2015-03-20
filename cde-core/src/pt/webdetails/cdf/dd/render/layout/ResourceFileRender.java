/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

public class ResourceFileRender extends ResourceRender {
  protected static final String LINK = "<link rel=\"stylesheet\" type=\"text/css\" href=\"{0}\" />";
  protected static final String SCRIPT_FILE =
    "<script language=\"javascript\" type=\"text/javascript\" src=\"{0}\"></script>";

  protected static final String RESOURCE_FILE = "resourceFile";

  public ResourceFileRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderStart() {
    String resourceType = getPropertyString( RESOURCE_TYPE );

    if ( resourceType.equals( CSS ) ) {
      return MessageFormat.format( LINK, getPropertyString( RESOURCE_FILE ) );
    } else if ( resourceType.equals( JAVASCRIPT ) ) {
      return MessageFormat.format( SCRIPT_FILE, getPropertyString( RESOURCE_FILE ) );
    }

    logger.error( "Resource not rendered" );

    return "";
  }
}
