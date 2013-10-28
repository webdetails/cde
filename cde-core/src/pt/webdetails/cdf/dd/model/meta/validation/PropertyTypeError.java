
package pt.webdetails.cdf.dd.model.meta.validation;

import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

/**
 * @author dcleao
 */
public abstract class PropertyTypeError extends ValidationError
{
  protected final String _propertyName;

  public PropertyTypeError(String propertyName) throws IllegalArgumentException
  {
    if(propertyName == null) { throw new IllegalArgumentException("propertyName"); }

    this._propertyName = propertyName;
  }

  public String getPropertyName()
  {
    return this._propertyName;
  }

  @Override
  public String toString()
  {
    return "PropertyType '" + this._propertyName + "' is invalid.";
  }
}
