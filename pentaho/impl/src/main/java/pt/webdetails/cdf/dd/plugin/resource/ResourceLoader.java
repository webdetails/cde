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

package pt.webdetails.cdf.dd.plugin.resource;

import java.io.UnsupportedEncodingException;

import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.resources.IResourceLoader;

public class ResourceLoader implements IResourceLoader {

  protected Log logger = LogFactory.getLog( getClass() );
  private IPluginResourceLoader pluginResourceLoader;

  public ResourceLoader( IPluginResourceLoader pluginResourceLoader ) {
    this.pluginResourceLoader = pluginResourceLoader;
  }

  @Override
  public String getPluginSetting( Class<?> arg0, String arg1 ) {
    return pluginResourceLoader.getPluginSetting( arg0, arg1 );
  }

  @Override
  public String getResourceAsString( Class<? extends Object> arg0, String arg1 ) {
    try {
      return pluginResourceLoader.getResourceAsString( arg0, arg1 );
    } catch ( UnsupportedEncodingException e ) {
      logger.error( "ResourceLoader.getResourceAsString()", e );
    }
    return null;
  }
}
