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
import pt.webdetails.cdf.dd.model.meta.CustomComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

public class CustomComponent<TM extends CustomComponentType> extends GenericComponent<TM> {
  protected CustomComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );
  }

  @Override
  public TM getMeta() {
    return super.getMeta();
  }

  /**
   * Class to create and modify CustomComponent instances.
   */
  public static class Builder extends GenericComponent.Builder {
    @Override
    public CustomComponent build( MetaModel metaModel ) throws ValidationException {
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new CustomComponent( this, metaModel );
    }
  }
}
