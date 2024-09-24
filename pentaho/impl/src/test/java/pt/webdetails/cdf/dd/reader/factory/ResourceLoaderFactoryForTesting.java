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

import pt.webdetails.cdf.dd.ICdeEnvironment;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;

public class ResourceLoaderFactoryForTesting extends ResourceLoaderFactory {

  private boolean systemStatic;
  private boolean repositoryStatic;

  public ResourceLoaderFactoryForTesting() {
    this.systemStatic = false;
    this.repositoryStatic = false;
  }

  public void setSystemStatic( boolean value ) {
    this.systemStatic = value;
  }

  public void setRepositoryStatic( boolean value ) {
    this.repositoryStatic = value;
  }

  @Override
  protected SystemResourceLoader getSystemResourceLoader( String path ) {
    return new SystemResourceLoader();
  }

  @Override
  protected SolutionResourceLoader getSolutionResourceLoader( String path ) {
    return new SolutionResourceLoader();
  }

  @Override
  protected ICdeEnvironment getCdeEnvironment() {
    return null;
  }

  @Override
  protected IContentAccessFactory getContentAccessFactory( ICdeEnvironment environment ) {
    return null;
  }

  @Override
  protected String getSystemDir( ICdeEnvironment environment ) {
    return "system/";
  }

  @Override
  protected String getPluginRepositoryDir( ICdeEnvironment environment ) {
    return "public/cde/";
  }

  @Override
  protected boolean isSystemStaticResource( IContentAccessFactory factory, String path ) {
    return systemStatic;
  }

  @Override
  protected boolean isRepositoryStaticResource( IContentAccessFactory factory, String path ) {
    return repositoryStatic;
  }

}
