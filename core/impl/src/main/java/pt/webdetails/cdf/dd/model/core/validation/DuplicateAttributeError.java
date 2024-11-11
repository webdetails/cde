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

import org.apache.commons.lang.StringUtils;

public final class DuplicateAttributeError extends ValidationError {
  private final String _attributeName;

  public DuplicateAttributeError( String attributeName ) {
    if ( StringUtils.isEmpty( attributeName ) ) {
      throw new IllegalArgumentException( "attributeName" );
    }

    this._attributeName = attributeName;
  }

  public String getAttributeName() {
    return this._attributeName;
  }

  @Override
  public String toString() {
    return "Attribute '" + this._attributeName + "' is already defined.";
  }
}
