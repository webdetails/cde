
package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.CodeComponent;

/**
 * @author dcleao
 */
public final class CodeComponentType extends NonVisualComponentType
{
  protected CodeComponentType(Builder builder, IPropertyTypeSource propSource)
      throws ValidationException
  {
    super(builder, propSource);
  }
  
  public static final class Builder extends NonVisualComponentType.Builder
  {
    @Override
    public CodeComponentType build(IPropertyTypeSource propSource) 
            throws ValidationException
    {
      if(propSource == null) { throw new IllegalArgumentException("propSource"); }

      return new CodeComponentType(this, propSource);
    }
  }
}
