/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.input;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * Location in the repository, basePath from repository root 
 */
public class RepositoryOrigin extends PathOrigin {

  public RepositoryOrigin(String basePath) {
    super(basePath);
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getPluginRepositoryReader(RepositoryHelper.appendPath("..", basePath));
  }

  @Override
  public String getUrl( String localPath ) {
    // TODO: cpf-friendly
    // want it resolving to: /pentaho/content/pentaho-cdf-dd/res/<basePath>/<localPath>
    return RepositoryHelper.joinPaths( CdeEngine.getEnv().getRepositoryBaseContentUrl(), basePath, localPath );
  }

}
