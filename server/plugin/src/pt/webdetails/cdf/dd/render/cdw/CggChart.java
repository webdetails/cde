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
  String filename, chartName;

  public CggChart(Pointer chart)
  {
    this.chart = chart;
    this.document = JXPathContext.newContext(chart.getRootNode());
    this.filename = "";
    this.chartName = JXPathContext.newContext(chart.getNode()).getPointer("properties/.[name='name']/value").getValue().toString();
  }

  public void render()
  {
    StringBuilder dataSource = new StringBuilder();
    renderPreamble(dataSource);

    renderChart(dataSource);
    renderDatasource(dataSource);

    dataSource.append("renderCcccFromComponent(render_" + this.chartName + ", data);\n");
    dataSource.append("output = document.body.innerHTML.match('<svg.*/svg>')[0];\n");

    writeFile(dataSource);
  }

  private void renderDatasource(StringBuilder dataSource)
  {
    String datasourceName = JXPathContext.newContext(chart.getNode()).getPointer("properties/.[name='dataSource']/value").getNode().toString();
    JXPathContext datasourceContext = JXPathContext.newContext(document.getPointer("/datasources/rows[properties[name='name' and value='" + datasourceName + "']]").getNode());

    renderDatasourcePreamble(dataSource, datasourceContext);
    renderParameters(dataSource, JSONArray.fromObject(datasourceContext.getValue("properties/.[name='parameters']/value", String.class)));

    dataSource.append("var data = JSON.parse(String(datasource.execute()));\n");
  }

  /**
   * Sets up the includes, place holders and any other niceties needed for the chart
   */
  private void renderPreamble(StringBuilder dataSource)
  {
    dataSource.append("lib('protovis-bundle.js');\n");
    dataSource.append("lib('ccc-utils.js');\n\n");

    dataSource.append("elem = document.createElement('div');\n"
            + "elem.setAttribute('id','canvas');\n"
            + "document.body.appendChild(elem);\n\n");
  }

  private void renderChart(StringBuilder dataSource)
  {
    ComponentManager engine = ComponentManager.getInstance();
    JXPathContext context = document.getRelativeContext(chart);
    BaseComponent renderer = engine.getRenderer(context);
    renderer.setNode(context);
    dataSource.append(renderer.render(context));


  }

  private void writeFile(StringBuilder dataSource)
  {
    logger.debug(dataSource.toString());
  }

  private void renderDatasourcePreamble(StringBuilder dataSource, JXPathContext context)
  {
    String dataAccessId = (String) context.getValue("properties/.[name='name']/value", String.class);

    dataSource.append("var datasource = datasourceFactory.createDatasource('cda');\n");
    dataSource.append("datasource.setDefinitionFile(render_" + this.chartName + ".chartDefinition.path);\n");
    dataSource.append("datasource.setDataAccessId('" + dataAccessId + "');\n\n");
  }

  private void renderParameters(StringBuilder dataSource, JSONArray params)
  {
    Iterator<JSONArray> it = params.iterator();
    while (it.hasNext())
    {
      JSONArray param = it.next();
      String paramName = param.get(0).toString();
      dataSource.append("var param" + paramName + " = params.get('" + paramName + "') || ''\n");
      dataSource.append("datasource.setParameter('" + paramName + "', param" + paramName + ");\n");
    }
  }
}
