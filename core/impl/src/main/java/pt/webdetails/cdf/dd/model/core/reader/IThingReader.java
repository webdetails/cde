/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

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
