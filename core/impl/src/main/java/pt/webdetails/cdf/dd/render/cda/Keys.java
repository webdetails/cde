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

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Element;

import java.util.Map;

public class Keys implements CdaElementRenderer {

  private Map<String, Object> definition;

  public void renderInto( Element dataAccess ) throws JSONException {
    JSONArray columns = new JSONArray( (String) definition.get( "value" ) );
    StringBuilder indexes = new StringBuilder();
    for ( int i = 0; i < columns.length(); i++ ) {
      String col = columns.getString( i );
      indexes.append( col );
      if ( i + 1 < columns.length() ) {
        indexes.append( "," );
      }
    }
    dataAccess.setAttribute( "keys", indexes.toString() );
  }

  public void setDefinition(  Map<String, Object> definition ) {
    this.definition = definition;
  }
}
