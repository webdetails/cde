/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.PrimitiveComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public class PrimitiveComponent<TM extends PrimitiveComponentType> extends GenericComponent<TM>
{
  protected PrimitiveComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  /**
   * Class to create and modify PrimitiveComponent instances.
   */
  public static class Builder extends GenericComponent.Builder
  {
    @Override
    public PrimitiveComponent build(MetaModel metaModel) throws ValidationException
    {
      if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
      
      return new PrimitiveComponent(this, metaModel);
    }
  }
}
