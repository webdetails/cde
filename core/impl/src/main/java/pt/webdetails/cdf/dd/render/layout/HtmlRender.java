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

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class HtmlRender extends Render {

  private boolean hasProperties = false;

  public HtmlRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    getPropertyBag().addStyle( "color", getPropertyString( "color" ) );
    getPropertyBag().addClass( getPropertyString( "cssClass" ) );
    String fontSize = getPropertyString( "fontSize" );
    if ( fontSize.length() > 0 ) {
      getPropertyBag().addStyle( "font-size", fontSize + "px" );
    }

    if ( getPropertyBagString().length() > 0 ) {
      hasProperties = true;
    }

    getPropertyBag().addClass( getPropertyString( "cssClass" ) );

  }

  public String renderStart() {

    String out = "";
    if ( hasProperties ) {
      out += "<span " + getPropertyBagString() + ">";
    }

    out += getPropertyString( "html" );
    return out;

  }

  public String renderClose() {

    if ( hasProperties ) {
      return "</span>";
    } else {
      return "";
    }
  }
}
