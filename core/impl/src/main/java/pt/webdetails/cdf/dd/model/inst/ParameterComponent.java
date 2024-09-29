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
import pt.webdetails.cdf.dd.model.meta.ParameterComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

public class ParameterComponent extends NonVisualComponent<ParameterComponentType> {
  protected ParameterComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );
  }

  @Override
  public ParameterComponentType getMeta() {
    return super.getMeta();
  }

  /**
   * Class to create and modify ParameterComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder {
    @Override
    public ParameterComponent build( MetaModel metaModel ) throws ValidationException {
      return new ParameterComponent( this, metaModel );
    }
  }
}
