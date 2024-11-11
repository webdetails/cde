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
