
package pt.webdetails.cdf.dd.model.meta;

import pt.webdetails.cdf.dd.model.core.validation.ValidationException;

/**
 * @author dcleao
 */
public class DataSourceComponentType extends NonVisualComponentType
{
  protected DataSourceComponentType(Builder builder, IPropertyTypeSource propSource)
      throws ValidationException
  {
    super(builder, propSource);
  }
  
  public static class Builder extends NonVisualComponentType.Builder
  {
    public DataSourceComponentType build(IPropertyTypeSource propSource)
            throws ValidationException
    {
      return new DataSourceComponentType(this, propSource);
    }
  }
}
