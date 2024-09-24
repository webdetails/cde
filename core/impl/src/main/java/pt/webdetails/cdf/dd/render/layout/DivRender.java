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
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.util.XPathUtils;

public class DivRender extends Render {


  public DivRender( JXPathContext context ) {
    super( context );
  }

  public String renderClose() {
    return "</div>";
  }


  @Override
  public void processProperties() {

    getPropertyBag().addId( getId() );
    getPropertyBag().addClass( getPropertyString( "roundCorners" ) );
    getPropertyBag().addClass( getPropertyString( "cssClass" ) );
    getPropertyBag().addStyle( "background-color", getPropertyString( "backgroundColor" ) );
    String height = getPropertyString( "height" );
    if ( StringUtils.isNotEmpty( height ) ) {
      getPropertyBag().addStyle( "height", height + "px" );
    }
    getPropertyBag().addStyle( "text-align", getPropertyString( "textAlign" ) );

  }

  @Override
  public String renderStart() {

    String div = "<div ";
    div += getPropertyBagString() + ">";
    return div;
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }

}
