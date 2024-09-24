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

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class SpaceRender extends Render {

  public SpaceRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    getPropertyBag().addStyle( "background-color", getPropertyString( "backgroundColor" ) );
    getPropertyBag().addClass( getPropertyString( "cssClass" ) );
    getPropertyBag().addClass( "space" );
    getPropertyBag().addStyle( "height", getPropertyString( "height" ) + "px" );

  }


  public String renderStart() {

    String div = "<hr ";
    div += getPropertyBagString() + ">";
    return div;
  }

  public String renderClose() {
    return "</hr>";
  }
}
