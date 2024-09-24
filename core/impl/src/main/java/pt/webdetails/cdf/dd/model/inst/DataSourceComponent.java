/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
