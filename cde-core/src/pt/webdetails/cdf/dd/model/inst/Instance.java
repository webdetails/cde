/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.Entity;
import pt.webdetails.cdf.dd.model.meta.MetaObject;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public abstract class Instance<TM extends MetaObject> extends Entity
{
  private final TM _meta;

  protected Instance(Builder<TM> builder) throws ValidationException
  {
    super(builder);

    if(builder._meta == null)
    {
      throw new ValidationException(new RequiredAttributeError("Meta"));
    }

    this._meta = builder._meta;
  }

  public TM getMeta()
  {
    return this._meta;
  }
  
  /**
   * Class to create and modify Instance instances.
   */
  public static abstract class Builder<TM extends MetaObject> extends Entity.Builder
  {
    private TM _meta;

    public TM getMeta()
    {
      return this._meta;
    }

    public Builder<TM> setMeta(TM meta)
    {
      this._meta = meta;
      return this;
    }
  }
}
