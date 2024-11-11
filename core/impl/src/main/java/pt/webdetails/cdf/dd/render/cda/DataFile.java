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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class DataFile implements CdaElementRenderer {

  private Map<String, Object> definition;

  public void renderInto( Element connection ) {
    Document doc = connection.getOwnerDocument();
    Element df = doc.createElement( "DataFile" );
    df.appendChild( doc.createTextNode( (String) definition.get( "value" ) ) );
    connection.appendChild( df );
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;

  }
}
