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



package pt.webdetails.cdf.dd.model.inst.validation;

import org.apache.commons.lang3.StringUtils;

public final class ComponentUnresolvedPropertyBindingError extends ComponentError {
  private final String _propertyAlias;

  public ComponentUnresolvedPropertyBindingError( String propertyAlias, String componentId, String componentTypeLabel )
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
      + "' has unresolved property with name/alias '" + this._propertyAlias + "'.";
  }
}
