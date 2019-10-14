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
