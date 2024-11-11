/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public final class ParameterComponentType extends NonVisualComponentType {
  protected ParameterComponentType( Builder builder, IPropertyTypeSource propSource )
    throws ValidationException {
    super( builder, propSource );
  }

  public static final class Builder extends NonVisualComponentType.Builder {
    @Override
    public ParameterComponentType build( IPropertyTypeSource propSource )
      throws ValidationException {
      if ( propSource == null ) {
        throw new IllegalArgumentException( "propSource" );
      }

      return new ParameterComponentType( this, propSource );
    }
  }
}
