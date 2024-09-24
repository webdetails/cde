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

package pt.webdetails.cdf.dd.model.core.reader;

import pt.webdetails.cdf.dd.util.Utils;

public final class ThingReadException extends Exception {
  private static final long serialVersionUID = 8809845865057834418L;

  public ThingReadException( String message, Exception cause ) {
    super( Utils.composeErrorMessage( message, cause ), cause );
  }

  public ThingReadException( Exception cause ) {
    super( Utils.composeErrorMessage( null, cause ), cause );
  }

  public ThingReadException( String message ) {
    super( message );
  }
}
