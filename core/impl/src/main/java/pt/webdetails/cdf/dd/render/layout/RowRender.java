/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;

public class RowRender extends DivRender {

  public RowRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    super.processProperties();

    getPropertyBag().addClass( "row" );
    getPropertyBag().addClass( "clearfix" );

  }

  @Override
  public String renderStart() {

    String div = "<div ";
    div += getPropertyBagString() + ">";
    return div;
  }

  @Override
  public String renderClose() {
    return "</div>";
  }
}
