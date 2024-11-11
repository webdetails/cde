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


package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

public class CggRunJsDashboardWriteContext extends DefaultThingWriteContext {
  private final Dashboard _dash;

  public CggRunJsDashboardWriteContext(
    IThingWriterFactory factory,
    Dashboard dash ) {
    super( factory, true );

    if ( dash == null ) {
      throw new IllegalArgumentException( "dash" );
    }
    this._dash = dash;
  }

  public Dashboard getDashboard() {
    return this._dash;
  }
}
