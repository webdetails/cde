/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
