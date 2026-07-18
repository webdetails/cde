/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cdf.dd.cache.api;

import java.util.List;
import pt.webdetails.cdf.dd.DashboardCacheKey;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

public interface ICache {

  CdfRunJsDashboardWriteResult get( DashboardCacheKey key );

  List<DashboardCacheKey> getKeys();

  void remove( DashboardCacheKey key );

  void removeAll();

  void put( DashboardCacheKey key, CdfRunJsDashboardWriteResult value );
}
