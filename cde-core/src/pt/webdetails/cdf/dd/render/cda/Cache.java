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


import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

public class Cache implements CdaElementRenderer {

  private JSONObject definition;
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
  public void renderInto( Element dataAccess ) {
    String isCacheEnabled = definition.getString( VALUE_ATTR );
    String cacheDuration = context.getValue( "properties/.[name='cacheDuration']/value" ).toString();

    Document doc = dataAccess.getOwnerDocument();
    Element cache = doc.createElement( CACHE_ELEMENT_NAME );
    cache.setAttribute( CACHE_ENABLED_ATTR, isCacheEnabled );
    cache.setAttribute( CACHE_DURATION_ATTR, cacheDuration );
    dataAccess.appendChild( cache );

    JSONArray cacheKeys = JSONArray.fromObject(
      context.getValue( "properties/.[name='cacheKeys']/value" ).toString() );

    if ( cacheKeys.isEmpty() ) {
      return;
    }

    @SuppressWarnings( "unchecked" )
    Iterator<JSONArray> keysIterator = cacheKeys.iterator();
    while ( keysIterator.hasNext() ) {
      JSONArray content = keysIterator.next();
      Element key = doc.createElement( KEY_ELEMENT_NAME );

      key.setAttribute( NAME_ATTR, (String) content.get( 0 ) );
      key.setAttribute( VALUE_ATTR, (String) content.get( 1 ) );
      if ( content.size() > 2 ) {
        key.setAttribute( DEFAULT_ATTR, (String) content.get( 2 ) );
      }
      cache.appendChild( key );
    }
  }

  @Override
  public void setDefinition( JSONObject definition ) {
    this.definition = definition;
  }

  public void setContext( JXPathContext context ) {
    this.context = context;
  }
}
