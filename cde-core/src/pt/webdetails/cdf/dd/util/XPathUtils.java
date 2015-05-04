/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XPathUtils {

  protected static final Log logger = LogFactory.getLog( XPathUtils.class );

  public static String getStringValue( JXPathContext node, String xPath ) {

    String value = "";
    try {
      Object val = node.getValue( xPath );
      value = val != null ? val.toString() : value;
    } catch ( JXPathException e ) {
      // Property not found; Should not be an error
    }

    return value;
  }

  public static boolean exists( JXPathContext node, String xPath ) {

    try {
      return ( (String) node.getValue( xPath ) ).length() > 0 ? true : false;

    } catch ( JXPathException e ) {
      return false;
    }
  }

  public static boolean getBooleanValue( JXPathContext node, String xPath ) {

    boolean value = false;
    try {
      value = ( (Boolean) node.getValue( "boolean(" + xPath + " = \"true\")" ) ).booleanValue();
    } catch ( JXPathException e ) {
      logger.error( e.getMessage() );
    }

    return value;
  }

  public static String getArrayValue( JXPathContext node, String xPath ) {

    String value = "";
    try {
      value = JSONSerializer.toJSON( node.selectNodes( xPath ) ).toString();
    } catch ( JSONException e ) {
      logger.error( e.getMessage() );
    } catch ( JXPathException e ) {
      logger.error( e.getMessage() );
    }

    return value;
  }

  public static String getValue( JXPathContext node, String xPath ) {

    String value = "";
    try {
      value = node.getValue( xPath ).toString();
    } catch ( JXPathException e ) {
      // Property not found; Should not be an error
    }

    return value;
  }
}
