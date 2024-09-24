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

package pt.webdetails.cdf.dd;

import java.util.List;

import pt.webdetails.cpf.packager.origin.PathOrigin;

public interface IPluginResourceLocationManager {

  public String getMessagePropertiesResourceLocation();

  public String getStyleResourceLocation( String styleName );

  List<PathOrigin> getCustomComponentsLocations();
}
