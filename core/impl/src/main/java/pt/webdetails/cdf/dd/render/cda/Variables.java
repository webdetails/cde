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


import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class Variables implements CdaElementRenderer {

  private Map<String, Object> definition;
  private JXPathContext context;

  public Variables() {
  }

  public Variables( JXPathContext context ) {
    this.context = context;
  }

  public void renderInto( Element dataAccess ) throws JSONException {
    JSONArray vars = new JSONArray( (String) definition.get( "value" ) );
    if ( vars.length() == 0 ) {
      return;
    }
    Document doc = dataAccess.getOwnerDocument();
    for ( int i = 0; i < vars.length(); i++ ) {
      JSONArray jsa = vars.getJSONArray( i );
      Element variable = doc.createElement( "variables" );
      if ( !jsa.getString( 0 ).equals( "" ) ) {
        variable.setAttribute( "datarow-name", jsa.getString( 0 ) );
      }
      if ( !jsa.getString( 1 ).equals( "" ) ) {
        variable.setAttribute( "variable-name", jsa.getString( 1 ) );
      }
      dataAccess.appendChild( variable );
    }
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }

  public void setContext( JXPathContext context ) {
    this.context = context;
  }
}
