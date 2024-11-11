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
