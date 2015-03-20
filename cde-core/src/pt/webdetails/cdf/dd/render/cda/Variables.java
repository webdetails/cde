/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cdf.dd.render.cda;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Variables implements CdaElementRenderer {

  private JSONObject definition;
  private JXPathContext context;

  public Variables() {
  }

  public Variables( JXPathContext context ) {
    this.context = context;
  }

  public void renderInto( Element dataAccess ) {
    JSONArray vars = JSONArray.fromObject( definition.getString( "value" ) );
    if ( vars.size() == 0 ) {
      return;
    }
    Document doc = dataAccess.getOwnerDocument();
    for ( Object o : vars.toArray() ) {
      JSONArray jsa = (JSONArray) o;
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

  public void setDefinition( JSONObject definition ) {
    this.definition = definition;
  }

  public void setContext( JXPathContext context ) {
    this.context = context;
  }
}
