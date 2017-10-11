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

package pt.webdetails.cdf.dd.render.cda;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class Columns implements CdaElementRenderer {

  private Map<String, Object> definition;

  public void renderInto( Element cols ) throws JSONException {
    JSONArray columns = new JSONArray( (String) definition.get( "value" ) );
    if ( columns.length() == 0 ) {
      return;
    }
    Document doc = cols.getOwnerDocument();
    for ( int i = 0; i < columns.length(); i++ ) {
      JSONArray content = columns.getJSONArray( i );
      Element col = doc.createElement( "Column" );
      Element name = doc.createElement( "Name" );
      col.setAttribute( "idx", (String) content.get( 0 ) );
      name.appendChild( doc.createTextNode( (String) content.get( 1 ) ) );
      col.appendChild( name );
      cols.appendChild( col );
    }
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }
}
