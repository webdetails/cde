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


package pt.webdetails.cdf.dd.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtils {
  protected static final SimpleDateFormat _format = new SimpleDateFormat( "yyyy-MM-dd" );

  public static String resolveDateValue( String value ) {
    if ( value.equals( "today" ) ) {
      Calendar cal = Calendar.getInstance();
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }
    }

    if ( value.equals( "yesterday" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.DATE, -1 );
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }

    }

    if ( value.equals( "lastWeek" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.DATE, -7 );
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }

    }

    if ( value.equals( "lastMonth" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.MONTH, -1 );
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }

    }

    if ( value.equals( "monthStart" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.set( Calendar.DATE, 1 );
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }

    }

    if ( value.equals( "yearStart" ) ) {
      Calendar cal = Calendar.getInstance();
      cal.set( Calendar.MONTH, 0 );
      cal.set( Calendar.DATE, 1 );
      synchronized ( _format ) {
        return _format.format( cal.getTime() );
      }

    }

    return value;
  }
}
