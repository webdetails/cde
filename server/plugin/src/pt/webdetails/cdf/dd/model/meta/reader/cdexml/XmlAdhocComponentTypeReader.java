
package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import org.dom4j.Element;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.meta.ComponentType;

/**
 * @author dcleao
 */
public final class XmlAdhocComponentTypeReader<TB extends ComponentType.Builder> 
    extends XmlComponentTypeReader
{
  private final Class<TB> _class;
  
  public XmlAdhocComponentTypeReader(Class<TB> pclass)
  {
    if(pclass == null) { throw new IllegalArgumentException("pclass"); }
    _class = pclass;
  }
  
  @Override
  public TB read(IThingReadContext context, java.lang.Object source, String sourcePath)
            throws ThingReadException
  {
    TB builder = createInstance();
    this.read(builder, (XmlPluginModelReadContext)context, (Element)source, sourcePath);
    return builder;
  }
  
  private TB createInstance() throws ThingReadException
  {
    try
    {
      return _class.newInstance();
    }
    catch (InstantiationException ex)
    {
      throw new ThingReadException(ex);
    }
    catch (IllegalAccessException ex)
    {
      throw new ThingReadException(ex);
    }
  }
}
