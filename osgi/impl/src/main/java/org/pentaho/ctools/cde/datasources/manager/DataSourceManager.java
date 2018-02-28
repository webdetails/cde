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
