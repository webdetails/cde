/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.model.core.writer.js;

import org.apache.commons.lang.StringUtils;
import static pt.webdetails.cdf.dd.CdeConstants.Writer.NEWLINE;

public abstract class JsWriterAbstract {

  protected static void addCommaAndLineSep( StringBuilder out ) {
    out.append( "," ).append( NEWLINE );
  }

  //TODO: review if this is used
  protected static void addVar( StringBuilder out, String name, String value ) {
    out.append( "var " );
    addAssignmentWithOr( out, name, value );
  }

  protected static void addAssignmentWithOr( StringBuilder out, String name, String value ) {
    out.append( name ).append( " = " ).append( name ).append( " || " ).append( value ).append( ";" ).append( NEWLINE );
  }

  protected static void addAssignment( StringBuilder out, String name, String value ) {
    out.append( name ).append( " = " ).append( value ).append( ";" ).append( NEWLINE );
  }

  protected static void addFirstJsProperty( StringBuilder out, String name, String jsValue, String indent ) {
    addJsProperty( out, name, jsValue, indent, true );
  }

  protected static void addJsProperty( StringBuilder out, String name, String jsValue, String indent ) {
    addJsProperty( out, name, jsValue, indent, false );
  }

  protected static void addJsProperty( StringBuilder out, String name, String jsValue, String indent,
                                       boolean isFirst ) {
    if ( !isFirst ) {
      addCommaAndLineSep( out );
    }

    if ( StringUtils.isNotEmpty( indent ) ) {
      out.append( indent );
    }

    out.append( name ).append( ": " ).append( jsValue );
  }

  protected static String wrapJsScriptTags( String code ) {
    StringBuilder out = new StringBuilder();
    wrapJsScriptTags( out, code );
    return out.toString();
  }

  protected static void wrapJsScriptTags( StringBuilder out, String code ) {
    out
      .append( NEWLINE )
      .append( "<script language=\"javascript\" type=\"text/javascript\">" )
      .append( NEWLINE )
      .append( code )
      .append( NEWLINE )
      .append( "</script>" )
      .append( NEWLINE );
  }
}
