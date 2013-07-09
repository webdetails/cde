/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.components;

import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.ParameterComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CdfRunJsParameterComponentWriter extends JsWriterAbstract implements IThingWriter
{
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((StringBuilder)output,(CdfRunJsDashboardWriteContext) context, (ParameterComponent)t);
  }
  
  public void write(StringBuilder out, CdfRunJsDashboardWriteContext context, ParameterComponent comp) throws ThingWriteException
  {
    String name = context.getId(comp);
    
    addVar(out, name, JsonUtils.toJsString(comp.tryGetPropertyValue("propertyValue", "")));
    
    maybeAddBookmarkable(out, comp, name);
    
    out.append("Dashboards.setParameterViewMode(");
    out.append(JsonUtils.toJsString(name));
    out.append(", ");
    out.append(JsonUtils.toJsString(comp.tryGetPropertyValue("parameterViewRole", "unused")));
    out.append(");");
    out.append(NEWLINE);
  }
  
  protected static void maybeAddBookmarkable(StringBuilder out, ParameterComponent comp, String name)
  {
    String bookmarkabelText = comp.tryGetPropertyValue("bookmarkable", null);
    if ("true".equalsIgnoreCase(bookmarkabelText))
    {
      addBookmarkable(out, name);
    }
  }
  
  protected static void addBookmarkable(StringBuilder out, String name)
  {
    out.append("Dashboards.setBookmarkable(");
    out.append(JsonUtils.toJsString(name));
    out.append(");");
    out.append(NEWLINE);
  }
}
