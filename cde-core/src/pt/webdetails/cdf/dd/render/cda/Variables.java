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
