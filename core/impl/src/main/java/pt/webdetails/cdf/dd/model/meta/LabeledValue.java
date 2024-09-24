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
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public final class LabeledValue {
  private final String _value;
  private final String _label;

  private LabeledValue( Builder builder ) throws ValidationException {
    assert builder != null;

    String value = builder._value;
    String label = builder._label;
    if ( StringUtils.isEmpty( label ) ) {
      label = value;
    }

    this._value = value;
    this._label = label;
  }

  public String getValue() {
    return this._value;
  }

  public String getLabel() {
    return this._label;
  }

  public final static class Builder {
    private String _value;
    private String _label;

    public Builder() {
    }

    public String getValue() {
      return this._value;
    }

    public Builder setValue( String value ) {
      this._value = value;
      return this;
    }

    public String getLabel() {
      return this._label;
    }

    public Builder setLabel( String label ) {
      this._label = label;
      return this;
    }

    public LabeledValue build() throws ValidationException {
      return new LabeledValue( this );
    }
  }
}
