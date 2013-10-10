/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.input;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * For keeping track of paths from static plugin system folders.
 * @see PathOrigin
 */
public class StaticSystemOrigin extends PathOrigin {

  public StaticSystemOrigin( String basePath ) {
    super( basePath );
  }

  public String getUrl( String path, IUrlProvider urlProvider ) {
    return RepositoryHelper.joinPaths(urlProvider.getPluginStaticBaseUrl(), basePath, path );
  }

  public IReadAccess getReader( IContentAccessFactory factory ) {
    return factory.getPluginSystemReader( basePath );
  }
}
