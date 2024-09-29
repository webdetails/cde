/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
