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

package pt.webdetails.cdf.dd.plugin.resource;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import pt.webdetails.cdf.dd.FsPluginResourceLocations;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.cdf.CdfStyles;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class PluginResourceLocationManager implements IPluginResourceLocationManager {

  private FsPluginResourceLocations componentLocator;

  public IReadAccess[] getAllCustomComponentsResourceLocations() {
    throw new NotImplementedException( "shouldn't be using this anymore" );
  }

  @Override
  public String getMessagePropertiesResourceLocation() {
    return "lang/messages.properties";
  }

  @Override
  public String getStyleResourceLocation( String arg0 ) {
    return new CdfStyles().getResourceLocation( arg0 );
  }

  public synchronized List<PathOrigin> getCustomComponentsLocations() {
    if ( componentLocator == null ) {
      componentLocator = new FsPluginResourceLocations();
    }
    return componentLocator.getCustomComponentLocations();
  }
}
