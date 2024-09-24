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
import org.json.JSONArray;
import org.json.JSONException;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class FreeFormRender extends Render {

  private String elementTag;
  private String moreProperties;

  public FreeFormRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    setElementTag( getPropertyString( "elementTag" ) );
    setMoreProperties( getPropertyString( "otherAttributes" ) );

    getPropertyBag().addId( getId() );
    getPropertyBag().addClass( getPropertyString( "cssClass" ) );

  }

  @Override
  public String renderStart() throws JSONException {
    String content = "<" + elementTag + " " + getPropertyBagString();
    content += buildMoreProperties() + ">";
    return content;
  }

  @Override
  public String renderClose() {
    return "</" + elementTag + ">";
  }

  protected String getId() {
    String id = getPropertyString( "name" );
    return id.length() > 0 ? id : XPathUtils.getStringValue( getNode(), "id" );
  }

  protected String buildMoreProperties() throws JSONException {
    String properties = "";
    String arg = "";
    String value = "";

    JSONArray attrs = new JSONArray( getMoreProperties() );
    JSONArray insertVal;

    for ( int i = 0; i < attrs.length(); i++ ) {
      insertVal = attrs.getJSONArray( i );
      arg = insertVal.getString( 0 );
      value = insertVal.getString( 1 );

      if ( ( arg.equals( "id" ) && getPropertyString( "name" ).equals( "" ) ) || !arg.equals( "id" ) ) {
        properties += " " + arg + "=" + ( value.indexOf( "\'" ) != -1
          ? "\"" + value + "\""
          : "'" + value + "'" );
      }
    }

    return properties;
  }

  protected String getMoreProperties() {
    return moreProperties;
  }

  protected void setMoreProperties( String properties ) {
    this.moreProperties = properties;
  }

  protected void setElementTag( String tag ) {
    this.elementTag = tag;
  }
}
