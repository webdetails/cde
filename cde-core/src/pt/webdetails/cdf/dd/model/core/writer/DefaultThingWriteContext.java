/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer;

/**
 * @author dcleao
 */
public class DefaultThingWriteContext implements IThingWriteContext
{
  private final IThingWriterFactory _factory;
  private final boolean _breakOnError;

  public DefaultThingWriteContext(IThingWriterFactory factory, boolean breakOnError)
  {
    if(factory == null) { throw new IllegalArgumentException("factory"); }

    this._factory = factory;
    this._breakOnError = breakOnError;
  }

  public IThingWriterFactory getFactory() {
    return this._factory;
  }

  public boolean getBreakOnError()
  {
    return this._breakOnError;
  }
}