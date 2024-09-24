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
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

public final class WidgetComponent extends GenericComponent<WidgetComponentType> {
  private WidgetComponent( Builder builder, MetaModel metaModel ) throws ValidationException {
    super( builder, metaModel );
  }

  @Override
  public WidgetComponentType getMeta() {
    return super.getMeta();
  }

  public String getWcdfPath() {
    return this.tryGetAttributeValue( "wcdf", null );
  }

  /**
   * Class to create and modify WidgetComponent instances.
   */
  public static class Builder extends GenericComponent.Builder {
    @Override
    public WidgetComponent build( MetaModel metaModel ) throws ValidationException {
      if ( metaModel == null ) {
        throw new IllegalArgumentException( "metaModel" );
      }

      return new WidgetComponent( this, metaModel );
    }
  }
}
