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
import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

public abstract class ComponentError extends ValidationError {
  protected final String _componentId;
  protected final String _componentTypeLabel;

  public ComponentError( String componentId, String componentTypeLabel ) throws IllegalArgumentException {
    if ( StringUtils.isEmpty( componentTypeLabel ) ) {
      throw new IllegalArgumentException( "componentTypeLabel" );
    }

    this._componentId = StringUtils.defaultIfEmpty( componentId, "???" );
    this._componentTypeLabel = componentTypeLabel;
  }

  public String getComponentId() {
    return this._componentId;
  }

  public String getComponentTypeLabel() {
    return this._componentTypeLabel;
  }

  @Override
  public String toString() {
    return "Component of id '" + this._componentId + "' and type '" + this._componentTypeLabel + "' is invalid.";
  }
}
