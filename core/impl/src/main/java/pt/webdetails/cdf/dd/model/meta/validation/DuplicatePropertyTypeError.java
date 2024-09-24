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

import pt.webdetails.cdf.dd.model.meta.PropertyType;

public final class DuplicatePropertyTypeError extends PropertyTypeError {
  public DuplicatePropertyTypeError( PropertyType prop ) {
    super( getPropName( prop ) );
  }

  @Override
  public String toString() {
    return "PropertyType '" + this._propertyName + "' is defined twice.";
  }

  private static String getPropName( PropertyType prop ) {
    if ( prop == null ) {
      throw new IllegalArgumentException( "prop" );
    }
    return prop.getName();
  }
}
