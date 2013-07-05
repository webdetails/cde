
package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author Duarte
 */
public class CdfRunJsDateParameterComponentWriter extends CdfRunJsParameterComponentWriter
{
  private static final SimpleDateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
  
  @Override
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp) throws ThingWriteException
  {
    String name  = context.getId(comp);
    
    String value = resolveDateValue(comp.tryGetPropertyValue("propertyDateValue", ""));
    
    addVar(out, name, JsonUtils.toJsString(value));
    
    maybeAddBookmarkable(out, comp, name);
  }

  private String resolveDateValue(String value)
  {
    if (value.equals("today"))
    {
      Calendar cal = Calendar.getInstance();
      return _format.format(cal.getTime());
    }

    if (value.equals("yesterday"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      return _format.format(cal.getTime());
    }

    if (value.equals("lastWeek"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      return _format.format(cal.getTime());
    }

    if (value.equals("lastMonth"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      return _format.format(cal.getTime());
    }

    if (value.equals("monthStart"))
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DATE, 1);
      return _format.format(cal.getTime());
    }

    if (value.equals("yearStart"))
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, 0);
      cal.set(Calendar.DATE, 1);
      return _format.format(cal.getTime());
    }
    
    return value;
  }
}
