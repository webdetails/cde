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

package pt.webdetails.cdf.dd.model.core.writer;

public class DefaultThingWriteContext implements IThingWriteContext {
  private final IThingWriterFactory _factory;
  private final boolean _breakOnError;

  public DefaultThingWriteContext( IThingWriterFactory factory, boolean breakOnError ) {
    if ( factory == null ) {
      throw new IllegalArgumentException( "factory" );
    }

    this._factory = factory;
    this._breakOnError = breakOnError;
  }

  public IThingWriterFactory getFactory() {
    return this._factory;
  }

  public boolean getBreakOnError() {
    return this._breakOnError;
  }
}
