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

package pt.webdetails.cdf.dd.model.meta.validation;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.meta.Resource;

public final class ComponentTypeDuplicateResourceError extends ComponentTypeError {
  private final Resource.Type _resourceType;
  private final String _resourceName;

  public ComponentTypeDuplicateResourceError( String componentTypeLabel, Resource.Type resourceType,
                                              String resourceName ) {
    super( componentTypeLabel );

    if ( resourceType == null ) {
      throw new IllegalArgumentException( "resourceType" );
    }
    if ( StringUtils.isEmpty( resourceName ) ) {
      throw new IllegalArgumentException( "resourceName" );
    }

    this._resourceType = resourceType;
    this._resourceName = resourceName;
  }

  public Resource.Type getResourceType() {
    return this._resourceType;
  }

  public String getResourceName() {
    return this._resourceName;
  }

  @Override
  public String toString() {
    return "ComponentType '" + this._componentTypeLabel + "' already has a resource named '" + this._resourceName
      + "' of type '" + this._resourceType + "'.";
  }
}
