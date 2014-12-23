/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cdf.dd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class JsonUtils {
  private static final int INDENT = 2;

  public static JSON readJsonFromInputStream( final InputStream input ) throws IOException {

    String contents = StringUtils.trim( IOUtils.toString( input, "UTF-8" ) );
    return (JSON) JSONSerializer.toJSON( contents );
  }

  public static void buildJsonResult( final OutputStream out, final Boolean sucess, final Object result ) {

    final JSONObject jsonResult = new JSONObject();

    jsonResult.put( "status", sucess.toString() );
    if ( result != null ) {
      jsonResult.put( "result", result );
    }

    PrintWriter pw = null;
    try {
      pw = new PrintWriter( out );
      pw.print( jsonResult.toString( INDENT ) );
      pw.flush();
    } finally {
      IOUtils.closeQuietly( pw );
    }
  }

  public static String getJsonResult( boolean success, Object result ) {
    final JSONObject jsonResult = new JSONObject();
    jsonResult.put( "status", Boolean.toString( success ) );
    if ( result != null ) {
      jsonResult.put( "result", result );
    }
    return jsonResult.toString( INDENT );
  }

  public static String toJsString( String text ) {
    String content;
    if ( text == null ) {
      content = "";
    } else {
      content = text.replaceAll( "\"", "\\\\\"" )  // replaceBy: |\\"|
        .replaceAll( "\n", "\\\\n" )  // replaceBy: |\\n|
        .replaceAll( "\r", "\\\\r" ); // replaceBy: |\\r|
    }

    return "\"" + content + "\"";
  }
}
