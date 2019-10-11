/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.legacy;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.CalendarUtils;
import pt.webdetails.cdf.dd.util.JsonUtils;

public class CdfRunJsDateParameterComponentWriter extends CdfRunJsParameterComponentWriter {

  @Override
  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp )
    throws ThingWriteException {
    String name = JsonUtils.toJsString( context.getId( comp ) );
    String value = JsonUtils.toJsString(
        CalendarUtils.resolveDateValue( comp.tryGetPropertyValue( "propertyDateValue", "" ) ) );
    Boolean isBookmarkable = "true".equalsIgnoreCase( comp.tryGetPropertyValue( "bookmarkable", null ) );

    addSetParameterAssignment( out, name, value );
    if ( isBookmarkable ) {
      addBookmarkable( out, name );
    }
  }
}
