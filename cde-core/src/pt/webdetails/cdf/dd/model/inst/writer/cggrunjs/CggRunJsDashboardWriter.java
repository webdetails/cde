/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.UnsupportedThingException;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cpf.repository.IRepositoryAccess;

/**
 * @author dcleao
 */
public class CggRunJsDashboardWriter implements IThingWriter
{
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((IRepositoryAccess)output, (CggRunJsDashboardWriteContext)context, (Dashboard)t);
  }
  
  public void write(IRepositoryAccess repository, CggRunJsDashboardWriteContext context, Dashboard dash) throws ThingWriteException
  {
    assert context.getDashboard() == dash;
    
    IThingWriterFactory factory = context.getFactory();
    Iterable<Component> comps = dash.getRegulars();
    for(Component comp : comps)
    {
      if(StringUtils.isNotEmpty(comp.getName()) && 
         (comp instanceof GenericComponent) && 
         !(comp instanceof WidgetComponent))
      {
        GenericComponent genComp = (GenericComponent)comp;
        if(genComp.getMeta().tryGetAttributeValue("cdwSupport", "false").equalsIgnoreCase("true") &&
           genComp.tryGetAttributeValue("cdwRender", "false").equalsIgnoreCase("true"))
        {
          IThingWriter writer;
          try
          {
            writer = factory.getWriter(genComp);
          }
          catch (UnsupportedThingException ex)
          {
            throw new ThingWriteException(ex);
          }
          
          writer.write(repository, context, comp);
        }
      }
    }
  }
}
