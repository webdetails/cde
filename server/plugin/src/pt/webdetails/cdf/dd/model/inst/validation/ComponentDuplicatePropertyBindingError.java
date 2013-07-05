
package pt.webdetails.cdf.dd.model.inst.validation;

import org.apache.commons.lang.StringUtils;

/**
 * @author dcleao
 */
public final class ComponentDuplicatePropertyBindingError extends ComponentError
{
  private final String _propertyAlias;

  public ComponentDuplicatePropertyBindingError(
          String propertyAlias, 
          String componentId, 
          String componentTypeLabel)
          throws IllegalArgumentException
  {
    super(componentId, componentTypeLabel);
    
    if(StringUtils.isEmpty(propertyAlias)) { throw new IllegalArgumentException("propertyAlias"); }

    this._propertyAlias = propertyAlias;
  }

  public String getPropertyAlias()
  {
    return this._propertyAlias;
  }

  @Override
  public String toString()
  {
    return "Component of id '" + this._componentId + " and type '" + this._componentTypeLabel + "' has a duplicate property binding for name/alias '" + this._propertyAlias + "'.";
  }
}
