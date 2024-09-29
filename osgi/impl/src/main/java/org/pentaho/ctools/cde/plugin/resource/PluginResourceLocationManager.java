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

package org.pentaho.ctools.cde.plugin.resource;

import java.util.Collections;
import java.util.List;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cpf.packager.origin.PathOrigin;

/**
 * Class used for accessing custom components provided by other plugins.
 * Note: In OSGi environments custom components are only made available via the available
 * {@code IReadAccess} resource services. This is a dummy class that is currently required by CDE core.
 */
public class PluginResourceLocationManager implements IPluginResourceLocationManager {

  @Override
  public String getMessagePropertiesResourceLocation() {
    return null;
  }

  @Override
  public String getStyleResourceLocation( String styleName ) {
    return null;
  }

  @Override
  public synchronized List<PathOrigin> getCustomComponentsLocations() {
    return Collections.emptyList();
  }
}
