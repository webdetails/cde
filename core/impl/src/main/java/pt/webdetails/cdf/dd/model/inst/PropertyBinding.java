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
