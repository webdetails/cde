/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
