/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyBag {

  private Hashtable<String, Object> properties;
  protected static final Log logger = LogFactory.getLog( PropertyBag.class );
  private static final String ID_KEY = "id";
  private static final String CLASSES_KEY = "classes";
  private static final String STYLES_KEY = "styles";

  public PropertyBag() {

    properties = new Hashtable<String, Object>();

  }

  public void addId( String id ) {
    if ( id.length() > 0 ) {
      properties.put( ID_KEY, id.replace( ' ', '_' ) );
    }
  }

  public String getId() {
    if ( properties.containsKey( ID_KEY ) ) {
      return properties.get( ID_KEY ).toString();
    } else {
      return null;
    }
  }

  public void addClass( String _class ) {
    if ( _class.length() > 0 ) {
      if ( !properties.containsKey( CLASSES_KEY ) ) {
        properties.put( CLASSES_KEY, new Vector<String>() );
      }
      ( (Vector<String>) properties.get( CLASSES_KEY ) ).add( _class );
    }
  }

  public void addStyle( String style, String value ) {
    if ( value.length() > 0 ) {
      if ( !properties.containsKey( STYLES_KEY ) ) {
        properties.put( STYLES_KEY, new Vector<String>() );
      }
      ( (Vector<String>) properties.get( STYLES_KEY ) ).add( style + ":" + value + ";" );
    }
  }

  public void addColClass( String _class, String v ) {
    if ( v.length() > 0 ) {
      addClass( _class + v );
    }
  }

  public void addColClass( String _class, Boolean v ) {
    if ( v ) {
      addClass( _class );
    }
  }

  public String getPropertiesString() {
    String str = properties.containsKey( ID_KEY ) ? "id='" + (String) properties.get( "id" ) + "' " : "";
    if ( properties.containsKey( CLASSES_KEY ) ) {
      str += " class='";
      Iterator<String> nodeIterator = ( (Vector<String>) properties.get( CLASSES_KEY ) ).iterator();
      while ( nodeIterator.hasNext() ) {
        str += nodeIterator.next() + " ";
      }
      str += "' ";
    }
    if ( properties.containsKey( STYLES_KEY ) ) {
      str += " style='";
      Iterator<String> nodeIterator = ( (Vector<String>) properties.get( STYLES_KEY ) ).iterator();
      while ( nodeIterator.hasNext() ) {
        str += nodeIterator.next();
      }
      str += "' ";
    }
    return str;
  }
}
