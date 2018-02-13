/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
