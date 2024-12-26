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


package pt.webdetails.cdf.dd.cache.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import pt.webdetails.cdf.dd.DashboardCacheKey;
import pt.webdetails.cdf.dd.cache.api.ICache;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
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
  private static final String ENABLE_SHUTDOWN_HOOK_PROPERTY = "javax.cache.CacheManager.enableShutdownHook";
  private javax.cache.Cache ehcache;

  public Cache( IContentAccessFactory contentAccessFactory ) throws InitializationException {

    CacheManager cacheManager;
    try {
      CachingProvider cachingProvider = Caching.getCachingProvider();
      URI configUri = getClass().getClassLoader().getResource( CACHE_CFG_FILE ).toURI();
      cacheManager = cachingProvider.getCacheManager( configUri, getClass().getClassLoader() );
    } catch ( CacheException e ) {
      throw new InitializationException( "Failed to create the cache manager.", e );
    } catch ( URISyntaxException e ) {
        throw new RuntimeException( "Failed to load the cache configuration file: " + CACHE_CFG_FILE, e );
    }

    // enableCacheProperShutdown
    System.setProperty( ENABLE_SHUTDOWN_HOOK_PROPERTY, "true" );

    if ( cacheManager.getCache( CACHE_NAME ) == null ) {
        MutableConfiguration<Object,Object> config = new MutableConfiguration<>().setStoreByValue( false );
      cacheManager.createCache( CACHE_NAME, config );
    }

    this.ehcache = cacheManager.getCache( CACHE_NAME );
  }

  public CdfRunJsDashboardWriteResult get( DashboardCacheKey key ) {
    Object element = this.ehcache.get( key );

    if ( element != null ) {
      return (CdfRunJsDashboardWriteResult) element;
    }
    return null;
  }

  public List<DashboardCacheKey> getKeys() {
    final List<DashboardCacheKey> keys = new ArrayList<>();
    ehcache.iterator().forEachRemaining( entry -> keys.add( ( ( javax.cache.Cache.Entry<DashboardCacheKey, CdfRunJsDashboardWriteResult> ) entry ).getKey() ) );
    return keys;
  }

  public void remove( DashboardCacheKey key ) {
    this.ehcache.remove( key );
  }

  public void removeAll() {
    this.ehcache.removeAll();
  }

  public void put( DashboardCacheKey key, CdfRunJsDashboardWriteResult value ) {
    this.ehcache.put( key, value );
  }
}
