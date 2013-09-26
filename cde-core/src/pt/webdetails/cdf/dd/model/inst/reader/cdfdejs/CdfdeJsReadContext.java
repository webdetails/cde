/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.reader.cdfdejs;

import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

/**
 * @author dcleao
 */
public class CdfdeJsReadContext implements IThingReadContext// extends DefaultThingReadContext
{
  private final DashboardWcdfDescriptor _wcdf;
  private final MetaModel _metaModel;
  private CdfdeJsThingReaderFactory factory;
  
  public CdfdeJsReadContext(CdfdeJsThingReaderFactory factory, DashboardWcdfDescriptor wcdf, MetaModel metaModel)
  {
    assert factory != null;
    assert wcdf != null;
    assert metaModel != null;

    this.factory = factory;

    this._wcdf = wcdf;
    this._metaModel = metaModel;
  }
  
  public final DashboardWcdfDescriptor getWcdf()
  {
    return this._wcdf;
  }
  
  public final MetaModel getMetaModel()
  {
    return this._metaModel;
  }

  public CdfdeJsThingReaderFactory getFactory() {
    return factory;
  }
}
