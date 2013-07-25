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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;

/**
 * 
 * @author Rafael P. Gomes<rafael.gomes@webdetails.pt>
 *
 */
public class DataSourceManager {

  public static final String CDE_DATASOURCE_IDENTIFIER = "cde-datasources";

  private static DataSourceManager instance;

  public static synchronized DataSourceManager getInstance() {
    if (instance == null) {
      instance = new DataSourceManager();
    }

    return instance;
  }

  private Logger logger = LoggerFactory.getLogger(getClass());

  // the map key is the data source provider id
  private Map<String, JSON> providerDefinitionCache = new HashMap<String, JSON>();

  // the map key is the data source provider id
  private Map<String, DataSourceProvider> sources = new LinkedHashMap<String, DataSourceProvider>();

  private DataSourceManager() {
    init();
  }

  /**
   * Searches the solution system components folders for declaration of data
   * sources for CDE Editor
   * 
   * @return List of file paths containing declaration of data sources for CDE Editor
   */
  private List<DataSourceProvider> getDataSourceProviders() {
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    List<PluginsAnalyzer.PluginWithEntity> pluginsWithEntity = pluginsAnalyzer.getRegisteredEntities(String.format(
        "/%s", CDE_DATASOURCE_IDENTIFIER));

    List<DataSourceProvider> dataSourceProviders = new ArrayList<DataSourceProvider>();

    for (PluginsAnalyzer.PluginWithEntity entity : pluginsWithEntity) {
      Plugin provider = entity.getPlugin();

      try {
        DataSourceProvider ds = new DataSourceProvider(provider);
        dataSourceProviders.add(ds);
        logger.info("found valid CDE Data Source(s) provider: {}", ds);
      } catch (InvalidDataSourceProviderException e) {
        logger
            .info(
                "found invalid CDE Data Source(s) provider in: {}. Please review plugin implementation and/or configuration.",
                provider.getPath());
      }

    }

    return dataSourceProviders;
  }

  public JSON getDataSourcesJsonDefinition() {
    JSONObject dsSpec = new JSONObject();

    for (DataSourceProvider provider : getProviders()) {
      JSON dsDefinition = getDefinitionFromCache(provider.getId());
      if (dsDefinition != null && !dsDefinition.isEmpty()) {
        if (dsDefinition instanceof JSONObject) {
          JSONObject obj = ((JSONObject) dsDefinition);
          if (!obj.isNullObject()) {
            dsSpec.putAll(obj);
          }
        }
      }
    }

    return dsSpec;
  }

  public JSON getDefinition(String providerId) {
    DataSourceProvider provider = sources.get(providerId);
    JSON result = null;
    if (provider != null) {
      result = provider.getDataSourceDefinitions();
      providerDefinitionCache.put(providerId, result);
    }

    return result;
  }

  public JSON getDefinitionFromCache(String providerId) {
    JSON result = null;

    synchronized (providerDefinitionCache) {
      if (providerDefinitionCache.containsKey(providerId)) {
        result = providerDefinitionCache.get(providerId);
      } else {
        result = getDefinition(providerId);
      }
    }

    return result;
  }

  /**
   * 
   * @param id Data Source Provider Id
   * @return DataSourceProvider if found, null otherwise
   */
  public DataSourceProvider getProvider(String id) {
    return sources.get(id);
  }

  /**
   * Lists current loaded Data Source Providers that have its configuration in cache
   * 
   * @return List of current loaded Data Source Providers
   */
  public List<DataSourceProvider> getProviders() {
    synchronized (sources) {
      return new ArrayList(sources.values());
    }
  }

  /**
   * 
   */
  private void init() {
    synchronized (sources) {
      try {
        List<DataSourceProvider> providers = getDataSourceProviders();

        sources.clear();
        for (DataSourceProvider ds : providers) {
          logger.info("Data source cached: id={}, object={}", ds.getId(), ds);
          sources.put(ds.getId(), ds);
        }

        logger.info("Data Source Manager successfully initialized!");
      } catch (Exception e) {
        logger.error("error initializing Data Source Manager: {}", e.getMessage(), e);
      }
    }
  }

  /**
   * Refreshes the Data Source Providers cache
   */
  public void refresh() {
    init();
  }

  public DataSourceProvider registerProvider(DataSourceProvider provider) throws InvalidDataSourceProviderException {

    if (provider == null) {
      throw new InvalidDataSourceProviderException("Can't register a null data source provider");
    }

    synchronized (sources) {

      if (!sources.containsKey(provider.getId())) {
        sources.put(provider.getId(), provider);
      }

      return provider;
    }
  }

}
