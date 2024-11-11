/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.meta.validation;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

public abstract class ComponentTypeError extends ValidationError {
  protected final String _componentTypeLabel;

  public ComponentTypeError( String componentTypeLabel ) throws IllegalArgumentException {
    if ( StringUtils.isEmpty( componentTypeLabel ) ) {
      throw new IllegalArgumentException( "componentTypeLabel" );
    }

    this._componentTypeLabel = componentTypeLabel;
  }

  public String getComponentLabel() {
    return this._componentTypeLabel;
  }

  @Override
  public String toString() {
    return "Component type '" + this._componentTypeLabel + "' is invalid.";
  }
}
