/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.cdw;

import java.util.Iterator;
import net.sf.json.JSONArray;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.render.components.BaseComponent;
import pt.webdetails.cdf.dd.render.components.ComponentManager;

/**
 *
 * @author pdpi
 */
public class CggChart
{

  private static final Log logger = LogFactory.getLog(CggChart.class);
  JXPathContext document;
  Pointer chart;
  String path, chartName;

  public CggChart(Pointer chart)
  {
    this.chart = chart;
    this.document = JXPathContext.newContext(chart.getRootNode());
    this.path = "";
    this.chartName = JXPathContext.newContext(chart.getNode()).getPointer("properties/.[name='name']/value").getValue().toString();
  }

  public void renderToFile()
  {
    StringBuilder chartScript = new StringBuilder();
    renderPreamble(chartScript);

    renderChart(chartScript);
    renderDatasource(chartScript);

    chartScript.append("renderCcccFromComponent(render_" + this.chartName + ", data);\n");
    chartScript.append("output = document.body.innerHTML.match('<svg.*/svg>')[0];\n");

    writeFile(chartScript);
  }

  private void renderDatasource(StringBuilder chartScript)
  {
    String datasourceName = JXPathContext.newContext(chart.getNode()).getPointer("properties/.[name='dataSource']/value").getNode().toString();
    JXPathContext datasourceContext = JXPathContext.newContext(document.getPointer("/datasources/rows[properties[name='name' and value='" + datasourceName + "']]").getNode());

    renderDatasourcePreamble(chartScript, datasourceContext);
    renderParameters(chartScript, JSONArray.fromObject(datasourceContext.getValue("properties/.[name='parameters']/value", String.class)));

    chartScript.append("var data = JSON.parse(String(datasource.execute()));\n");
  }

  /**
   * Sets up the includes, place holders and any other niceties needed for the chart
   */
  private void renderPreamble(StringBuilder chartScript)
  {
    chartScript.append("lib('protovis-bundle.js');\n");
    chartScript.append("lib('ccc-utils.js');\n\n");

    chartScript.append("elem = document.createElement('div');\n"
            + "elem.setAttribute('id','canvas');\n"
            + "document.body.appendChild(elem);\n\n");
  }

  private void renderChart(StringBuilder chartScript)
  {
    ComponentManager engine = ComponentManager.getInstance();
    JXPathContext context = document.getRelativeContext(chart);
    BaseComponent renderer = engine.getRenderer(context);
    renderer.setNode(context);
    chartScript.append(renderer.render(context));


  }

  private void writeFile(StringBuilder chartScript)
  {
    try
    {
      ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession());
      solutionRepository.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), path, this.chartName + ".js", chartScript.toString().getBytes("UTF-8"), true);
    }
    catch (Exception e)
    {
      logger.error("failed to write script file for " + chartName + ": " + e.getCause().getMessage());
    }
  }

  private void renderDatasourcePreamble(StringBuilder chartScript, JXPathContext context)
  {
    String dataAccessId = (String) context.getValue("properties/.[name='name']/value", String.class);

    chartScript.append("var datasource = datasourceFactory.createDatasource('cda');\n");
    chartScript.append("datasource.setDefinitionFile(render_" + this.chartName + ".chartDefinition.path);\n");
    chartScript.append("datasource.setDataAccessId('" + dataAccessId + "');\n\n");
  }

  private void renderParameters(StringBuilder chartScript, JSONArray params)
  {
    Iterator<JSONArray> it = params.iterator();
    while (it.hasNext())
    {
      JSONArray param = it.next();
      String paramName = param.get(0).toString();
      chartScript.append("var param" + paramName + " = params.get('" + paramName + "') || ''\n");
      chartScript.append("datasource.setParameter('" + paramName + "', param" + paramName + ");\n");
    }
  }

  public void setPath(String path)
  {
    this.path = path;
  }
}
