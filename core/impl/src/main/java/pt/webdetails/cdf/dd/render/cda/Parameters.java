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

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class Parameters implements CdaElementRenderer {

  private Map<String, Object> definition;
  private final String NAME_ATTR = "name";
  private final String DEFAULT_ATTR = "default";
  private final String TYPE_ATTR = "type";
  private final String ACCESS_ATTR = "access";
  private final String PATTERN_ATTR = "pattern";
  private final String ELEMENT_NAME = "Parameter";

  public void renderInto( Element dataAccess ) throws JSONException {
    Document doc = dataAccess.getOwnerDocument();
    Element parameters = doc.createElement( "Parameters" );
    dataAccess.appendChild( parameters );
    JSONArray params = new JSONArray( (String) definition.get( "value" ) );

    for ( int i = 0; i < params.length(); i++ ) {
      JSONArray param = params.getJSONArray( i );
      Element parameter = doc.createElement( ELEMENT_NAME );
      parameter.setAttribute( NAME_ATTR, (String) param.get( 0 ) );
      parameter.setAttribute( DEFAULT_ATTR, (String) param.get( 1 ) );
      if ( param.length() > 2 ) {
        parameter.setAttribute( TYPE_ATTR, (String) param.get( 2 ) );
        if ( param.length() > 3 ) {
          String access = (String) param.get( 3 );
          if ( !StringUtils.isEmpty( access ) ) {
            parameter.setAttribute( ACCESS_ATTR, access );
          }
          if ( param.length() > 4 ) {
            String pattern = (String) param.get(4);
            if (!StringUtils.isEmpty(pattern)) {
              parameter.setAttribute(PATTERN_ATTR, pattern);
            }
          }
        }
      } else {
        parameter.setAttribute(TYPE_ATTR, "String");
      }
      parameters.appendChild( parameter );
    }
  }

  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }
}
