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
import pt.webdetails.cdf.dd.util.XPathUtils;

public class FilterBlockRender extends DivRender {

  public FilterBlockRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public String renderClose() {
    return "</div>";
  }

  @Override
  public void processProperties() {
    super.processProperties();
    getPropertyBag().addId( getId() );
  }

  @Override
  public String renderStart() {
    return "<div id='filters'>";
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }
}
