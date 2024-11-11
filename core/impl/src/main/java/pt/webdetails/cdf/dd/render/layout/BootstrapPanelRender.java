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

public class BootstrapPanelRender extends DivRender {

  public BootstrapPanelRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {
    super.processProperties();

    getPropertyBag().addClass( "panel" );
    getPropertyBag().addClass( getBootstrapPanelStyle() );
  }

  protected String getBootstrapPanelStyle() {
    return getPropertyString( "bootstrapPanelStyle" );
  }

}
