/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * To keep track of file origin <u>within the same plugin</u>.<br>
 * Everything needed to reinstantiate a Plugin's IReadAccess, and translate its location to a url.<br>
 */
public abstract class PathOrigin {

    public PathOrigin(String basePath) {
      this.basePath = basePath;
    }
    protected String basePath;

    public abstract IReadAccess getReader(IContentAccessFactory factory);

    /**
     * @return URL path from host
     */
    public abstract String getUrl(String localPath, IUrlProvider urlProvider);

    public String toString() {
      return getClass().getSimpleName() + ":" + basePath;
    }

    @Override
    public boolean equals(Object other) {
      return
          other instanceof PathOrigin &&
          getClass().equals(other.getClass()) &&
          StringUtils.equals( basePath, ((PathOrigin)other).basePath );
    }

    @Override
    public int hashCode() {
      int hash = getClass().hashCode();
      hash *= 73;
      if (!StringUtils.isEmpty( basePath )) {
        hash += basePath.hashCode();
      }
      return hash;
    }
}

