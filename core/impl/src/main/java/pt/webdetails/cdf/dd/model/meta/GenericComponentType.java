/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

public abstract class GenericComponentType extends VisualComponentType {
  protected GenericComponentType( Builder builder, IPropertyTypeSource propSource ) throws ValidationException {
    super( builder, propSource );
  }

  /**
   * Class to create and modify GenericComponentType instances.
   */
  public static abstract class Builder extends VisualComponentType.Builder {
    public Builder() {
      super();

      this.useProperty( null, "priority" );
    }

    @Override
    public abstract GenericComponentType build( IPropertyTypeSource propSource ) throws ValidationException;
  }
}
