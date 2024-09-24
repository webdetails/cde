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
