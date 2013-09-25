/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.util.Utils;

/**
 * @author dcleao
 */
public final class ThingWriteException extends Exception
{

	private static final long serialVersionUID = -4267343314404524599L;

public ThingWriteException(String message, Exception cause)
  {
    super(Utils.composeErrorMessage(message, cause), cause);
  }

  public ThingWriteException(Exception cause)
  {
    super(Utils.composeErrorMessage(null, cause), cause);
  }

  public ThingWriteException(String message)
  {
    super(message);
  }
}
