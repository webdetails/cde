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

public class Cache implements CdaElementRenderer {

  private Map<String, Object> definition;
  private JXPathContext context;
  private final String NAME_ATTR = "name";
  private final String VALUE_ATTR = "value";
  private final String DEFAULT_ATTR = "default";
  private final String CACHE_ENABLED_ATTR = "enabled";
  private final String CACHE_DURATION_ATTR = "duration";
  private final String CACHE_ELEMENT_NAME = "Cache";
  private final String KEY_ELEMENT_NAME = "Key";

  public Cache() {
  }

  public Cache( JXPathContext context ) {
    this.context = context;
  }

  @Override
  public void renderInto( Element dataAccess ) throws JSONException {
    String isCacheEnabled = (String) definition.get( VALUE_ATTR );
    String cacheDuration = context.getValue( "properties/.[name='cacheDuration']/value" ).toString();

    Document doc = dataAccess.getOwnerDocument();
    Element cache = doc.createElement( CACHE_ELEMENT_NAME );
    cache.setAttribute( CACHE_ENABLED_ATTR, isCacheEnabled );
    cache.setAttribute( CACHE_DURATION_ATTR, cacheDuration );
    dataAccess.appendChild( cache );

    JSONArray cacheKeys = new JSONArray(
        context.getValue( "properties/.[name='cacheKeys']/value" ).toString() );

    if ( cacheKeys.length() == 0 ) {
      return;
    }
    for ( int i = 0; i < cacheKeys.length(); i++ ) {
      JSONArray content = cacheKeys.getJSONArray( i );
      Element key = doc.createElement( KEY_ELEMENT_NAME );

      key.setAttribute( NAME_ATTR, (String) content.get( 0 ) );
      key.setAttribute( VALUE_ATTR, (String) content.get( 1 ) );
      if ( content.length() > 2 ) {
        key.setAttribute( DEFAULT_ATTR, (String) content.get( 2 ) );
      }
      cache.appendChild( key );
    }
  }

  @Override
  public void setDefinition( Map<String, Object> definition ) {
    this.definition = definition;
  }

  public void setContext( JXPathContext context ) {
    this.context = context;
  }
}
