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

import pt.webdetails.cdf.dd.model.core.Thing;

/**
 * A two-stage builder factory with unknown context, source, or output
 */
public interface IThingReader {
  /**
   * Creates and loads a builder object of appropriate type 
   * with information from an implicit data source.
   *
   * If sub-things need to be read,
   * the specified factory can be used to obtain additional readers.
   */
  Thing.Builder read( IThingReadContext context, java.lang.Object source, String sourcePath )
      throws ThingReadException;

  /**
   * Reads an existing appropriately typed builder object with
   * information from an implicit data source.
   *
   * If sub-things need to be read,
   * the specified factory can be used to obtain additional readers.
   */
  void read( Thing.Builder builder, IThingReadContext context, java.lang.Object source, String sourcePath )
      throws ThingReadException;
}
