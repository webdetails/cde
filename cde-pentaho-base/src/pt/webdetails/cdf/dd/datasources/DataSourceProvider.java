/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package pt.webdetails.cdf.dd.datasources;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  
  public JSON getDataSourceDefinitions(boolean refresh)
  {
    try 
    {
      String dsDefinitions = InterPluginBroker.getDataSourceDefinitions(pluginId, null, DATA_SOURCE_DEFINITION_METHOD_NAME, refresh);
      return JSONSerializer.toJSON(dsDefinitions);
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

//  protected void checkValid(InterPluginCall.Plugin ipcPlugin) throws InvalidDataSourceProviderException 
//  {
//    InterPluginCall ipc = new InterPluginCall(ipcPlugin, DATA_SOURCE_DEFINITION_METHOD_NAME);
//    if(!ipc.pluginExists()) 
//    {
//      throw new InvalidDataSourceProviderException(String.format("%s not found!", this));
//    }
//
//    /*
//     * TODO(rafa) 
//     * 
//     * check if there is a better way to check if a given plugin has a method
//     * called DATA_SOURCE_DEFINITION_METHOD_NAME defined
//     */
//    String result = null;
//    try 
//    {
//      result = ipc.call();
//    } 
//    catch(Exception e) 
//    {
//      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
//          DATA_SOURCE_DEFINITION_METHOD_NAME, this), e);
//    }
//
//    if(StringUtils.isEmpty(result)) 
//    {
//      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
//          DATA_SOURCE_DEFINITION_METHOD_NAME, this));
//    }
//
//  }

  @Override
  public String toString() 
  {
    return String.format("DataSourceProvider [pluginId=%s]", pluginId);
  }
}