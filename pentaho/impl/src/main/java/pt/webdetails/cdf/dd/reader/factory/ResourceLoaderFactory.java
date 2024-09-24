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

package pt.webdetails.cdf.dd.reader.factory;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public class ResourceLoaderFactory {

  public ResourceLoaderFactory() {
  }

  public IResourceLoader getResourceLoader( String dashboardPath ) {
    if ( isSystem( dashboardPath ) ) {
      return getSystemResourceLoader( dashboardPath );
    } else {
      return getSolutionResourceLoader( dashboardPath );
    }
  }

  protected boolean isSystem( String path ) {
    if ( path.isEmpty() ) {
      return false;
    }

    ICdeEnvironment env = getCdeEnvironment();
    IContentAccessFactory factory = getContentAccessFactory( env );

    path = StringUtils.strip( path.toLowerCase(), "/" );

    if ( path.startsWith( getSystemDir( env ) ) ) {
      return true;

    } else if ( path.startsWith( getPluginRepositoryDir( env ) ) ) {
      return false;

    } else {
      if ( isSystemStaticResource( factory, path ) ) {
        return true;

      } else if ( isRepositoryStaticResource( factory, path ) ) {
        return false;

      }
    }
    return false;
  }

  protected SystemResourceLoader getSystemResourceLoader( String path ) {
    return new SystemResourceLoader( path );
  }

  protected SolutionResourceLoader getSolutionResourceLoader( String path ) {
    return new SolutionResourceLoader( path );
  }

  protected ICdeEnvironment getCdeEnvironment() {
    return CdeEngine.getInstance().getEnvironment();
  }

  protected IContentAccessFactory getContentAccessFactory( ICdeEnvironment environment ) {
    return environment.getContentAccessFactory();
  }

  protected String getSystemDir( ICdeEnvironment environment ) {
    return environment.getSystemDir() + "/";
  }

  protected String getPluginRepositoryDir( ICdeEnvironment environment ) {
    return environment.getPluginRepositoryDir() + "/";
  }

  protected boolean isSystemStaticResource( IContentAccessFactory factory, String path ) {
    return factory.getPluginSystemReader( null ).fileExists( path );
  }

  protected boolean isRepositoryStaticResource( IContentAccessFactory factory, String path ) {
    return factory.getUserContentAccess( null ).fileExists( path );
  }

}
