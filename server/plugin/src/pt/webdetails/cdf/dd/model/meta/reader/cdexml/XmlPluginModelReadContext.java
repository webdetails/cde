
package pt.webdetails.cdf.dd.model.meta.reader.cdexml;

import pt.webdetails.cdf.dd.model.core.reader.DefaultThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReadContext;
import pt.webdetails.cdf.dd.model.core.reader.IThingReaderFactory;

/**
 * @author dcleao
 */
public class XmlPluginModelReadContext extends DefaultThingReadContext
{
  private final String _basePath;
  
  public XmlPluginModelReadContext(IThingReadContext parent, String basePath)
  {
    super(getParentFactory(parent));

    this._basePath = basePath;
  }

  public String getBasePath()
  {
    return this._basePath;
  }

  private static IThingReaderFactory getParentFactory(IThingReadContext parent)
  {
    if(parent == null) { throw new IllegalArgumentException("parent"); }

    return parent.getFactory();
  }
}

