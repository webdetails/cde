/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

/**
 * @author dcleao
 */
public interface IThingWriterFactory
{
  /**
   * Obtains an thing writer for a given thing,
   * and a prespecified output format.
   */
  IThingWriter getWriter(Thing t) throws UnsupportedThingException;
}