
package pt.webdetails.cdf.dd.model.core.reader;

/**
 * @author dcleao
 */
public class DefaultThingReadContext implements IThingReadContext
{
  private final IThingReaderFactory _factory;

  public DefaultThingReadContext(IThingReaderFactory factory)
  {
    if(factory == null) { throw new IllegalArgumentException("factory"); }

    this._factory = factory;
  }

  public final IThingReaderFactory getFactory()
  {
    return this._factory;
  }
}
