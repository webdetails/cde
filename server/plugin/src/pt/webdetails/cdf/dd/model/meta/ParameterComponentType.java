
package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public final class ParameterComponentType extends NonVisualComponentType
{
  protected ParameterComponentType(Builder builder, IPropertyTypeSource propSource)
      throws ValidationException
  {
    super(builder, propSource);
  }

  public static final class Builder extends NonVisualComponentType.Builder
  {
    @Override
    public ParameterComponentType build(IPropertyTypeSource propSource) 
            throws ValidationException
    {
      if(propSource == null) { throw new IllegalArgumentException("propSource"); }

      return new ParameterComponentType(this, propSource);
    }
  }
}
