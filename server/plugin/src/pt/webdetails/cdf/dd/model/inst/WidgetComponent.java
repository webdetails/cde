
package pt.webdetails.cdf.dd.model.inst;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.meta.WidgetComponentType;
import pt.webdetails.cdf.dd.model.meta.MetaModel;

/**
 * @author dcleao
 */
public final class WidgetComponent<TM extends WidgetComponentType> extends GenericComponent<TM>
{
  private WidgetComponent(Builder builder, MetaModel metaModel) throws ValidationException
  {
    super(builder, metaModel);
  }

  @Override
  public TM getMeta()
  {
    return super.getMeta();
  }

  public String getWcdfPath()
  {
    return this.tryGetAttributeValue("wcdf", null);
  }
  
  /**
   * Class to create and modify WidgetComponent instances.
   */
  public static class Builder extends GenericComponent.Builder
  {
    @Override
    public WidgetComponent build(MetaModel metaModel) throws ValidationException
    {
      if(metaModel == null) { throw new IllegalArgumentException("metaModel"); }
      
      return new WidgetComponent(this, metaModel);
    }
  }
}
