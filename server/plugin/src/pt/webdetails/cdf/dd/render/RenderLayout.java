package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.render.layout.Render;
import pt.webdetails.cdf.dd.util.XPathUtils;

public class RenderLayout extends Renderer
{
  public RenderLayout(JXPathContext doc, CdfRunJsDashboardWriteContext context)
  {
    super(doc, context);
  }
  
  public String render(String alias) throws Exception
  {
    try
    {
      @SuppressWarnings("unchecked")
      final Iterator<Pointer> rootRows = doc.iteratePointers("/layout/rows[parent='UnIqEiD']");
      
      StringBuffer layout = new StringBuffer();
      
      layout.append(NEWLINE + getIndent(2) + "<div class='container'>");
      
      Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId = getWidgets(alias);
      
      renderRows(doc, rootRows, widgetsByContainerId, alias, layout, 4);
      
      layout.append(NEWLINE + getIndent(2) + "</div>");
      
      return layout.toString();
    }
    catch(RenderException ex)
    {
      return ex.getMessage();
    }
  }

  private void renderRows(
          final JXPathContext doc, 
          final Iterator<Pointer> nodeIterator, 
          final Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId, 
          final String alias, 
          final StringBuffer layout, 
          final int indent) throws Exception
  {
    while (nodeIterator.hasNext())
    {
      final Pointer pointer = (Pointer) nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext(pointer);

      final String rowId   = (String)context.getValue("id");
      final String rowName = XPathUtils.getStringValue(context, "properties[name='name']/value");
      
      @SuppressWarnings("unchecked")
      final Render renderer = (Render)getRender(context);
      renderer.processProperties();
      renderer.aliasId(alias);
      layout.append(NEWLINE + getIndent(indent));
      layout.append(renderer.renderStart());
      
      if(widgetsByContainerId.containsKey(rowName))
      {
        CdfRunJsDashboardWriteResult widgetResult = widgetsByContainerId.get(rowName);
        layout.append(widgetResult.getLayout());
      }
      else
      {
        renderRows(
            context, 
            context.iteratePointers("/layout/rows[parent='" + rowId + "']"), 
            widgetsByContainerId, 
            alias, 
            layout, 
            indent + 2);
      }
      
      layout.append(NEWLINE + getIndent(indent));
      layout.append(renderer.renderClose());
    }
  }

  @Override
  protected String getRenderClassName(final String type)
  {
    return "pt.webdetails.cdf.dd.render.layout." + type.replace("Layout", "") + "Render";
  }
}