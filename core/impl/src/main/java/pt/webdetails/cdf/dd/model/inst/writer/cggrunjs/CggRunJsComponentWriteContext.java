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


package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;

public class CggRunJsComponentWriteContext extends CggRunJsDashboardWriteContext {
  private final GenericComponent _comp;

  public CggRunJsComponentWriteContext(
    IThingWriterFactory factory,
    Dashboard dash,
    GenericComponent comp ) {
    super( factory, dash );

    if ( comp == null ) {
      throw new IllegalArgumentException( "comp" );
    }
    this._comp = comp;
  }

  public GenericComponent getChartComponent() {
    return this._comp;
  }
}
