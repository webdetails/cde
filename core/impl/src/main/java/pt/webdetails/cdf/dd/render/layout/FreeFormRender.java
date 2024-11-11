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
