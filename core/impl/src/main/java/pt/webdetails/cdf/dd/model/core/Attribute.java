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

package pt.webdetails.cdf.dd.model.core;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public final class Attribute {
  private final String _name;
  private final String _value;

  private Attribute( Builder builder ) throws ValidationException {
    assert builder != null;

    this._name  = builder._name  == null ? "" : builder._name;
    this._value = builder._value == null ? "" : builder._value;
  }

  public String getName() {
    return this._name;
  }

  public String getValue() {
    return this._value;
  }

  public static final class Builder {
    private String _name;
    private String _value;

    public Builder() { }

    public String getName() {
      return this._name;
    }

    public Builder setName( String name ) {
      this._name = name;
      return this;
    }

    public String getValue() {
      return this._value;
    }

    public Builder setValue( String value ) {
      this._value = value;
      return this;
    }

    public Attribute build() throws ValidationException {
      return new Attribute( this );
    }
  }
}
