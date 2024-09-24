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
