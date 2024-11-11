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


package pt.webdetails.cdf.dd.render.cda;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class Output implements CdaElementRenderer {

  private Map<String, Object> definition;
  private JXPathContext context;

  public Output() {
  }

  public Output( JXPathContext context ) {
    this.context = context;
  }

  public void renderInto( Element dataAccess ) throws JSONException {
    JSONArray columns = new JSONArray( (String) definition.get( "value" ) );
    String mode;
    try {
      mode = context.getValue( "properties/.[name='outputMode']/value" ).toString();
    } catch ( Exception e ) {
      // If we fail to read the property, it defaults to Inclusive mode.
      mode = "include";
    }
    if ( columns.length() == 0 ) {
      return;
    }
    Document doc = dataAccess.getOwnerDocument();
    Element output = doc.createElement( "Output" );
    output.setAttribute( "mode", mode.toLowerCase() );
    dataAccess.appendChild( output );
    StringBuilder indexes = new StringBuilder();

    for ( int i = 0; i < columns.length(); i++ ) {
      String col = columns.getString( i );
      indexes.append( col );
      if ( i + 1 < columns.length() ) {
        indexes.append( "," );
      }
    }
    output.setAttribute( "indexes", indexes.toString() );
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }

  public void setContext( JXPathContext context ) {
    this.context = context;
  }
}
