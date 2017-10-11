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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Olap4jProperties implements CdaElementRenderer {

  private Map<String, Object> definition;
  private String paramName = "";

  private HashMap<String, String> names;

  public Olap4jProperties( String paramName ) {
    this.paramName = paramName;
    names = new HashMap<String, String>();
    names.put( "olap4juser", "JdbcUser" );
    names.put( "olap4jpass", "JdbcPassword" );
    names.put( "olap4jurl", "Jdbc" );
    names.put( "olap4jcatalog", "Catalog" );
    names.put( "olap4jdriver", "JdbcDrivers" );
  }

  public void renderInto( Element dataAccess ) {
    Document doc = dataAccess.getOwnerDocument();

    Element prop = doc.createElement( "Property" );
    prop.setAttribute( "name", names.get( paramName ) );
    prop.appendChild( doc.createTextNode( (String) definition.get( "value" ) ) );
    dataAccess.appendChild( prop );
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;

  }

}
