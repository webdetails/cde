
package pt.webdetails.cdf.dd.model.core.reader;

import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;

/**
 * @author dcleao
 */
public interface IThingReaderFactory
{
  /**
   * Obtains an object type reader for a given object type,
   * given its kind, class and id.
   */
  IThingReader getReader(String kind, String className, String id) throws UnsupportedThingException;
}
