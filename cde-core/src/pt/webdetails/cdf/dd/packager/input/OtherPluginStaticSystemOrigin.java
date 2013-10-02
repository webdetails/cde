/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.input;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;


public class OtherPluginStaticSystemOrigin extends PathOrigin {

  private String pluginId;

  public OtherPluginStaticSystemOrigin(String pluginId, String basePath) {
    super(basePath);
    assert pluginId != null;
    this.pluginId = pluginId;
  }

  public String getPluginId() {
    return pluginId;
  }

  @Override
  public String getUrl(String localPath) {
    // ex.: 
    // pluginId=cdc
    // basePath=static, localPath=css/some.css
    // url-> "/pentaho/content/<pluginId>/..."
    return RepositoryHelper.joinPaths( CdeEngine.getEnv().getApplicationBaseUrl() , "content", pluginId, basePath, localPath );
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getOtherPluginSystemReader(pluginId, basePath);
  }

    @Override
    public boolean equals( Object other ) {
      return 
          super.equals( other ) &&
          other instanceof OtherPluginStaticSystemOrigin &&
          StringUtils.equals( pluginId, ((OtherPluginStaticSystemOrigin) other).pluginId);
    }
  
    @Override
    public int hashCode() {
      int hash = super.hashCode();
      hash *= 73;
      hash += pluginId.hashCode();
      return hash;
    }

    public String toString() {
      return getClass().getSimpleName() + ":" + pluginId + ":" + basePath;
    }
}
