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
