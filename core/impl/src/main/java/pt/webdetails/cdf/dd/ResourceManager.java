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


package pt.webdetails.cdf.dd;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Caches files that need token replacements.<br>
 */
public class ResourceManager {

  private static final Log logger = LogFactory.getLog( ResourceManager.class );

  public static ResourceManager instance;

  private static final HashSet<String> CACHEABLE_EXTENSIONS = new HashSet<String>();
  private static final HashMap<String, String> cacheContainer = new HashMap<String, String>();

  private boolean isCacheEnabled = true;

  public ResourceManager() {

    CACHEABLE_EXTENSIONS.add( "html" );
    CACHEABLE_EXTENSIONS.add( "json" );
    CACHEABLE_EXTENSIONS.add( "cdfde" );

    this.isCacheEnabled = Boolean.parseBoolean( CdeEngine.getInstance().getEnvironment().getResourceLoader()
      .getPluginSetting( this.getClass(), "pentaho-cdf-dd/enable-cache" ) );
  }

  public static ResourceManager getInstance() {

    if ( instance == null ) {
      instance = new ResourceManager();
    }

    return instance;
  }

  public boolean existsInCache( final String cacheKey ) {
    return isCacheEnabled && !StringUtils.isEmpty( cacheKey ) && cacheContainer.containsKey( cacheKey );
  }

  public boolean existsInCache( final String path, final HashMap<String, String> tokens ) {
    final String extension = getResourceExtension( path );
    final String cacheKey = buildCacheKey( path, tokens );

    return isCacheEnabled && CACHEABLE_EXTENSIONS.contains( extension ) && cacheContainer.containsKey( cacheKey );
  }

  public boolean isCacheEnabled() {
    return isCacheEnabled;
  }

  public boolean isCacheableExtension( String extension ) {
    return extension != null && CACHEABLE_EXTENSIONS.contains( extension );
  }

  public boolean putResourceInCache( String key, String resource ) {

    boolean success = false;

    if ( isCacheEnabled() ) {
      cacheContainer.put( key, resource );
      success = true;
    }

    return success;
  }

  public String getResourceFromCache( String key ) {

    if ( isCacheEnabled() && cacheContainer.containsKey( key ) ) {
      return cacheContainer.get( key );
    }
    return null;
  }

  public static String buildCacheKey( final String path, final HashMap<String, String> tokens ) {

    final StringBuilder keyBuilder = new StringBuilder( path );

    if ( tokens != null ) {
      keyBuilder.append( tokens.hashCode() );
    }

    return keyBuilder.toString();
  }

  public static String getResourceExtension( final String path ) {
    return path.substring( path.lastIndexOf( '.' ) + 1 );

  }

}
