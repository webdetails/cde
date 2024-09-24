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

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

public class PrimitiveComponent<TM extends PrimitiveComponentType> extends GenericComponent<TM> {
  protected PrimitiveComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );
  }

  @Override
  public TM getMeta() {
    return super.getMeta();
  }

  /**
   * Class to create and modify PrimitiveComponent instances.
   */
  public static class Builder extends GenericComponent.Builder {
    @Override
    public PrimitiveComponent build( MetaModel metaModel ) throws ValidationException {
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new PrimitiveComponent( this, metaModel );
    }
  }
}
