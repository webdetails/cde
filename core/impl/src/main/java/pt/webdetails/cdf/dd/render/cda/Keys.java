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
