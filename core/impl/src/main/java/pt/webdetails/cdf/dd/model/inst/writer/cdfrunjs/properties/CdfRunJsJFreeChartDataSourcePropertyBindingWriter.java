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
