/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalculatedColumns implements CdaElementRenderer {

  private JSONObject definition;

  public void renderInto( Element cols ) {
    JSONArray columns = JSONArray.fromObject( definition.getString( "value" ) );
    if ( columns.size() == 0 ) {
      return;
    }
    Document doc = cols.getOwnerDocument();
    @SuppressWarnings( "unchecked" )
    Iterator<JSONArray> paramIterator = columns.iterator();
    while ( paramIterator.hasNext() ) {
      JSONArray content = paramIterator.next();
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

  public void setDefinition( JSONObject definition ) {
    this.definition = definition;
  }
}
