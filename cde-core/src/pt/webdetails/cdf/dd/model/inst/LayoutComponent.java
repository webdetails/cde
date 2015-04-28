/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.model.core.validation.RequiredAttributeError;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.LayoutComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * // TODO: HACK:
 * Temporary implementation that stores all the layout rows
 * of a cdfde file...
 * Until RenderLayout can be transformed to this model, if anytime.
 * @author dcleao
 */
public class LayoutComponent extends VisualComponent<LayoutComponentType>
{
  private final JXPathContext _layoutXP;
  
  private LayoutComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
    
    if(builder._layoutXP == null) 
    {
      throw new ValidationException(new RequiredAttributeError("LayoutXPContext"));
    }
    
    this._layoutXP = builder._layoutXP;
  }

  @Override
  public LayoutComponentType getMeta()
  {
    return super.getMeta();
  }
  
  public JXPathContext getLayoutXPContext()
  {
    return this._layoutXP;
  }
  
  /**
   * Class to create and modify LayoutComponent instances.
   */
  public static final class Builder extends VisualComponent.Builder
  {
    private JXPathContext _layoutXP;
    
    public Builder()
    {
    }
    
    public JXPathContext getLayoutXPContext()
    {
      return this._layoutXP;
    }
    
    public Builder setLayoutXPContext(JXPathContext layoutXP)
    {
      this._layoutXP = layoutXP;
      return this;
    }

    @Override
    public LayoutComponent build(MetaModel metaModel) throws ValidationException
    {
      if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
      
      return new LayoutComponent(this, metaModel);
    }
  }
}
