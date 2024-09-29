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

public final class WidgetComponentType extends GenericComponentType {
  private WidgetComponentType( Builder builder, IPropertyTypeSource propSource ) throws ValidationException {
    super( builder, propSource );
  }

  /**
   * Class to create and modify WidgetComponentType instances.
   */
  public static final class Builder extends GenericComponentType.Builder {
    @Override
    public WidgetComponentType build( IPropertyTypeSource propSource ) throws ValidationException {
      if ( propSource == null ) {
        throw new IllegalArgumentException( "propSource" );
      }

      return new WidgetComponentType( this, propSource );
    }
  }
}
