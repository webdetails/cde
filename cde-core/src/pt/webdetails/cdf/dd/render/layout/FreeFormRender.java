/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cdf.dd.render.layout;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

import java.util.HashMap;
import java.util.Map;

public class FreeFormRender extends Render {

  private String elementTag;
  private String moreProperties;

  public FreeFormRender( JXPathContext context ) {
    super( context );
  }

  @Override
  public void processProperties() {

    this.elementTag = getPropertyString( "elementTag" );
    this.moreProperties = getPropertyString( "otherAttributes" );
    getPropertyBag().addId( getId() );
    getPropertyBag().addClass( getPropertyString( "cssClass" ) );

  }

  @Override
  public String renderStart() {
    Map<String, String> attrs = getMoreProperties();
    String content = "<" + elementTag + " " + getPropertyBagString();

    for ( Map.Entry<String, String> entry : attrs.entrySet() ) {
      content += " " + entry.getKey() + "='" + entry.getValue() + "'";
    }
    content += ">";

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

  protected Map<String, String> getMoreProperties() {
    Map<String, String> values = new HashMap<String, String>();
    String[] attrs;
    String[] insertVal;

    if ( !moreProperties.equals( "[]" ) ) {
      moreProperties = moreProperties.replaceAll( "\"\"", "\" \"" ).replaceAll( "(\\[\\[\")|(\"\\]\\])" , "" )
        .replaceAll( "\",\"", "," ).replaceAll( "\"\\],\\[\"", ";" );


      attrs = moreProperties.split( ";" );
      for ( int i = 0; i < attrs.length; i++ ) {
        insertVal = attrs[ i ].split( "," );
        if ( !insertVal[ 0 ].equals( "id" ) ) {
          values.put( insertVal[ 0 ], insertVal[ 1 ] );
        }
      }
    }

    return values;
  }
}
