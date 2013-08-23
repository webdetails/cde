/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.core.validation;

/**
 * @author dcleao
 */
public final class RequiredAttributeError extends ValidationError
{
  protected final String _name;
  
  public RequiredAttributeError(String name)
  {
    if(name == null) { throw new IllegalArgumentException("name"); }

    this._name = name;
  }

  @Override
  public String toString()
  {
    return "Attribute '" + this._name + "' is required.";
  }
}
