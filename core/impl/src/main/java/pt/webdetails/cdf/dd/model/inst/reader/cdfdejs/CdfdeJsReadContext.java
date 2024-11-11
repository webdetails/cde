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


package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

public class CdfdeJsReadContext implements IThingReadContext /* extends DefaultThingReadContext */ {
  private final DashboardWcdfDescriptor _wcdf;
  private final MetaModel _metaModel;
  private CdfdeJsThingReaderFactory factory;

  public CdfdeJsReadContext( CdfdeJsThingReaderFactory factory, DashboardWcdfDescriptor wcdf, MetaModel metaModel ) {
    assert factory != null;
    assert wcdf != null;
    assert metaModel != null;

    this.factory = factory;

    this._wcdf = wcdf;
    this._metaModel = metaModel;
  }

  public final DashboardWcdfDescriptor getWcdf() {
    return this._wcdf;
  }

  public final MetaModel getMetaModel() {
    return this._metaModel;
  }

  public CdfdeJsThingReaderFactory getFactory() {
    return factory;
  }
}
