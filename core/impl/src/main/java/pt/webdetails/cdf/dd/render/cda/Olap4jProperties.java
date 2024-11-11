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
