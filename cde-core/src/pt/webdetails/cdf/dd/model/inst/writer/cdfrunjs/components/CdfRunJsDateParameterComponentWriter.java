/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdfRunJsDateParameterComponentWriter extends CdfRunJsParameterComponentWriter {
  private static final SimpleDateFormat _format = new SimpleDateFormat( "yyyy-MM-dd" );

  @Override
  public void write( StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp )
    throws ThingWriteException {
    String name = JsonUtils.toJsString( context.getId( comp ) );
    String value = JsonUtils.toJsString( resolveDateValue( comp.tryGetPropertyValue( "propertyDateValue", "" ) ) );
    Boolean isBookmarkable = "true".equalsIgnoreCase( comp.tryGetPropertyValue( "bookmarkable", null ) );

    addSetParameterAssignment( out, name, value );
    if ( isBookmarkable ) {
      addBookmarkable( out, name );
    }
  }

  private String resolveDateValue( String value ) {
    if ( value.equals( "today" ) ) {
      Calendar cal = Calendar.getInstance();
      return _format.format( cal.getTime() );
    }

    if ( value.equals( "yesterday" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.DATE, -1 );
      return _format.format( cal.getTime() );
    }

    if ( value.equals( "lastWeek" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.DATE, -7 );
      return _format.format( cal.getTime() );
    }

    if ( value.equals( "lastMonth" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.MONTH, -1 );
      return _format.format( cal.getTime() );
    }

    if ( value.equals( "monthStart" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.set( Calendar.DATE, 1 );
      return _format.format( cal.getTime() );
    }

    if ( value.equals( "yearStart" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.set( Calendar.MONTH, 0 );
      cal.set( Calendar.DATE, 1 );
      return _format.format( cal.getTime() );
    }

    return value;
  }
}
