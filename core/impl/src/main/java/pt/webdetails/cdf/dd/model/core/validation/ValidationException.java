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


package pt.webdetails.cdf.dd.model.core.validation;

/**
 * TODO: implement serialization?
 */
public class ValidationException extends Exception {
  private static final long serialVersionUID = -7782142065075580240L;

  private final ValidationError _error;

  public ValidationException( ValidationError error ) {
    super( getMessage( error ) );

    this._error = error;
  }

  public ValidationError getError() {
    return this._error;
  }

  private static String getMessage( ValidationError error ) {
    if ( error == null ) {
      throw new IllegalArgumentException( "error" );
    }
    return error.toString();
  }
}
