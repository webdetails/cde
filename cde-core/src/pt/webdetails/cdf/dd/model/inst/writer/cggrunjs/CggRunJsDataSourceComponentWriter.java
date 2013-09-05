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
import pt.webdetails.cdf.dd.model.inst.GenericComponent;
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
    String dataAccessId = comp.getName();

    GenericComponent chartComp = context.getChartComponent();
    out.append("var datasource = datasourceFactory.createDatasource('cda');");
    out.append(NEWLINE);
    
    String jsPathToDataSourcePath = "render_" + chartComp.getName() + ".chartDefinition.path";
    out.append("datasource.setDefinitionFile(");
    out.append(jsPathToDataSourcePath);
    out.append(");");
    out.append(NEWLINE);
    
    out.append("datasource.setDataAccessId(");
    out.append(JsonUtils.toJsString(dataAccessId));
    out.append(");");
    out.append(NEWLINE);
    
    String jsParamsArray = comp.tryGetPropertyValue("parameters", null);
    if(jsParamsArray != null) 
    {
      renderParameters(out, JSONArray.fromObject(jsParamsArray));
    }
    
    out.append("var data = eval('new Object(' + String(datasource.execute()) + ');');");
    out.append(NEWLINE);
  }

  private void renderParameters(StringBuilder out, JSONArray params)
  {
    @SuppressWarnings("unchecked")
    Iterator<JSONArray> it = params.iterator();
    while (it.hasNext())
    {
      JSONArray param = it.next();
      
      String paramName    = param.get(0).toString();
      String defaultValue = param.get(1).toString();
      
      String paramVarName = "param" + paramName;
      String jsParamName = JsonUtils.toJsString(paramName);
      
      out.append("var "); 
      out.append(paramVarName);
      out.append(" = params.get(");
      out.append(jsParamName);
      out.append(");");
      out.append(NEWLINE);
      
      out.append(paramVarName);
      out.append(" = (");
      out.append(paramVarName);
      out.append(" !== null && ");
      out.append(paramVarName);
      out.append(" !== '') ? ");
      out.append(paramVarName);
      out.append(" : ");
      out.append(JsonUtils.toJsString(defaultValue));
      out.append(";");
      out.append(NEWLINE);
      
      out.append("datasource.setParameter(");
      out.append(jsParamName);
      out.append(", ");
      out.append(paramVarName);
      out.append(");");
      out.append(NEWLINE);
    }
  }
}
