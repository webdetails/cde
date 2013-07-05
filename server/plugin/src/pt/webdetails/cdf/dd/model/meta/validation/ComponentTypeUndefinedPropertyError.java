
package pt.webdetails.cdf.dd.model.meta.validation;

/**
 * @author dcleao
 */
public final class ComponentTypeUndefinedPropertyError extends ComponentTypeError
{
  private final String _propertyName;

  public ComponentTypeUndefinedPropertyError(String componentTypeLabel, String propertyName)
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
    return "ComponentType '" + this._componentTypeLabel + "' refers an undefined property: '" + this._propertyName + "'.";
  }
}
