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
import pt.webdetails.cdf.dd.util.XPathUtils;

public class CarouselRender extends DivRender {

  public CarouselRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderClose() {
    return "</ul></div>";
  }

  @Override
  public void processProperties() {
    super.processProperties();
    getPropertyBag().addId( getId() );
    getPropertyBag().addClass( "cdfCarousel" );
    if ( getPropertyBoolean( "showTitle" ) ) {
      getPropertyBag().addClass( "showTitle" );
    }
    getPropertyBag().addClass( "cdfCarousel" );

  }

  @Override
  public String renderStart() {

    String div = "<div class='cdfCarouselHolder'><ul ";
    div += getPropertyBagString() + ">";
    return div;
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }
}
