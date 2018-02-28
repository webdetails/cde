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

package org.pentaho.ctools.cde.cache.impl;

import java.io.IOException;
import java.util.List;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import pt.webdetails.cdf.dd.DashboardCacheKey;
import pt.webdetails.cdf.dd.cache.api.ICache;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Allows caching {@code CdfRunJsDashboardWriteResult} objects referenced by {@code DashboardCacheKey}.
 * Both these types are serializable.
 * {@code CdfRunJsDashboardWriteResult} objects are an almost-final render of a given dashboard and options.
 * The cached values allow rendering a dashboard multiple times, with different options.
 * These are re-built whenever the corresponding WCDF and/or CDFDE files have changed.
 */
public final class Cache implements ICache {
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";
  private final Ehcache ehcache;

  public Cache( IReadAccess readAccess ) throws InitializationException {
    CacheManager cacheManager;
    try {
      cacheManager = CacheManager.create( readAccess.getFileInputStream( CACHE_CFG_FILE ) );
    } catch ( IOException e ) {
      throw new InitializationException( "Failed to load the cache configuration file: " + CACHE_CFG_FILE, e );
    } catch ( CacheException e ) {
      throw new InitializationException( "Failed to create the cache manager.", e );
    }

    // enableCacheProperShutdown
    System.setProperty( CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true" );

    if ( !cacheManager.cacheExists( CACHE_NAME ) ) {
      cacheManager.addCache( CACHE_NAME );
    }

    this.ehcache = cacheManager.getCache( CACHE_NAME );
  }

  public CdfRunJsDashboardWriteResult get( DashboardCacheKey key ) {
    Element element = this.ehcache.get( key );

    if ( element != null ) {
      return (CdfRunJsDashboardWriteResult) element.getValue();
    }
    return null;
  }

  public List<DashboardCacheKey> getKeys() {
    return (List<DashboardCacheKey>) this.ehcache.getKeys();
  }

  public void remove( DashboardCacheKey key ) {
    this.ehcache.remove( key );
  }

  public void removeAll() {
    this.ehcache.removeAll();
  }

  public void put( DashboardCacheKey key, CdfRunJsDashboardWriteResult value ) {
    Element element = new Element( key, value );
    this.ehcache.put( element );
  }
}
