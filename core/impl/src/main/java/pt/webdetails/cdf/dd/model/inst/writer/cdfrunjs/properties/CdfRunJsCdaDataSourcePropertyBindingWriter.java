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
