/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

/**
 * Allows passing context information during the reading process.
 * 
 * @author dcleao
 */
public interface IThingReadContext
{
  IThingReaderFactory getFactory();
}
