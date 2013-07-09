/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.validation;

/**
 * @author dcleao
 *
 * TODO: implement serialization?
 */
public class ValidationException extends Exception
{
  private final ValidationError _error;

  public ValidationException(ValidationError error)
  {
    super(getMessage(error));

    this._error = error;
  }

  public ValidationError getError()
  {
    return this._error;
  }

  private static String getMessage(ValidationError error)
  {
    if(error == null) { throw new IllegalArgumentException("error"); }
    return error.toString();
  }
}
