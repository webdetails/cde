package pt.webdetails.cdf.dd.model.core;

import org.apache.commons.lang.StringUtils;

/**
 * @author dcleao
 */
public final class UnsupportedThingException extends Exception
{
  private final String _thingKind;
  private final String _thingId;

  public UnsupportedThingException(String thingKind, String thingId) throws IllegalArgumentException
  {
    super(createMessage(thingKind, thingId));
    
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

  public static String createMessage(String thingKind, String thingId)
  {
    if(StringUtils.isEmpty(thingKind)) { throw new IllegalArgumentException("thingKind"); }
    if(StringUtils.isEmpty(thingId  )) { throw new IllegalArgumentException("thingId"); }

    return "Thing of kind '" + thingKind + "' and id '" + thingId + "' is not supported.";
  }
}
