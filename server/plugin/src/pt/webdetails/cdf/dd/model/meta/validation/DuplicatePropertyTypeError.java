
package pt.webdetails.cdf.dd.model.meta.validation;

import pt.webdetails.cdf.dd.model.meta.PropertyType;

/**
 * @author dcleao
 */
public final class DuplicatePropertyTypeError extends PropertyTypeError
{
  public DuplicatePropertyTypeError(PropertyType prop)
  {
    super(getPropName(prop));
  }
  
  @Override
  public String toString()
  {
    return "PropertyType '" + this._propertyName + "' is defined twice.";
  }

  private static String getPropName(PropertyType prop)
  {
    if(prop == null) { throw new IllegalArgumentException("prop"); }
    return prop.getName();
  }
}
