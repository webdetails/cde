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

import pt.webdetails.cdf.dd.model.core.Entity;
import pt.webdetails.cdf.dd.model.meta.MetaObject;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public abstract class Instance<TM extends MetaObject> extends Entity {
  private final TM _meta;

  protected Instance( Builder<TM> builder ) throws ValidationException {
    super( builder );

    if ( builder._meta == null ) {
      throw new ValidationException( new RequiredAttributeError( "Meta" ) );
    }

    this._meta = builder._meta;
  }

  public TM getMeta() {
    return this._meta;
  }

  /**
   * Class to create and modify Instance instances.
   */
  public abstract static class Builder<TM extends MetaObject> extends Entity.Builder {
    private TM _meta;

    public TM getMeta() {
      return this._meta;
    }

    public Builder<TM> setMeta( TM meta ) {
      this._meta = meta;
      return this;
    }
  }
}
