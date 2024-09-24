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

package pt.webdetails.cdf.dd.model.meta;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.meta.validation.ComponentTypeUndefinedPropertyError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public final class PropertyTypeUsage {
  private final String _alias;
  private final String _definitionName;
  private final PropertyType _prop;

  private PropertyTypeUsage( Builder builder, ComponentType owner, IPropertyTypeSource propSource )
    throws ValidationException {
    assert builder != null;
    assert owner != null;
    assert propSource != null;

    String propName = builder._propName;
    if ( StringUtils.isEmpty( propName ) ) {
      throw new ValidationException( new RequiredAttributeError( "PropertyName" ) );
    }

    PropertyType prop = propSource.getProperty( propName );
    if ( prop == null ) {
      throw new ValidationException(
        new ComponentTypeUndefinedPropertyError( owner.getLabel(), propName ) );
    }

    String alias = builder._alias;
    if ( StringUtils.isEmpty( alias ) ) {
      alias = propName;
    }

    this._alias = alias;
    this._definitionName = StringUtils.defaultIfEmpty( builder._definitionName, "" );
    this._prop = prop;
  }

  public final String getAlias() {
    return this._alias;
  }

  public final boolean isAliased() {
    return !this._alias.equals( this.getName() );
  }

  public final String getName() {
    return this._prop.getName();
  }

  public final boolean isOwned() {
    return this._prop.getOwner() != null;
  }

  public final PropertyType getProperty() {
    return this._prop;
  }

  public final String getDefinitionName() {
    return this._definitionName;
  }

  public static final class Builder {
    private String _alias;
    private String _propName;
    private String _definitionName;

    public Builder() {
    }

    public String getAlias() {
      return this._alias;
    }

    public Builder setAlias( String alias ) {
      this._alias = alias;
      return this;
    }

    public String getName() {
      return this._propName;
    }

    public Builder setName( String name ) {
      this._propName = name;
      return this;
    }

    public String getDefinitionName() {
      return this._definitionName;
    }

    public Builder setDefinitionName( String definitionName ) {
      this._definitionName = definitionName;
      return this;
    }

    public PropertyTypeUsage build( ComponentType owner, IPropertyTypeSource propSource ) throws ValidationException {
      if ( owner == null ) {
        throw new IllegalArgumentException( "owner" );
      }
      if ( propSource == null ) {
        throw new IllegalArgumentException( "propSource" );
      }

      return new PropertyTypeUsage( this, owner, propSource );
    }
  }
}
