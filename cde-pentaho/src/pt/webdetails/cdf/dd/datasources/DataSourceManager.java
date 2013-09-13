/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package pt.webdetails.cdf.dd.datasources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;

/**
 * 
 * @author Rafael P. Gomes<rafael.gomes@webdetails.pt>
 *
 */
public class DataSourceManager implements IDataSourceManager {
  public static final String CDE_DATASOURCE_IDENTIFIER = "cde-datasources";
  
  private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
  
  private static DataSourceManager instance;
  
  // The map key is the data source provider id.
  private final Map<String, DataSourceProvider> providersById;
  
  // The map key is the data source provider id.
  private final Map<String, JSON> providerDefinitionsById;

  private boolean _isRefresh;
  
  public static DataSourceManager getInstance(){
    
	  if(instance == null){
		  instance = new DataSourceManager();
	  }
	  
	  return instance;
  }
  
  private DataSourceManager() 
  {
    this.providersById = new LinkedHashMap<String, DataSourceProvider>();
    this.providerDefinitionsById = new HashMap<String, JSON>();
    init(/*isRefresh*/false);
  }
  
  /**
   * Searches the solution system components folders for declaration of data
   * sources for CDE Editor
   * 
   * @return List of file paths containing declaration of data sources for CDE Editor
   */
  private List<DataSourceProvider> readProviders() {
	
	List<DataSourceProvider> dataSourceProviders = new ArrayList<DataSourceProvider>();
	  
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer(CdeEnvironment.getContentAccessFactory(), PentahoSystem.get(IPluginManager.class));
    pluginsAnalyzer.refresh();

    List<PluginsAnalyzer.PluginWithEntity> pluginsWithEntity = pluginsAnalyzer.getRegisteredEntities("/" + CDE_DATASOURCE_IDENTIFIER);

    for(PluginsAnalyzer.PluginWithEntity entity : pluginsWithEntity) {
      Plugin provider = entity.getPlugin();

      try {
        DataSourceProvider ds = new DataSourceProvider(provider);
        dataSourceProviders.add(ds);
        logger.info("Found valid CDE Data Source provider: {}", ds);
      
      } catch(InvalidDataSourceProviderException e) {
        logger.info(
            "Found invalid CDE Data Source provider in: {}. " + 
            "Please review plugin implementation and/or configuration.",
            provider.getPath());
      }
    }

    return dataSourceProviders;
  }

  public JSON getJsDefinition()
  {
    JSONObject dsSpec = new JSONObject();
    
    // TODO: this code seems to make more ifs than necessary...
    for(IDataSourceProvider provider : getProviders()) {
      JSON dsDefinition = this.getProviderJsDefinition(provider.getId());
      if(dsDefinition != null && !dsDefinition.isEmpty()) {
        if(dsDefinition instanceof JSONObject) {
          JSONObject obj = ((JSONObject) dsDefinition);
          if(!obj.isNullObject()) {
            dsSpec.putAll(obj);
          }
        }
      }
    }

    return dsSpec;
  }

  public JSON getProviderJsDefinition(String providerId) 
  {
    return this.getProviderJsDefinition(providerId, false);
  }

  public JSON getProviderJsDefinition(String providerId, boolean bypassCacheRead)
  {
    JSON result = null;
    
    if(!bypassCacheRead)
    {
      synchronized(providerDefinitionsById) 
      {
        result = providerDefinitionsById.get(providerId);
      }
    }
    
    if(result == null) 
    {
      DataSourceProvider provider;
      synchronized(providersById) 
      {
        provider = providersById.get(providerId);
      }
      
      if(provider != null)
      {
        result = provider.getDataSourceDefinitions(this._isRefresh);
        if(result != null) 
        {
          synchronized(providerDefinitionsById)
          {
            providerDefinitionsById.put(providerId, result);
          }
        }
      }
    }
    
    return result;
  }

  /**
   * Obtains a DataSourceProvider given its id.
   * @param id Data Source Provider Id
   * @return DataSourceProvider if found, null otherwise
   */
  public DataSourceProvider getProvider(String id) 
  {
    synchronized(providersById) 
    {
      return providersById.get(id);
    }
  }

  /**
   * Lists currently loaded DataSourceProvider instances.
   * 
   * @return List of currently loaded DataSourceProvider
   */
  public List<IDataSourceProvider> getProviders() {
    synchronized(providersById) {
      return new ArrayList(providersById.values());
    }
  }

  private void init(boolean isRefresh) 
  {
    List<DataSourceProvider> providers = readProviders();
    
    synchronized(providersById)
    {
      this._isRefresh = isRefresh;
    
      try
      {
        providersById.clear();
        providerDefinitionsById.clear();
        
        for(DataSourceProvider ds : providers)
        {
          logger.info("Loaded DataSourceProvider: id={}, object={}", ds.getId(), ds);
          providersById.put(ds.getId(), ds);
        }

        logger.info("Successfully initialized.");
      } catch(Exception e) {
        logger.error("Error initializing: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * Refreshes the Data Source Providers cache.
   */
  public void refresh() 
  {
    init(/*isRefresh*/true);
  }
}
