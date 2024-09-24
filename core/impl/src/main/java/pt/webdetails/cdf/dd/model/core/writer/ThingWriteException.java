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

package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.util.Utils;

public final class ThingWriteException extends Exception {
  private static final long serialVersionUID = -4267343314404524599L;

  public ThingWriteException( String message, Exception cause ) {
    super( Utils.composeErrorMessage( message, cause ), cause );
  }

  public ThingWriteException( Exception cause ) {
    super( Utils.composeErrorMessage( null, cause ), cause );
  }

  public ThingWriteException( String message ) {
    super( message );
  }
}
