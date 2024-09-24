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

public class CarouselItemRender extends DivRender {

  public CarouselItemRender( JXPathContext context ) {
    super( context );
  }

  public String renderClose() {
    return "</div></li>";
  }

  @Override
  public void processProperties() {

    super.processProperties();
    getPropertyBag().addId( getId() );
    getPropertyBag().addClass( "cdfCarouselItemContent" );
  }

  @Override
  public String renderStart() {

    StringBuilder div = new StringBuilder();
    div.append( "<li class='cdfCarouselItem'><div class='cdfCarouselItemTitle'>" );
    div.append( getPropertyString( "title" ) );
    div.append( "</div><div  " );
    div.append( getPropertyBagString() );
    div.append( ">" );

    return div.toString();
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }
}
