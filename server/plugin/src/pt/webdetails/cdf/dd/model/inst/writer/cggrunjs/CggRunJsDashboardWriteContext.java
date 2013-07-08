
package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;

import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.model.core.writer.DefaultThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;

/**
 * @author dcleao
 */
public class CggRunJsDashboardWriteContext extends DefaultThingWriteContext
{
  private final Dashboard _dash;
  private final IPentahoSession _userSession;
  
  public CggRunJsDashboardWriteContext(
          IThingWriterFactory factory, 
          Dashboard dash,
          IPentahoSession userSession)
  {
    super(factory, true);
    
    if(dash == null) { throw new IllegalArgumentException("dash"); }
    if(userSession == null) { throw new IllegalArgumentException("userSession"); }
    this._dash  = dash;
    this._userSession = userSession;
  }
  
  public Dashboard getDashboard()
  {
    return this._dash;
  }
  
  public IPentahoSession getUserSession()
  {
    return this._userSession;
  }
}
