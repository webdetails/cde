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

package pt.webdetails.cdf.dd.model.meta.validation;

import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

public abstract class PropertyTypeError extends ValidationError {
  protected final String _propertyName;

  public PropertyTypeError( String propertyName ) throws IllegalArgumentException {
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
    return "PropertyType '" + this._propertyName + "' is invalid.";
  }
}
