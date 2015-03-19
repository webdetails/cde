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

public class ResourceRender extends Render {

  public ResourceRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {
  }

  public String renderStart() {

    String out = "";

    String resourceType = getPropertyString( "resourceType" );

    if ( hasProperty( "resourceFile" ) ) {
      if ( resourceType.equals( "Css" ) ) {
        out += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + getPropertyString( "resourceFile" ) + "\" />\n";
      } else if ( resourceType.equals( "Javascript" ) ) {
        out += "<script language=\"javascript\" type=\"text/javascript\" src=\"" + getPropertyString( "resourceFile" )
          + "\" ></script>\n";
      }
    }
    if ( hasProperty( "resourceCode" ) ) {
      if ( resourceType.equals( "Css" ) ) {
        out += "<style>\n<!--\n" + getPropertyString( "resourceCode" ) + "\n-->\n</style>\n";
      } else if ( resourceType.equals( "Javascript" ) ) {
        out += "<script language=\"javascript\" type=\"text/javascript\" >\n" + getPropertyString( "resourceCode" )
          + "\n</script>\n";
      }
    }

    return out;

  }

  public String renderClose() {

    return "";
  }
}
