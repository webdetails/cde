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

package org.pentaho.ctools.cde.datasources.manager;

import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceProvider;

/**
 * Class used for managing a list of known Data Source providers.
 * Note: In OSGi environments data sources, other than the bundle resources, are no currently supported. This is a
 * dummy class that is currently required by CDE core.
 */
public class DataSourceManager implements IDataSourceManager {

  public DataSourceManager() { }

  @Override
  public JSONObject getProviderJsDefinition( String providerId ) {
    return null;
  }

  @Override
  public JSONObject getProviderJsDefinition( String providerId, boolean bypassCacheRead ) {
    return null;
  }

  /**
   * Obtains a DataSourceProvider given its id.
   *
   * @param id Data Source Provider Id
   * @return DataSourceProvider if found, null otherwise
   */
  @Override
  public IDataSourceProvider getProvider( String id ) {
    return null;
  }

  /**
   * Lists currently loaded DataSourceProvider instances.
   *
   * @return List of currently loaded DataSourceProvider
   */
  @Override
  public List<IDataSourceProvider> getProviders() {
    return Collections.emptyList();
  }

  /**
   * Refreshes the Data Source Providers cache.
   */
  @Override
  public void refresh() { }
}
