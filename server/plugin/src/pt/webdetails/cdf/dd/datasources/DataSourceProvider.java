/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package pt.webdetails.cdf.dd.datasources;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.slf4j.LoggerFactory;

import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.plugins.Plugin;

public class DataSourceProvider {

  public static final String DATA_SOURCE_DEFINITION_METHOD_NAME = "listDataAccessTypes";

  private Plugin provider;

  private InterPluginCall.Plugin providerPlugin;

  protected org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * 
   * @param provider PLugin that contains Data Source definitions
   * @throws InvalidDataSourceProviderException when passed provider is null
   */
  public DataSourceProvider(Plugin provider) throws InvalidDataSourceProviderException {
    
    if (provider == null) {
      throw new InvalidDataSourceProviderException("Null provider passed");
    }

    setProvider(provider);
  }

  protected void checkIfIsValid(InterPluginCall.Plugin plugin) throws InvalidDataSourceProviderException {

    InterPluginCall ipc = new InterPluginCall(plugin, DATA_SOURCE_DEFINITION_METHOD_NAME);
    if (!ipc.pluginExists()) {
      throw new InvalidDataSourceProviderException(String.format("%s not found!", this));
    }

    /*
     * TODO(rafa) 
     * 
     * check if there is a better way to check if a given plugin has a method
     * called DATA_SOURCE_DEFINITION_METHOD_NAME defined
     */
    String result = null;
    try {
      result = ipc.call();
    } catch (Exception e) {
      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
          DATA_SOURCE_DEFINITION_METHOD_NAME, this), e);
    }

    if (result == null || result.equals("")) {
      throw new InvalidDataSourceProviderException(String.format("error calling method %s in %s",
          DATA_SOURCE_DEFINITION_METHOD_NAME, this));
    }

  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DataSourceProvider)) {
      return false;
    }
    DataSourceProvider other = (DataSourceProvider) obj;
    if (provider == null) {
      if (other.provider != null) {
        return false;
      }
    } else if (!provider.equals(other.provider)) {
      return false;
    }
    return true;
  }

  public JSON getDataSourceDefinitions() {
    JSON result = null;

    InterPluginCall listDataAccessTypes = new InterPluginCall(this.providerPlugin, DATA_SOURCE_DEFINITION_METHOD_NAME);
    listDataAccessTypes.setSession(PentahoSessionHolder.getSession());
    listDataAccessTypes.putParameter("refreshCache", "true");

    try {
      String dsDefinitions = listDataAccessTypes.call();
      result = JSONSerializer.toJSON(dsDefinitions);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    return result;
  }

  public String getId() {
    return provider.getId();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((provider == null) ? 0 : provider.hashCode());
    return result;
  }

  private void setProvider(Plugin provider) throws InvalidDataSourceProviderException {

    InterPluginCall.Plugin interPluginCall = new InterPluginCall.Plugin(provider.getId(), provider.getName());
    checkIfIsValid(interPluginCall);

    this.provider = provider;
    this.providerPlugin = interPluginCall;

  }

  @Override
  public String toString() {
    String result = "";

    if (provider != null) {
      result = String.format("DataSourceProvider [id=%s, name=%s, path=%s]", provider.getId(), provider.getName(),
          provider.getPath());
    }

    return result;
  }

}
