/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

/**
 * @author dcleao
 */
public final class ThingReadException extends Exception
{
  public ThingReadException(String message, Exception cause)
  {
    super(message, cause);
  }

  public ThingReadException(Exception cause)
  {
    super(cause);
  }

  public ThingReadException(String message)
  {
    super(message);
  }
}
