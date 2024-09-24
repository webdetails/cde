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
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.PropertyBinding;
import pt.webdetails.cdf.dd.util.JsonUtils;

public class CdfRunJsJFreeChartDataSourcePropertyBindingWriter extends CdfRunJsDataSourcePropertyBindingWriter {
  @Override
  protected DataSourceComponent getDataSourceComponent( CdfRunJsDashboardWriteContext context,
                                                        PropertyBinding propBind ) {
    DataSourceComponent ds = super.getDataSourceComponent( context, propBind );
    if ( ds == null ) {
      // TODO: ? Is this some kind of backward compatibility HACK?
      // Maybe works because for most components, 
      // only «expected» properties are rendered...
      String dataSourceName = propBind.getOwner().tryGetPropertyValue( "dataSource", null );
      if ( dataSourceName != null ) {
        ds = context.getDashboard().tryGetDataSource( dataSourceName );
      }
    }

    return ds;
  }

  @Override
  protected void renderCdaDatasource( StringBuilder out, CdfRunJsDashboardWriteContext context,
                                      DataSourceComponent dataSourceComp, String dataAccessId ) {
    String indent = context.getIndent();

    addJsProperty( out, "dataAccessId", buildJsStringValue( dataAccessId ), indent, context.isFirstInList() );

    context.setIsFirstInList( false );

    // Check if we have a cdaFile
    String cdaPath = dataSourceComp.tryGetPropertyValue( "cdaPath", null );
    if ( cdaPath != null ) {
      addJsProperty( out, "cdaFile",   buildJsStringValue( cdaPath ), indent, false );
      addJsProperty( out, "queryType", JsonUtils.toJsString( "cda" ), indent, false );
    } else {
      // legacy
      addJsProperty( out, "solution", buildJsStringValue( dataSourceComp.tryGetPropertyValue( "solution", "" ) ), indent, false );
      addJsProperty( out, "path",     buildJsStringValue( dataSourceComp.tryGetPropertyValue( "path",     "" ) ), indent, false );
      addJsProperty( out, "file",     buildJsStringValue( dataSourceComp.tryGetPropertyValue( "file",     "" ) ), indent, false );
    }
  }
}
