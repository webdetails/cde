/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.ParameterComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class ParameterComponent extends NonVisualComponent<ParameterComponentType>
{
  protected ParameterComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public ParameterComponentType getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify ParameterComponent instances.
   */
  public static class Builder extends NonVisualComponent.Builder
  {
    @Override
    public ParameterComponent build(MetaModel metaModel) throws ValidationException
    {
      return new ParameterComponent(this, metaModel);
    }
  }
}
