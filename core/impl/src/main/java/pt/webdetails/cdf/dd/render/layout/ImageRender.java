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


public class ImageRender extends Render {

  public ImageRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    getPropertyBag().addClass( getPropertyString( "cssClass" ) );

  }

  public String renderStart() {

    String image = "<image src='" + getPropertyString( "url" ) + "'";
    image += getPropertyBagString() + ">";
    return image;
  }

  public String renderClose() {
    return "</image>";
  }

}
