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
