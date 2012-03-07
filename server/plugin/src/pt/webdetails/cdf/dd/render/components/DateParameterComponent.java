/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.components;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.commons.jxpath.JXPathContext;
import pt.webdetails.cdf.dd.util.XPathUtils;

/**
 *
 * @author pedro
 */
public class DateParameterComponent extends ParameterComponent
{

  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

  public DateParameterComponent(JXPathContext context)
  {
    super(context);
  }

  public DateParameterComponent()
  {
    super();
  }

  @Override
  public String getName()
  {
    return "DateParameter";
  }

  @Override
  public String render()
  {
    String name = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']");
    String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='propertyValue']");
    return "var " + name + " = \"" + parseValue(value) + "\";" + newLine;
  }

  @Override
  public String render(JXPathContext context)
  {
    String result;
    boolean bookmarkable = XPathUtils.getBooleanValue(getNode(), "properties/value[../name='bookmarkable']");
    String name = XPathUtils.getStringValue(getNode(), "properties/value[../name='name']");
    String value = XPathUtils.getStringValue(getNode(), "properties/value[../name='propertyDateValue']");
    if (bookmarkable)
    {
      result = "Dashboards.setBookmarkable('" + name + "');" + newLine;
    }
    else
    {
      result = "";
    }
    return "var " + name + " = \"" + parseValue(value) + "\";" + newLine + result;
  }

  private String parseValue(String value)
  {


    if (value.equals("today"))
    {
      Calendar cal = Calendar.getInstance();
      return format.format(cal.getTime());
    }
    else if (value.equals("yesterday"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -1);
      return format.format(cal.getTime());
    }
    else if (value.equals("lastWeek"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, -7);
      return format.format(cal.getTime());
    }
    else if (value.equals("lastMonth"))
    {
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.MONTH, -1);
      return format.format(cal.getTime());
    }
    else if (value.equals("monthStart"))
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DATE, 1);
      return format.format(cal.getTime());
    }
    else if (value.equals("yearStart"))
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, 0);
      cal.set(Calendar.DATE, 1);
      return format.format(cal.getTime());
    }
    else
    {
      return value;
    }

  }
}
