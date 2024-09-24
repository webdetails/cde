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

package pt.webdetails.cdf.dd.model.core.validation;

import org.apache.commons.lang.StringUtils;

public abstract class ThingValidationError extends ValidationError {
  protected final String _thingKind;
  protected final String _thingId;

  public ThingValidationError( String thingKind, String thingId ) throws IllegalArgumentException {
    if ( StringUtils.isEmpty( thingKind ) ) {
      throw new IllegalArgumentException( "thingKind" );
    }
    if ( StringUtils.isEmpty( thingId   ) ) {
      throw new IllegalArgumentException( "thingId" );
    }

    this._thingKind = thingKind;
    this._thingId   = thingId;
  }

  public String getThingKind() {
    return this._thingKind;
  }

  public String getThingId() {
    return this._thingId;
  }

  @Override
  public String toString() {
    return "Thing of kind '" + this._thingKind + "' and id '" + this._thingId + "' is invalid.";
  }
}
