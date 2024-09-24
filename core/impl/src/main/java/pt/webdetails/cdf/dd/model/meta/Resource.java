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
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cpf.packager.origin.PathOrigin;

public final class Resource {
  private final String name;
  private final String app;
  private final String version;
  private final String source;
  private final Type type;
  private final PathOrigin origin;

  private Resource( Builder builder ) throws ValidationException {
    assert builder != null;

    if ( StringUtils.isEmpty( builder._source ) ) {
      throw new ValidationException( new RequiredAttributeError( "Source" ) );
    }

    if ( builder._type == null ) {
      throw new ValidationException( new RequiredAttributeError( "Type" ) );
    }

    this.name = StringUtils.defaultIfEmpty( builder._name, builder._source );
    this.app = StringUtils.defaultIfEmpty( builder._app, "" );
    this.source = builder._source;
    this.version = StringUtils.defaultIfEmpty( builder._version, "1.0" );
    // TODO: validate version format

    this.type = builder._type;

    this.origin = builder.origin;
  }

  // -------------
  // Properties
  public static String buildKey( Type type, String name ) {
    return type + "|" + ( name == null ? "" : name );
  }

  public String getKey() {
    return buildKey( this.type, this.name );
  }

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }

  public String getSource() {
    return this.source;
  }

  public Type getType() {
    return this.type;
  }

  //TODO: doc
  public String getApp() {
    return this.app;
  }


  public PathOrigin getOrigin() {
    return origin;
  }
  // ------------


  public enum Type {
    SCRIPT,
    RAW, // Raw code //TODO: can't raw be either?
    STYLE
  }

  //TODO: this builder is just a reiteration of resource..
  public static final class Builder {
    private String _name;
    private String _version;
    private String _source;
    private Type _type;
    private String _app;
    private PathOrigin origin;

    // ----------
    // Properties

    public String getName() {
      return this._name;
    }

    public Builder setName( String name ) {
      this._name = name;
      return this;
    }

    public String getVersion() {
      return this._version;
    }

    public Builder setVersion( String version ) {
      this._version = version;
      return this;
    }

    public String getSource() {
      return this._source;
    }

    public Builder setSource( String source ) {
      this._source = source;
      return this;
    }

    public Type getType() {
      return this._type;
    }

    public Builder setType( Type type ) {
      this._type = type;
      return this;
    }

    public String getApp() {
      return this._app;
    }

    public Builder setApp( String app ) {
      this._app = app;
      return this;
    }

    public Builder setOrigin( PathOrigin origin ) {
      this.origin = origin;
      return this;
    }

    public Resource build() throws ValidationException {
      return new Resource( this );
    }
  }
}
