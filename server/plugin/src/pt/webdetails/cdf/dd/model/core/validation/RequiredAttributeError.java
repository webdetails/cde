
package pt.webdetails.cdf.dd.model.core.validation;

/**
 * @author dcleao
 */
public final class RequiredAttributeError extends ValidationError
{
  protected final String _name;
  
  public RequiredAttributeError(String name)
  {
    if(name == null) { throw new IllegalArgumentException("name"); }

    this._name = name;
  }

  @Override
  public String toString()
  {
    return "Attribute '" + this._name + "' is required.";
  }
}
