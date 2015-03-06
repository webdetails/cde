/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components.amd;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

public class CdfRunJsParameterComponentWriter extends JsWriterAbstract implements IThingWriter {
  public void write( Object output, IThingWriteContext context, Thing t ) throws ThingWriteException {
    this.write( (StringBuilder) output, (CdfRunJsDashboardWriteContext) context, (ParameterComponent) t );
  }

  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp )
    throws ThingWriteException {
    String name = JsonUtils.toJsString( context.getId( comp ) );
    String value = JsonUtils.toJsString( comp.tryGetPropertyValue( "propertyValue", "" ) );
    String viewRole = JsonUtils.toJsString( comp.tryGetPropertyValue( "parameterViewRole", "unused" ) );
    Boolean isBookmarkable = Boolean.valueOf( comp.tryGetPropertyValue( "bookmarkable", null ) );

    // when writing a dashboard as an AMD module, we want to refer to "this" instead of "dashboard"
    final String targetDash = context.getOptions().isAmdModule() ? "this" : "dashboard";

    addSetParameterAssignment( out, name, value, targetDash );
    if ( isBookmarkable ) {
      addBookmarkable( out, name, targetDash );
    }
    addViewMode( out, name, viewRole, targetDash );
  }

  protected static void addSetParameterAssignment( StringBuilder out, String name, String value, String targetDash ) {
    out.append( targetDash )
        .append( ".addParameter(" )
        .append( name )
        .append( ", " )
        .append( value )
        .append( ");" )
        .append( NEWLINE );
  }

  protected static void addViewMode( StringBuilder out, String name, String viewRole, String targetDash ) {
    out.append( targetDash )
        .append( ".setParameterViewMode(" )
        .append( name )
        .append( ", " )
        .append( viewRole )
        .append( ");" )
        .append( NEWLINE );
  }

  protected static void addBookmarkable( StringBuilder out, String name, String targetDash ) {
    out.append( targetDash )
        .append( ".setBookmarkable(" )
        .append( name )
        .append( ");" )
        .append( NEWLINE );
  }
}
