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

package pt.webdetails.cdf.dd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
  private static final int INDENT = 2;

  public static JSONObject readJsonFromInputStream( final InputStream input ) throws IOException, JSONException {

    String contents = StringUtils.trim( IOUtils.toString( input, "UTF-8" ) );

    return new JSONObject( contents );
  }

  public static void buildJsonResult( final OutputStream out, final Boolean success, final Object result )
    throws JSONException {

    final JSONObject jsonResult = new JSONObject();

    if ( result != null ) {
      jsonResult.put( "result", result );
    }
    jsonResult.put( "status", success.toString() );

    PrintWriter pw = null;
    try {
      pw = new PrintWriter( out );
      pw.print( jsonResult.toString( INDENT ) );
      pw.flush();
    } finally {
      IOUtils.closeQuietly( pw );
    }
  }

  public static String getJsonResult( boolean success, Object result ) throws JSONException {
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

  public static JXPathContext toJXPathContext( JSONObject json ) throws JSONException {
    Map<String, Object> hashMap = new HashMap<String, Object>();
    Iterator<String> it = json.keys();
    while ( it.hasNext() ) {
      String key = it.next();
      hashMap.put( key, processValue( json.get( key ) ) );
    }
    return JXPathContext.newContext( hashMap );
  }

  private static Object processValue( Object obj ) throws JSONException {
    if ( obj instanceof JSONObject ) {
      return processValueAsJSON( (JSONObject) obj );
    } else if ( obj instanceof JSONArray ) {
      return processValueAsJSONArray( (JSONArray) obj );
    }
    return obj;
  }

  private static Map<String, Object> processValueAsJSON( JSONObject json ) throws JSONException {
    Map<String, Object> hashMap = new HashMap<String, Object>();
    Iterator<String> it = json.keys();
    while ( it.hasNext() ) {
      String key = it.next();
      hashMap.put( key, processValue( json.get( key ) ) );
    }
    return hashMap;
  }

  private static List<Object> processValueAsJSONArray( JSONArray arr ) throws JSONException {
    List<Object> arrayList = new ArrayList<Object>();
    for ( int i = 0; i < arr.length(); i++ ) {
      arrayList.add( processValue( arr.get( i ) ) );
    }
    return arrayList;
  }
}
