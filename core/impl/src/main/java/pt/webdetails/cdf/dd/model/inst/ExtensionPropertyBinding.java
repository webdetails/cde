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

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;

/**
 * A property binding that is not identified to exist, as a property type usage, in the component type,
 * but for which the MetaModel contains a global property of same name.
 */
public final class ExtensionPropertyBinding extends PropertyBinding {
  private final PropertyType _prop;
  private final String _alias; // will always be = to prop.getName()?

  private ExtensionPropertyBinding( Builder builder, Component owner, MetaModel metaModel ) throws ValidationException {
    super( builder, owner, metaModel );

    PropertyType prop = builder._prop;
    if ( prop == null ) {
      throw new ValidationException( new RequiredAttributeError( "Property" ) );
    }

    String alias = builder._alias;
    if ( StringUtils.isEmpty( alias ) ) {
      alias = prop.getName();
    }

    this._alias = alias;
    this._prop = prop;
  }

  @Override
  public String getAlias() {
    return this._alias;
  }

  @Override
  public PropertyType getProperty() {
    return this._prop;
  }

  @Override
  public String getInputType() {
    return this._prop.getInputType();
  }

  @Override
  public final PropertyTypeUsage getPropertyUsage() {
    return null;
  }

  public static final class Builder extends PropertyBinding.Builder {
    private String _alias;
    private PropertyType _prop;

    @Override
    public String getAlias() {
      return this._alias;
    }

    public Builder setAlias( String alias ) {
      this._alias = alias;
      return this;
    }

    public PropertyType getProperty() {
      return this._prop;
    }

    public Builder setProperty( PropertyType prop ) {
      this._prop = prop;
      return this;
    }

    @Override
    public ExtensionPropertyBinding build( Component owner, MetaModel metaModel )
      throws ValidationException {
      if ( owner == null ) {
        throw new IllegalArgumentException( "owner" );
      }
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new ExtensionPropertyBinding( this, owner, metaModel );
    }
  }
}
