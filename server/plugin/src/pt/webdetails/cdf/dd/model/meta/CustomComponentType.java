
package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;
import pt.webdetails.cdf.dd.model.inst.CustomComponent;

/**
 * @author dcleao
 */
public final class CustomComponentType extends GenericComponentType
{
  private CustomComponentType(Builder builder, IPropertyTypeSource propSource) throws ValidationException
  {
    super(builder, propSource);
  }

  /**
   * Class to create and modify ComponentType instances.
   */
  public static final class Builder extends GenericComponentType.Builder
  {
    @Override
    public CustomComponentType build(IPropertyTypeSource propSource) throws ValidationException
    {
      if(propSource == null) { throw new IllegalArgumentException("propSource"); }

      return new CustomComponentType(this, propSource);
    }
  }
}
