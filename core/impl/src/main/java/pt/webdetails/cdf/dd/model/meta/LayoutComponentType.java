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

public final class LayoutComponentType extends VisualComponentType {
  protected LayoutComponentType( Builder builder, IPropertyTypeSource propSource ) throws ValidationException {
    super( builder, propSource );
  }

  /**
   * Class to create and modify LayoutComponentType instances.
   */
  public static final class Builder extends VisualComponentType.Builder {
    @Override
    public final LayoutComponentType build( IPropertyTypeSource propSource ) throws ValidationException {
      return new LayoutComponentType( this, propSource );
    }
  }
}
