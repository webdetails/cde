/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
