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

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public final class PrimitiveComponentType extends GenericComponentType {
  private PrimitiveComponentType( Builder builder, IPropertyTypeSource propSource ) throws ValidationException {
    super( builder, propSource );
  }

  /**
   * Class to create and modify PrimitiveComponentType instances.
   */
  public static final class Builder extends GenericComponentType.Builder {
    @Override
    public PrimitiveComponentType build( IPropertyTypeSource propSource ) throws ValidationException {
      if ( propSource == null ) {
        throw new IllegalArgumentException( "propSource" );
      }

      return new PrimitiveComponentType( this, propSource );
    }
  }
}
