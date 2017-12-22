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

package pt.webdetails.cdf.dd.datasources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;

public class DataSourceManager implements IDataSourceManager {
  public static final String CDE_DATASOURCE_IDENTIFIER = "cde-datasources";

  private static Log logger = LogFactory.getLog( DataSourceManager.class );

  private static DataSourceManager instance;

  // The map key is the data source provider id.
  private final Map<String, DataSourceProvider> providersById;

  // The map key is the data source provider id.
  private final Map<String, JSONObject> providerDefinitionsById;

  private boolean _isRefresh;

  public static DataSourceManager getInstance() {

    if ( instance == null ) {
      instance = new DataSourceManager();
    }

    return instance;
  }

  private DataSourceManager() {
    this.providersById = new LinkedHashMap<String, DataSourceProvider>();
    this.providerDefinitionsById = new HashMap<String, JSONObject>();
    init( false );
  }

  /**
   * Searches the solution system components folders for declaration of data sources for CDE Editor
   *
   * @return List of file paths containing declaration of data sources for CDE Editor
   */
  private List<DataSourceProvider> readProviders() {

    List<DataSourceProvider> dataSourceProviders = new ArrayList<DataSourceProvider>();

    PluginsAnalyzer pluginsAnalyzer =
        new PluginsAnalyzer( CdeEnvironment.getContentAccessFactory(), PentahoSystem.get( IPluginManager.class ) );
    pluginsAnalyzer.refresh();

    List<PluginsAnalyzer.PluginWithEntity> pluginsWithEntity =
        pluginsAnalyzer.getRegisteredEntities( "/" + CDE_DATASOURCE_IDENTIFIER );

    for ( PluginsAnalyzer.PluginWithEntity entity : pluginsWithEntity ) {
      String provider = entity.getPlugin().getId();

      try {
        DataSourceProvider ds = new DataSourceProvider( provider );
        dataSourceProviders.add( ds );
        logger.debug( String.format( "Found valid CDE Data Source provider: %s", ds ) );
      } catch ( InvalidDataSourceProviderException e ) {
        logger.error( String.format( "Found invalid CDE Data Source provider in plugin %s.", provider ) );
      }
    }

    return dataSourceProviders;
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
      synchronized ( providerDefinitionsById ) {
        result = providerDefinitionsById.get( providerId );
      }
    }

    if ( result == null ) {
      DataSourceProvider provider;
      synchronized ( providersById ) {
        provider = providersById.get( providerId );
      }

      if ( provider != null ) {
        result = provider.getDataSourceDefinitions( this._isRefresh );
        if ( result != null ) {
          synchronized ( providerDefinitionsById ) {
            providerDefinitionsById.put( providerId, result );
          }
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
    synchronized ( providersById ) {
      return providersById.get( id );
    }
  }

  /**
   * Lists currently loaded DataSourceProvider instances.
   *
   * @return List of currently loaded DataSourceProvider
   */
  public List<IDataSourceProvider> getProviders() {
    synchronized ( providersById ) {
      return new ArrayList<IDataSourceProvider>( providersById.values() );
    }
  }

  private void init( boolean isRefresh ) {
    List<DataSourceProvider> providers = readProviders();

    synchronized ( providersById ) {
      this._isRefresh = isRefresh;

      try {
        providersById.clear();
        providerDefinitionsById.clear();

        for ( DataSourceProvider ds : providers ) {
          logger.info( String.format( "Loaded DataSourceProvider: id=%s, object=%s", ds.getId(), ds ) );
          providersById.put( ds.getId(), ds );
        }

        logger.debug( "Successfully initialized." );
      } catch ( Exception e ) {
        logger.error( "Error initializing.", e );
      }
    }
  }

  /**
   * Refreshes the Data Source Providers cache.
   */
  public void refresh() {
    init( true );
  }
}
