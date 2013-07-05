package pt.webdetails.cdf.dd.model.core.writer;

/**
 * @author dcleao
 */
public final class ThingWriteException extends Exception
{
  public ThingWriteException(String message, Exception cause)
  {
    super(message, cause);
  }

  public ThingWriteException(Exception cause)
  {
    super(cause);
  }

  public ThingWriteException(String message)
  {
    super(message);
  }
}
