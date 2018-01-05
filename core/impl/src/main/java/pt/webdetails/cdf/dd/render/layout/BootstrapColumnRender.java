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
