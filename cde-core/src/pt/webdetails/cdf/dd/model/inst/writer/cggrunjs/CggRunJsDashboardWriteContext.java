/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

/**
 * @author dcleao
 */
public class CggRunJsDashboardWriteContext extends DefaultThingWriteContext
{
  private final Dashboard _dash;
  
  public CggRunJsDashboardWriteContext(
          IThingWriterFactory factory, 
          Dashboard dash)
  {
    super(factory, true);
    
    if(dash == null) { throw new IllegalArgumentException("dash"); }
    this._dash  = dash;
  }
  
  public Dashboard getDashboard()
  {
    return this._dash;
  }
}
