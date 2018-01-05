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
