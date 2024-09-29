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

public class DataSourceComponentType extends NonVisualComponentType {
  protected DataSourceComponentType( Builder builder, IPropertyTypeSource propSource )
    throws ValidationException {
    super( builder, propSource );
  }

  public static class Builder extends NonVisualComponentType.Builder {
    public DataSourceComponentType build( IPropertyTypeSource propSource )
      throws ValidationException {
      return new DataSourceComponentType( this, propSource );
    }
  }
}
