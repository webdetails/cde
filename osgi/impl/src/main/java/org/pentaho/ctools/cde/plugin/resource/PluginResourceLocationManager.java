/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
