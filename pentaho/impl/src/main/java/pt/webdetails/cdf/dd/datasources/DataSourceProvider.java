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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;
import pt.webdetails.cdf.dd.InterPluginBroker;

public class DataSourceProvider implements IDataSourceProvider {
  public static final String DATA_SOURCE_DEFINITION_METHOD_NAME = "listDataAccessTypes";

  String pluginId;

  private static Log logger = LogFactory.getLog( DataSourceProvider.class );

  /**
   * @param pluginId Plugin that contains Data Source definitions
   * @throws InvalidDataSourceProviderException when passed provider is null
   */
  public DataSourceProvider( String pluginId ) throws InvalidDataSourceProviderException {
    assert pluginId != null;
    this.pluginId = pluginId;
  }

  public JSONObject getDataSourceDefinitions( boolean refresh ) {
    try {
      String dsDefinitions =
          InterPluginBroker.getDataSourceDefinitions( pluginId, null, DATA_SOURCE_DEFINITION_METHOD_NAME, refresh );
      return new JSONObject( dsDefinitions );
    } catch ( Exception ex ) {
      logger.error( ex.getMessage(), ex );
      return null;
    }
  }

  public String getId() {
    return pluginId;
  }

  @Override
  public String toString() {
    return String.format( "DataSourceProvider [pluginId=%s]", pluginId );
  }
}
