
package pt.webdetails.cdf.dd.model.core.writer;

/**
 * @author dcleao
 */
public class DefaultThingWriteContext implements IThingWriteContext
{
  private final IThingWriterFactory _factory;
  private final boolean _breakOnError;

  public DefaultThingWriteContext(IThingWriterFactory factory, boolean breakOnError)
  {
    if(factory == null) { throw new IllegalArgumentException("factory"); }

    this._factory = factory;
    this._breakOnError = breakOnError;
  }

  public final IThingWriterFactory getFactory()
  {
    return this._factory;
  }

  public boolean getBreakOnError()
  {
    return this._breakOnError;
  }
}