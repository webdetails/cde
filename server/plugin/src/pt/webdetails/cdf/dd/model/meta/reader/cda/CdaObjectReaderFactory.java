
package pt.webdetails.cdf.dd.model.meta.reader.cda;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.reader.IThingReader;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;

/**
 * @author dcleao
 */
public class CdaObjectReaderFactory implements IThingReaderFactory
{
  public IThingReader getReader(String kind, String className, String name)
          throws UnsupportedThingException
  {
    if(KnownThingKind.MetaModel.equals(kind))
    {
      return new CdaModelReader();
    }
    
    throw new UnsupportedThingException(kind, className);
  }
}
