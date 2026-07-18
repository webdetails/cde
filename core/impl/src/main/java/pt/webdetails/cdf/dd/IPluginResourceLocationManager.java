/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cdf.dd;

import java.util.List;

import pt.webdetails.cpf.packager.origin.PathOrigin;

public interface IPluginResourceLocationManager {

  public String getMessagePropertiesResourceLocation();

  public String getStyleResourceLocation( String styleName );

  List<PathOrigin> getCustomComponentsLocations();
}
