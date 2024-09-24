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

package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Atom;
import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.PropertyType;
import pt.webdetails.cdf.dd.model.meta.PropertyTypeUsage;

public abstract class PropertyBinding extends Atom {
  private final Component _owner;
  private final String _value;

  protected PropertyBinding( Builder builder, Component owner, MetaModel metaModel ) throws ValidationException {
    assert builder != null;
    assert owner != null;
    assert metaModel != null;

    this._owner = owner;
    this._value = StringUtils.defaultIfEmpty( builder._value, "" );
  }

  @Override
  public final String getKind() {
    return KnownThingKind.PropertyBinding;
  }

  public abstract PropertyTypeUsage getPropertyUsage();

  /*
   The following fields store the information of a cdfdhe property record:
   {
      "name": "color",
      "value": "",
      "type": "Color"
   }
   */
  public abstract String getAlias();

  public final String getValue() {
    return this._value;
  }

  public abstract String getInputType(); // Stored in cdfde...

  public abstract PropertyType getProperty();

  public Component getOwner() {
    return this._owner;
  }

  public String getName() {
    return this.getProperty().getName();
  }

  public abstract static class Builder extends Atom.Builder {
    private String _value;

    public abstract String getAlias();

    public String getValue() {
      return this._value;
    }

    public Builder setValue( String value ) {
      this._value = value;
      return this;
    }

    public abstract PropertyBinding build( Component owner, MetaModel metaModel )
      throws ValidationException;
  }
}
