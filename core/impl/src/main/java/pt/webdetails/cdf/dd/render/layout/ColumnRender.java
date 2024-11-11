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

@SuppressWarnings( "unchecked" )
public class ColumnRender extends DivRender {

  private String renderType;

  public ColumnRender( JXPathContext context ) {
    super( context );
    renderType = XPathUtils.getStringValue( context, "//rendererType" );
  }

  @Override
  public void processProperties() {

    super.processProperties();

    addColSpan( getPropertyString( "columnSpan" ) );
    getPropertyBag().addColClass( "append-", getPropertyString( "columnAppend" ) );
    getPropertyBag().addColClass( "prepend-", getPropertyString( "columnPrepend" ) );
    getPropertyBag().addColClass( ".prepend-top", getPropertyBoolean( "columnPrependTop" ) );
    getPropertyBag().addColClass( ".append-bottom", getPropertyBoolean( "columnAppendBottom" ) );
    getPropertyBag().addColClass( "border", getPropertyBoolean( "columnBorder" ) );
    getPropertyBag().addColClass( "colborder", getPropertyBoolean( "columnBigBorder" ) );

  }

  @Override
  public String renderStart() {
    String div = "<div ";

    if ( lastColumn() ) {
      getPropertyBag().addClass( "last" );
    }
    div += getPropertyBagString() + ">";

    return div;
  }

  protected boolean lastColumn() {

    String parentId = (String) getNode().getValue( "parent" );
    return ( (Boolean) getNode()
      .getValue( "not(following-sibling::*[parent='" + parentId + "'][type='LayoutColumn'])" ) ).booleanValue();
  }

  protected String getRenderType() {
    return this.renderType;
  }

  protected void addColSpan( String value ) {
    final String spanPrefix = getRenderType().equals( "bootstrap" ) ? "col-md-" : "span-";
    getPropertyBag().addColClass( spanPrefix, value );
  }

}
