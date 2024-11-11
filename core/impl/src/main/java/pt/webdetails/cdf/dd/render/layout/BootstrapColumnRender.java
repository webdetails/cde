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

public class BootstrapColumnRender extends DivRender {

  private String cssClass;

  public BootstrapColumnRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    super.processProperties();
    cssClass = getBootstrapClassString();

  }

  @Override
  public String renderStart() {

    String div = "<div";

    div += cssClass + ">";

    div += "<div " + getPropertyBagString() + ">";

    return div;
  }

  @Override
  public String renderClose() {
    return "</div></div>";
  }

  protected boolean lastColumn() {
    String parentId = (String) getNode().getValue( "parent" );
    return ( (Boolean) getNode().getValue(
      "not(following-sibling::*[parent='" + parentId + "'][type='LayoutBootstrapColumn'])" ) ).booleanValue();
  }

  protected String getBootstrapClassString() {
    String css = "";

    if ( !getPropertyString( "bootstrapExtraSmall" ).equals( "" ) ) {
      css += "col-xs-" + getPropertyString( "bootstrapExtraSmall" );
    } else {
      css += "col-xs-12";
    }
    if ( !getPropertyString( "bootstrapSmall" ).equals( "" ) ) {
      css += " col-sm-" + getPropertyString( "bootstrapSmall" );
    }
    if ( !getPropertyString( "bootstrapMedium" ).equals( "" ) ) {
      css += " col-md-" + getPropertyString( "bootstrapMedium" );
    }
    if ( !getPropertyString( "bootstrapLarge" ).equals( "" ) ) {
      css += " col-lg-" + getPropertyString( "bootstrapLarge" );
    }
    if ( !getPropertyString( "bootstrapCssClass" ).equals( "" ) ) {
      css += " " + getPropertyString( "bootstrapCssClass" );
    }
    if ( lastColumn() ) {
      css += " last";
    }

    return ( css.equals( "" ) ? css : " class='" + css + "'" );
  }
}
