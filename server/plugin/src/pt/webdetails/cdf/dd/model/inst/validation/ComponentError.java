
package pt.webdetails.cdf.dd.model.inst.validation;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.validation.ValidationError;

/**
 * @author dcleao
 */
public abstract class ComponentError extends ValidationError
{
  protected final String _componentId;
  protected final String _componentTypeLabel;

  public ComponentError(String componentId, String componentTypeLabel) throws IllegalArgumentException
  {
    if(StringUtils.isEmpty(componentTypeLabel)) { throw new IllegalArgumentException("componentTypeLabel"); }

    this._componentId = StringUtils.defaultIfEmpty(componentId, "???");
    this._componentTypeLabel = componentTypeLabel;
  }

  public String getComponentId()
  {
    return this._componentId;
  }

  public String getComponentTypeLabel()
  {
    return this._componentTypeLabel;
  }

  @Override
  public String toString()
  {
    return "Component of id '" + this._componentId + " and type '" + this._componentTypeLabel + "' is invalid.";
  }
}
