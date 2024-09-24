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

import pt.webdetails.cdf.dd.model.core.Thing;

/**
 * Writes a thing in a given format into a specified output.
 */
public interface IThingWriter {
  void write( java.lang.Object output, IThingWriteContext context, Thing t )
    throws ThingWriteException;
}
