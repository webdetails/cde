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


package pt.webdetails.cdf.dd.model.core.validation;

public final class RequiredAttributeError extends ValidationError {
  protected final String _name;

  public RequiredAttributeError( String name ) {
    if ( name == null ) {
      throw new IllegalArgumentException( "name" );
    }

    this._name = name;
  }

  @Override
  public String toString() {
    return "Attribute '" + this._name + "' is required.";
  }
}
