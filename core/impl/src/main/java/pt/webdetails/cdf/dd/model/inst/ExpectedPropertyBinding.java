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

package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;

/**
 * A property binding that is identified in a component type,
 * as a component type usage.
 */
public final class ExpectedPropertyBinding extends PropertyBinding {
  private final PropertyTypeUsage _propUsage;

  private ExpectedPropertyBinding( Builder builder, Component owner, MetaModel metaModel )
      throws ValidationException {
    super( builder, owner, metaModel );

    PropertyTypeUsage propUsage = builder._propUsage;
    if ( propUsage == null ) {
      throw new ValidationException( new RequiredAttributeError( "PropertyUsage" ) );
    }

    this._propUsage = propUsage;
  }

  @Override
  public final PropertyTypeUsage getPropertyUsage() {
    return this._propUsage;
  }

  @Override
  public final String getAlias() {
    return this._propUsage.getAlias();
  }

  @Override
  public PropertyType getProperty() {
    return this._propUsage.getProperty();
  }

  @Override
  public String getInputType() {
    return this.getProperty().getInputType();
  }

  public static final class Builder extends PropertyBinding.Builder {
    private PropertyTypeUsage _propUsage;

    @Override
    public String getAlias() {
      return this._propUsage != null ? this.getAlias() : null;
    }

    public PropertyTypeUsage getPropertyUsage() {
      return this._propUsage;
    }

    public Builder setPropertyUsage( PropertyTypeUsage propUsage ) {
      this._propUsage = propUsage;
      return this;
    }

    @Override
    public ExpectedPropertyBinding build( Component owner, MetaModel metaModel ) throws ValidationException {
      if ( owner == null ) {
        throw new IllegalArgumentException( "owner" );
      }
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new ExpectedPropertyBinding( this, owner, metaModel );
    }
  }
}
