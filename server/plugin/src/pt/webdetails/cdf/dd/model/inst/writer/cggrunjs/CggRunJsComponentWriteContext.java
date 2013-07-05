
package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;

/**
 * @author dcleao
 */
public class CggRunJsComponentWriteContext extends CggRunJsDashboardWriteContext
{
  private final GenericComponent _comp;
  
  public CggRunJsComponentWriteContext(
          IThingWriterFactory factory, 
          Dashboard dash,
          GenericComponent comp,
          IPentahoSession userSession)
  {
    super(factory, dash, userSession);
    
    if(comp == null) { throw new IllegalArgumentException("comp"); }
    this._comp  = comp;
  }
  
  public GenericComponent getChartComponent()
  {
    return this._comp;
  }
}
