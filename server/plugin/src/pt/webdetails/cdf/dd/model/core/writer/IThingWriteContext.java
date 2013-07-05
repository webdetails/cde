
package pt.webdetails.cdf.dd.model.core.writer;

/**
 * Allows passing context information during the writing process.
 * 
 * @author dcleao
 */
public interface IThingWriteContext
{
  boolean getBreakOnError();

  IThingWriterFactory getFactory();
}
