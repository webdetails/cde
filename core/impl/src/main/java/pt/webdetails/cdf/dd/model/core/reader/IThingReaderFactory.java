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

import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

public interface IThingReaderFactory {
  /**
   * Obtains an object type reader for a given object type,
   * given its kind, class and id.
   */
  IThingReader getReader( String kind, String className, String id ) throws UnsupportedThingException;
}
