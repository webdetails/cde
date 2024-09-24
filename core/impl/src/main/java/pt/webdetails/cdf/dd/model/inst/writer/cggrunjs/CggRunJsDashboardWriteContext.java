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
