
package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import pt.webdetails.cdf.dd.model.core.KnownThingKind;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;

/**
 * @author dcleao
 */
public class CggRunJsThingWriterFactory implements IThingWriterFactory
{
  public IThingWriter getWriter(Thing t) throws UnsupportedThingException
  {
    if(t == null) { throw new IllegalArgumentException("t"); }

    String kind = t.getKind();
    
    if(KnownThingKind.Component.equals(kind))
    {
      if((t instanceof GenericComponent) && !(t instanceof WidgetComponent))
      {
        GenericComponent comp = (GenericComponent)t;
        if(comp.getMeta().tryGetAttributeValue("cdwSupport", "false").equalsIgnoreCase("true"))
        {
          return new CggRunJsGenericComponentWriter();
        }
      }
      else if(t instanceof DataSourceComponent)
      {
        return new CggRunJsDataSourceComponentWriter();
      }
    }
    else if(KnownThingKind.Dashboard.equals(kind))
    {
      return new CggRunJsDashboardWriter();
    }
    
    throw new UnsupportedThingException(kind, t.getId());
  }
}
