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



package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.DataSourceComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

public class DataSourceComponent extends NonVisualComponent<DataSourceComponentType> {
  protected DataSourceComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );
  }

  @Override
  public DataSourceComponentType getMeta() {
    return super.getMeta();
  }

  /**
   * Class to create and modify DataSourceComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder {
    @Override
    public DataSourceComponent build( MetaModel metaModel ) throws ValidationException {
      return new DataSourceComponent( this, metaModel );
    }
  }
}
