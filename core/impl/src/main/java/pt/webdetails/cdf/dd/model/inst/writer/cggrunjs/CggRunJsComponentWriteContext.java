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
