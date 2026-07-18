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

public abstract class VisualComponentType extends ComponentType {
  protected VisualComponentType( Builder builder, IPropertyTypeSource propSource )
    throws ValidationException {
    super( builder, propSource );
  }

  public abstract static class Builder extends ComponentType.Builder {
    @Override
    public abstract VisualComponentType build( IPropertyTypeSource propSource )
      throws ValidationException;
  }
}
