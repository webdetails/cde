
package pt.webdetails.cdf.dd.model.core.validation;

import org.apache.commons.lang.StringUtils;

/**
 * @author dcleao
 */
public abstract class ThingValidationError extends ValidationError
{
  protected final String _thingKind;
  protected final String _thingId;

  public ThingValidationError(String thingKind, String thingId) throws IllegalArgumentException
  {
    if(StringUtils.isEmpty(thingKind)) { throw new IllegalArgumentException("thingKind"); }
    if(StringUtils.isEmpty(thingId  )) { throw new IllegalArgumentException("thingId"); }

    this._thingKind = thingKind;
    this._thingId   = thingId;
  }

  public String getThingKind()
  {
    return this._thingKind;
  }

  public String getThingId()
  {
    return this._thingId;
  }

  @Override
  public String toString()
  {
    return "Thing of kind '" + this._thingKind + "' and id '" + this._thingId + "' is invalid.";
  }
}
