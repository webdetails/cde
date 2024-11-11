/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cdf.dd.datasources;

import java.util.List;

import org.json.JSONObject;

public interface IDataSourceManager {

  public List<IDataSourceProvider> getProviders();

  public IDataSourceProvider getProvider( String id );

  public JSONObject getProviderJsDefinition( String providerId );

  public JSONObject getProviderJsDefinition( String providerId, boolean bypassCacheRead );

  public void refresh();

}
