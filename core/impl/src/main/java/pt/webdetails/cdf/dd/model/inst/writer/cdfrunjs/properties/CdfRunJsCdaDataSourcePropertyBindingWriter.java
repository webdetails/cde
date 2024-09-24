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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.properties;

import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;

public final class CdfRunJsCdaDataSourcePropertyBindingWriter extends CdfRunJsDataSourcePropertyBindingWriter {
  @Override
  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, PropertyBinding propBind )
      throws ThingWriteException {
    String dataSourceName = propBind.getValue();
    DataSourceComponent dataSourceComp = context.getDashboard().tryGetDataSource( dataSourceName );
    if ( dataSourceComp != null ) {
      String dataAccessId = dataSourceComp.tryGetPropertyValue( "dataAccessId", null );

      String indent = context.getIndent();

      addJsProperty( out, "dataAccessId", buildJsStringValue( dataAccessId ), indent, context.isFirstInList() );

      context.setIsFirstInList( false );

      addJsProperty( out, "solution", buildJsStringValue(
        dataSourceComp.tryGetPropertyValue( "solution", "" ) ), indent, false );
      addJsProperty( out, "path",     buildJsStringValue(
        dataSourceComp.tryGetPropertyValue( "path",     "" ) ), indent, false );
      addJsProperty( out, "file",     buildJsStringValue(
        dataSourceComp.tryGetPropertyValue( "file",     "" ) ), indent, false );
    }
  }
}
