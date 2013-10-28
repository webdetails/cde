/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.model.inst.writer.cggrunjs;
import java.util.Iterator;
import net.sf.json.JSONArray;
import pt.webdetails.cdf.dd.model.core.Thing;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriteContext;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriter;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.core.writer.js.JsWriterAbstract;
import pt.webdetails.cdf.dd.model.inst.DataSourceComponent;
import pt.webdetails.cdf.dd.util.JsonUtils;

/**
 * @author dcleao
 */
public class CggRunJsDataSourceComponentWriter extends JsWriterAbstract implements IThingWriter
{
  public void write(Object output, IThingWriteContext context, Thing t) throws ThingWriteException
  {
    this.write((StringBuilder)output, (CggRunJsComponentWriteContext)context, (DataSourceComponent)t);
  }
  
  public void write(StringBuilder out, CggRunJsComponentWriteContext context, DataSourceComponent comp) throws ThingWriteException
  {
    String jsParamsArray = comp.tryGetPropertyValue("parameters", null);
    if(jsParamsArray != null) 
    {
      renderParameters(out, JSONArray.fromObject(jsParamsArray));
    }
  }

  private void renderParameters(StringBuilder out, JSONArray params)
  {
    if(params.isEmpty()) { return; }
    
    /* ex:
      cgg.initParameter
      ('productLine', 'Classic Cars')
      ('territory'    'EMEA')
      ;
     */
    out.append("cgg.initParameter");
    out.append(NEWLINE);
    
    @SuppressWarnings("unchecked")
    Iterator<JSONArray> it = params.iterator();
    while(it.hasNext())
    {
      JSONArray param = it.next();
      
      String paramName    = param.get(0).toString();
      String defaultValue = param.get(1).toString();
      
      out.append("("); 
      out.append(JsonUtils.toJsString(paramName));
      out.append(", ");
      out.append(JsonUtils.toJsString(defaultValue));
      out.append(")");
      out.append(NEWLINE);
    }
    
    out.append(";");
  }
}
