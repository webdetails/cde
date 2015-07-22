/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package pt.webdetails.cdf.dd.datasources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;
import pt.webdetails.cdf.dd.InterPluginBroker;

public class DataSourceProvider implements IDataSourceProvider {
  public static final String DATA_SOURCE_DEFINITION_METHOD_NAME = "listDataAccessTypes";

  String pluginId;

  private static Log logger = LogFactory.getLog(DataSourceProvider.class);

  /**
   * 
   * @param provider PLugin that contains Data Source definitions
   * @throws InvalidDataSourceProviderException when passed provider is null
   */
  public DataSourceProvider(String pluginId) throws InvalidDataSourceProviderException
  {
    assert pluginId != null;
    this.pluginId = pluginId;
  }
  
  public JSONObject getDataSourceDefinitions(boolean refresh)
  {
    try 
    {
      String dsDefinitions = InterPluginBroker.getDataSourceDefinitions(pluginId, null, DATA_SOURCE_DEFINITION_METHOD_NAME, refresh);
      return new JSONObject( dsDefinitions );
    } 
    catch(Exception ex)
    {
      logger.error(ex.getMessage(), ex);
      return null;
    }
  }

  public String getId()
  {
    return pluginId;
  }

  @Override
  public String toString() 
  {
    return String.format("DataSourceProvider [pluginId=%s]", pluginId);
  }
}