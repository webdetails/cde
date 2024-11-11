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


package pt.webdetails.cdf.dd.extapi;

import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.context.api.IUrlProvider;

public class CdeApiPathProvider implements ICdeApiPathProvider {

  private IUrlProvider urlProvider;

  public CdeApiPathProvider( IUrlProvider urlProvider ) {
    this.urlProvider = urlProvider;
  }

  @Override
  public String getRendererBasePath() {
    return Util.joinPath( urlProvider.getPluginBaseUrl(), "renderer" );
  }

  @Override
  public String getPluginStaticBaseUrl() {
    return urlProvider.getPluginStaticBaseUrl();
  }

  @Override
  public String getResourcesBasePath() {
    return Util.joinPath( urlProvider.getPluginBaseUrl(), "resources" );
  }

}
