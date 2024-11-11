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


package pt.webdetails.cdf.dd.model.core.reader;

public class DefaultThingReadContext implements IThingReadContext  {
  private final IThingReaderFactory _factory;

  public DefaultThingReadContext( IThingReaderFactory factory ) {
    if ( factory == null ) {
      throw new IllegalArgumentException( "factory" );
    }

    this._factory = factory;
  }

  public final IThingReaderFactory getFactory() {
    return this._factory;
  }
}
