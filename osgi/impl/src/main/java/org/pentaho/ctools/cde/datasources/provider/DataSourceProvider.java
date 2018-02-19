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

package org.pentaho.ctools.cde.datasources.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;

public class DataSourceProvider implements IDataSourceProvider {
  private static Log logger = LogFactory.getLog( DataSourceProvider.class );
  private final String pluginId;

  public DataSourceProvider( String pluginId ) {
    assert pluginId != null;
    this.pluginId = pluginId;
  }

  public JSONObject getDataSourceDefinitions( boolean refresh ) {
    logger.fatal( "Not implemented for the OSGi environment" );
    return null;
  }

  /**
   * Returns the identifier of the plugin that contains the data source definitions.
   */
  public String getId() {
    return pluginId;
  }

  @Override
  public String toString() {
    return String.format( "DataSourceProvider [pluginId=%s]", getId() );
  }
}
