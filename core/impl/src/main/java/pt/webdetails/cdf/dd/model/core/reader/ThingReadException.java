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
