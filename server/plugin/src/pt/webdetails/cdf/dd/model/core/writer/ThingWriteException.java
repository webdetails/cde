/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer;

/**
 * @author dcleao
 */
public final class ThingWriteException extends Exception
{
  public ThingWriteException(String message, Exception cause)
  {
    super(message, cause);
  }

  public ThingWriteException(Exception cause)
  {
    super(cause);
  }

  public ThingWriteException(String message)
  {
    super(message);
  }
}
