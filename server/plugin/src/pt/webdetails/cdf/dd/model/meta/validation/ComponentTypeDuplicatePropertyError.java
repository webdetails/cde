
package pt.webdetails.cdf.dd.model.meta.validation;

/**
 * @author dcleao
 */
public final class ComponentTypeDuplicatePropertyError extends ComponentTypeError
{
  private final String _propertyName;

  public ComponentTypeDuplicatePropertyError(String componentTypeLabel, String propertyName)
  {
    super(componentTypeLabel);

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
    return "ComponentType '" + this._componentTypeLabel + "' already has a property named '" + this._propertyName + "'.";
  }
}
