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

package pt.webdetails.cdf.dd.datasources;

public class InvalidDataSourceProviderException extends Exception {
  private static final long serialVersionUID = 8885274026585468691L;

  public InvalidDataSourceProviderException() { }

  public InvalidDataSourceProviderException( String message ) {
    super( message );
  }

  public InvalidDataSourceProviderException( Throwable cause ) {
    super( cause );
  }

  public InvalidDataSourceProviderException( String message, Throwable cause ) {
    super( message, cause );
  }
}
