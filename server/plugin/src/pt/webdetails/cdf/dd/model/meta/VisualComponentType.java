/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public abstract class VisualComponentType extends ComponentType
{
  protected VisualComponentType(Builder builder, IPropertyTypeSource propSource)
      throws ValidationException
  {
    super(builder, propSource);
  }
  
  public static abstract class Builder extends ComponentType.Builder
  {
    @Override
    public  abstract VisualComponentType build(IPropertyTypeSource propSource)
        throws ValidationException;
  }
}
