/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.model.core.Thing;

/**
 * Writes a thing in a given format into a specified output.
 *
 * @author dcleao
 */
public interface IThingWriter {
  void write( java.lang.Object output, IThingWriteContext context, Thing t )
    throws ThingWriteException;
}
