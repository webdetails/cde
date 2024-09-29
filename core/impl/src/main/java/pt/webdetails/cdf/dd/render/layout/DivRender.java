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
