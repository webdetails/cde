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



package pt.webdetails.cdf.dd.model.meta.validation;

public final class ComponentTypeDuplicatePropertyError extends ComponentTypeError {
  private final String _propertyName;

  public ComponentTypeDuplicatePropertyError( String componentTypeLabel, String propertyName ) {
    super( componentTypeLabel );

    if ( propertyName == null ) {
      throw new IllegalArgumentException( "propertyName" );
    }

    this._propertyName = propertyName;
  }

  public String getPropertyName() {
    return this._propertyName;
  }

  @Override
  public String toString() {
    return "ComponentType '" + this._componentTypeLabel + "' already has a property named '" + this._propertyName
      + "'.";
  }
}
