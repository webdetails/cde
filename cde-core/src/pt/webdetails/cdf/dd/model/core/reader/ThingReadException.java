/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.reader;

import pt.webdetails.cdf.dd.util.Utils;

/**
 * @author dcleao
 */
public final class ThingReadException extends Exception
{

	private static final long serialVersionUID = 8809845865057834418L;

public ThingReadException(String message, Exception cause)
  {
    super(Utils.composeErrorMessage(message, cause), cause);
  }

  public ThingReadException(Exception cause)
  {
    super(Utils.composeErrorMessage(null, cause), cause);
  }

  public ThingReadException(String message)
  {
    super(message);
  }
}
