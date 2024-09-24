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
