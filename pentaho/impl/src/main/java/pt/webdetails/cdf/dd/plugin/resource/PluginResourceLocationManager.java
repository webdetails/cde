/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
