
package pt.webdetails.cdf.dd.model.core.writer;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

/**
 * @author dcleao
 */
public interface IThingWriterFactory
{
  /**
   * Obtains an thing writer for a given thing,
   * and a prespecified output format.
   */
  IThingWriter getWriter(Thing t) throws UnsupportedThingException;
}