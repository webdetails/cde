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

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

import static pt.webdetails.cdf.dd.CdeConstants.Writer.*;

public class CdfRunJsParameterComponentWriter extends JsWriterAbstract implements IThingWriter {
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (ParameterComponent) t );
  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp )
    throws ThingWriteException {
    String name = JsonUtils.toJsString( comp.getId() );
    String value = JsonUtils.toJsString( comp.tryGetPropertyValue( "propertyValue", "" ) );
    String viewRole = JsonUtils.toJsString( comp.tryGetPropertyValue( "parameterViewRole", "unused" ) );
    Boolean isBookmarkable = Boolean.valueOf( comp.tryGetPropertyValue( "bookmarkable", null ) );

    addSetParameterAssignment( out, name, value );
    if ( isBookmarkable ) {
      addBookmarkable( out, name );
    }
    addViewMode( out, name, viewRole );
  }

  protected static void addSetParameterAssignment( StringBuilder out, String name, String value ) {
    out
      .append( "dashboard.addParameter(" )
      .append( name )
      .append( ", " )
      .append( value )
      .append( ");" )
      .append( NEWLINE );
  }

  protected static void addViewMode( StringBuilder out, String name, String viewRole ) {
    out
      .append( "dashboard.setParameterViewMode(" )
      .append( name )
      .append( ", " )
      .append( viewRole )
      .append( ");" )
      .append( NEWLINE );
  }

  protected static void addBookmarkable( StringBuilder out, String name ) {
    out
      .append( "dashboard.setBookmarkable(" )
      .append( name )
      .append( ");" )
      .append( NEWLINE );
  }
}
