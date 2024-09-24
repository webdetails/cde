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

package pt.webdetails.cdf.dd.model.inst.validation;

import org.apache.commons.lang.StringUtils;

public final class ComponentDuplicatePropertyBindingError extends ComponentError {
  private final String _propertyAlias;

  public ComponentDuplicatePropertyBindingError( String propertyAlias, String componentId, String componentTypeLabel )
      throws IllegalArgumentException {
    super( componentId, componentTypeLabel );

    if ( StringUtils.isEmpty( propertyAlias ) ) {
      throw new IllegalArgumentException( "propertyAlias" );
    }

    this._propertyAlias = propertyAlias;
  }

  public String getPropertyAlias() {
    return this._propertyAlias;
  }

  @Override
  public String toString() {
    return "Component of id '" + this._componentId + "' and type '" + this._componentTypeLabel
      + "' has a duplicate property binding for name/alias '" + this._propertyAlias + "'.";
  }
}
