
package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.model.core.Thing;

/**
 * Writes a thing in a given format into a specified output.
 * 
 * @author dcleao
 */
public interface IThingWriter
{
  void write(java.lang.Object output, IThingWriteContext context, Thing t)
          throws ThingWriteException;
}
