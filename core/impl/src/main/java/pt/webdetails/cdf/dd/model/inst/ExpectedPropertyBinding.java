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
