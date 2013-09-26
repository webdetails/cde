/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

import pt.webdetails.cdf.dd.model.core.Thing;

/**
 * A two-stage builder factory with unknown context, source, or output
 * @author dcleao
 */
public interface IThingReader
{
  /**
   * Creates and loads a builder object of appropriate type 
   * with information from an implicit data source.
   *
   * If sub-things need to be read,
   * the specified factory can be used to obtain additional readers.
   */
  Thing.Builder read(IThingReadContext context, java.lang.Object source, String sourcePath)
          throws ThingReadException;

  /**
   * Reads an existing appropriately typed builder object with
   * information from an implicit data source.
   *
   * If sub-things need to be read,
   * the specified factory can be used to obtain additional readers.
   */
  void read(Thing.Builder builder, IThingReadContext context, java.lang.Object source, String sourcePath)
          throws ThingReadException;
}
