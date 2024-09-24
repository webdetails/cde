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

public final class ComponentDuplicatePropertyBindingError extends ComponentError {
  private final String _propertyAlias;

  public ComponentDuplicatePropertyBindingError( String propertyAlias, String componentId, String componentTypeLabel )
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
      + "' has a duplicate property binding for name/alias '" + this._propertyAlias + "'.";
  }
}
