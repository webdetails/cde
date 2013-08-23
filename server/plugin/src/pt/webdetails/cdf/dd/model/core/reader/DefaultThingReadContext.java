/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

/**
 * @author dcleao
 */
public class DefaultThingReadContext implements IThingReadContext
{
  private final IThingReaderFactory _factory;

  public DefaultThingReadContext(IThingReaderFactory factory)
  {
    if(factory == null) { throw new IllegalArgumentException("factory"); }

    this._factory = factory;
  }

  public final IThingReaderFactory getFactory()
  {
    return this._factory;
  }
}
