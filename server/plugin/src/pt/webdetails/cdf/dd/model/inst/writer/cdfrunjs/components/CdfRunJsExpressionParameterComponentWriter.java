
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;

/**
 * @author Duarte
 */
public class CdfRunJsExpressionParameterComponentWriter extends CdfRunJsParameterComponentWriter
{
  @Override
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp) throws ThingWriteException
  {
    String name  = context.getId(comp);
    String value = comp.tryGetPropertyValue("javaScript", "");
    
    addVar(out, name, value);
    
    maybeAddBookmarkable(out, comp, name);
  }
}
