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
