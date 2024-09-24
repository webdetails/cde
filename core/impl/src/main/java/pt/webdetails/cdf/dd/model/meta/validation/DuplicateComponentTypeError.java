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

import pt.webdetails.cdf.dd.model.meta.ComponentType;

public final class DuplicateComponentTypeError extends ComponentTypeError {
  public DuplicateComponentTypeError( ComponentType comp ) {
    super( getCompLabel( comp ) );
  }

  @Override
  public String toString() {
    return "ComponentType '" + this._componentTypeLabel + "' is defined twice.";
  }

  private static String getCompLabel( ComponentType comp ) {
    if ( comp == null ) {
      throw new IllegalArgumentException( "comp" );
    }
    return comp.getLabel();
  }
}
