
package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.NonVisualComponent;

/**
 * @author dcleao
 */
public abstract class NonVisualComponentType extends ComponentType
{
  protected NonVisualComponentType(Builder builder, IPropertyTypeSource propSource)
      throws ValidationException
  {
    super(builder, propSource);
  }
  
  public static abstract class Builder extends ComponentType.Builder
  {
    @Override
    public  abstract NonVisualComponentType build(IPropertyTypeSource propSource)
        throws ValidationException;
  }
}
