/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta.validation;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

/**
 * @author dcleao
 */
public abstract class ComponentTypeError extends ValidationError
{
  protected final String _componentTypeLabel;

  public ComponentTypeError(String componentTypeLabel) throws IllegalArgumentException
  {
    if(StringUtils.isEmpty(componentTypeLabel)) { throw new IllegalArgumentException("componentTypeLabel"); }

    this._componentTypeLabel = componentTypeLabel;
  }

  public String getComponentLabel()
  {
    return this._componentTypeLabel;
  }

  @Override
  public String toString()
  {
    return "Component type '" + this._componentTypeLabel + "' is invalid.";
  }
}
