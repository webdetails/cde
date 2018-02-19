/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
package org.pentaho.ctools.cde.datasources.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.ctools.cde.datasources.provider.DataSourceProvider;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;

public class DataSourceManager implements IDataSourceManager {
  private static Log logger = LogFactory.getLog( DataSourceManager.class );
  // The map key is the data source provider id.
  private final Map<String, DataSourceProvider> providersById;
  // The map key is the data source provider id.
  private final Map<String, JSONObject> providerDefinitionsById;
  private boolean _isRefresh;

  public DataSourceManager() {
    this.providersById = new LinkedHashMap<>();
    this.providerDefinitionsById = new HashMap<>();
    this._isRefresh = false;
  }

  public JSONObject getJsDefinition() throws JSONException {
    JSONObject dsSpec = new JSONObject();

    // TODO: this code seems to make more ifs than necessary...
    for ( IDataSourceProvider provider : getProviders() ) {
      JSONObject dsDefinition = this.getProviderJsDefinition( provider.getId() );
      if ( dsDefinition != null && ( dsDefinition.length() > 0 ) ) {
        if ( dsDefinition instanceof JSONObject ) {
          JSONObject obj = dsDefinition;
          if ( !obj.equals( JSONObject.NULL ) ) {
            for ( String key : JSONObject.getNames( obj ) ) {
              dsSpec.put( key, obj.get( key ) );
            }
          }
        }
      }
    }

    return dsSpec;
  }

  public JSONObject getProviderJsDefinition( String providerId ) {
    return this.getProviderJsDefinition( providerId, false );
  }

  public JSONObject getProviderJsDefinition( String providerId, boolean bypassCacheRead ) {
    JSONObject result = null;

    if ( !bypassCacheRead ) {
      result = providerDefinitionsById.get( providerId );
    }

    if ( result == null ) {
      DataSourceProvider provider = providersById.get( providerId );

      if ( provider != null ) {
        result = provider.getDataSourceDefinitions( this._isRefresh );
        if ( result != null ) {
          providerDefinitionsById.put( providerId, result );
        }
      }
    }

    return result;
  }

  /**
   * Obtains a DataSourceProvider given its id.
   *
   * @param id Data Source Provider Id
   * @return DataSourceProvider if found, null otherwise
   */
  public DataSourceProvider getProvider( String id ) {
    return providersById.get( id );
  }

  /**
   * Lists currently loaded DataSourceProvider instances.
   *
   * @return List of currently loaded DataSourceProvider
   */
  public List<IDataSourceProvider> getProviders() {
    return new ArrayList<IDataSourceProvider>( providersById.values() );
  }

  private void init( boolean isRefresh ) {
    this._isRefresh = isRefresh;

    try {
      providersById.clear();
      providerDefinitionsById.clear();

      logger.debug( "Successfully initialized." );
    } catch ( Exception e ) {
      logger.error( "Error initializing.", e );
    }
  }

  /**
   * Refreshes the Data Source Providers cache.
   */
  public void refresh() {
    init( true );
  }
}
