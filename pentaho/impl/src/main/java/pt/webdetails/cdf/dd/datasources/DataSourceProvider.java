/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
