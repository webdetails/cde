
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
