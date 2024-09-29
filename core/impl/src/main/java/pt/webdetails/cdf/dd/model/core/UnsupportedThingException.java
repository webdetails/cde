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


package pt.webdetails.cdf.dd.model.core;

import org.apache.commons.lang.StringUtils;

public final class UnsupportedThingException extends Exception {

  private static final long serialVersionUID = 879684902399171869L;

  private final String _thingKind;
  private final String _thingId;

  public UnsupportedThingException( String thingKind, String thingId ) throws IllegalArgumentException {
    super( createMessage( thingKind, thingId ) );

    this._thingKind = thingKind;
    this._thingId = thingId;
  }

  public String getThingKind() {
    return this._thingKind;
  }

  public String getThingId() {
    return this._thingId;
  }

  public static String createMessage( String thingKind, String thingId ) {
    if ( StringUtils.isEmpty( thingKind ) ) {
      throw new IllegalArgumentException( "thingKind" );
    }
    if ( StringUtils.isEmpty( thingId ) ) {
      throw new IllegalArgumentException( "thingId" );
    }

    return thingKind + " of id '" + thingId + "' is not supported/defined.";
  }
}
