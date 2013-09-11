/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package pt.webdetails.cdf.dd.datasources;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.slf4j.LoggerFactory;

import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.plugins.Plugin;

public class DataSourceProvider 
{
  public static final String DATA_SOURCE_DEFINITION_METHOD_NAME = "listDataAccessTypes";

  private Plugin provider;

  private InterPluginCall.Plugin providerPlugin;

  protected org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 
   * @param provider PLugin that contains Data Source definitions
   * @throws InvalidDataSourceProviderException when passed provider is null
   */
  public DataSourceProvider(Plugin provider) throws InvalidDataSourceProviderException
  {
    if(provider == null) { throw new IllegalArgumentException("provider"); }

    setProvider(provider);
  }
  
  public JSON getDataSourceDefinitions(boolean refresh)
  {
    JSON result = null;

    InterPluginCall listDATypesCall = new InterPluginCall(this.providerPlugin, DATA_SOURCE_DEFINITION_METHOD_NAME);
    listDATypesCall.setSession(PentahoSessionHolder.getSession());
    listDATypesCall.putParameter("refreshCache", "" + refresh);

    try 
    {
      String dsDefinitions = listDATypesCall.call();
      result = JSONSerializer.toJSON(dsDefinitions);
    } 
    catch(Exception ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return result;
  }

  public String getId()
  {
    return provider.getId();
  }

  private void setProvider(Plugin provider) throws InvalidDataSourceProviderException 
  {
    InterPluginCall.Plugin ipcPlugin = new InterPluginCall.Plugin(provider.getId(), provider.getName());
    
    checkValid(ipcPlugin);

    this.provider = provider;
    this.providerPlugin = ipcPlugin;
  }
  
  
  protected void checkValid(InterPluginCall.Plugin ipcPlugin) throws InvalidDataSourceProviderException 
  {
    InterPluginCall ipc = new InterPluginCall(ipcPlugin, DATA_SOURCE_DEFINITION_METHOD_NAME);
    if(!ipc.pluginExists()) 
    {
      throw new InvalidDataSourceProviderException(String.format("%s not found!", this));
    }

    /*
     * TODO(rafa) 
     * 
     * check if there is a better way to check if a given plugin has a method
     * called DATA_SOURCE_DEFINITION_METHOD_NAME defined
     */
    String result = null;
    try 
    {
      result = ipc.call();
    } 
    catch(Exception e) 
    {
      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
          DATA_SOURCE_DEFINITION_METHOD_NAME, this), e);
    }

    if(StringUtils.isEmpty(result)) 
    {
      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
          DATA_SOURCE_DEFINITION_METHOD_NAME, this));
    }

  }
  
  @Override
  public String toString() 
  {
    String result = "";

    if(provider != null) 
    {
      result = String.format(
              "DataSourceProvider [id=%s, name=%s, path=%s]", 
              provider.getId(), 
              provider.getName(),
              provider.getPath());
    }

    return result;
  }
}
