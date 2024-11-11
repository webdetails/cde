/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.render.cda;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalculatedColumns implements CdaElementRenderer {

  private Map<String, Object> definition;

  public void renderInto( Element cols ) throws JSONException {
    JSONArray columns = new JSONArray( (String) definition.get( "value" ) );
    if ( columns.length() == 0 ) {
      return;
    }
    Document doc = cols.getOwnerDocument();
    for ( int i = 0; i < columns.length(); i++ ) {
      JSONArray content = columns.getJSONArray( i );
      Element col = doc.createElement( "CalculatedColumn" );
      Element name = doc.createElement( "Name" );
      Element formula = doc.createElement( "Formula" );
      name.appendChild( doc.createTextNode( (String) content.get( 0 ) ) );
      formula.appendChild( doc.createTextNode( (String) content.get( 1 ) ) );
      col.appendChild( name );
      col.appendChild( formula );
      cols.appendChild( col );
    }
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }
}
