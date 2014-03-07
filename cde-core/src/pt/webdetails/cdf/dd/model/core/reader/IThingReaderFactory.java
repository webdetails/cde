/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

/**
 * @author dcleao
 */
public interface IThingReaderFactory
{
  /**
   * Obtains an object type reader for a given object type,
   * given its kind, class and id.
   */
  IThingReader getReader(String kind, String className, String id) throws UnsupportedThingException;
}
