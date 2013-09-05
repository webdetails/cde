/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.MetaModel;
import pt.webdetails.cdf.dd.model.meta.VisualComponentType;

/**
 * @author dcleao
 */
public class VisualComponent<TM extends VisualComponentType> extends Component<TM>
{
  public static final String DEF_IDPREFIX = "render";
  
  private final String _priority;

  protected VisualComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);

    PropertyBinding bind = this.tryGetPropertyBinding("priority");
    this._priority = bind != null ? StringUtils.defaultIfEmpty(bind.getValue(), "") : "";
  }

  public final String getPriority()
  {
    return this._priority;
  }
  
  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  @Override
  protected String initGetDefaultIdPrefix()
  {
    return DEF_IDPREFIX;
  }

  /**
   * Class to create and modify VisualComponent instances.
   */
  public static class Builder extends Component.Builder
  {
    @Override
    public VisualComponent build(MetaModel metaModel) throws ValidationException
    {
      if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
      
      return new VisualComponent(this, metaModel);
    }
  }
}
