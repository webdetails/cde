package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.render.layout.Render;


public class RenderMobileLayout extends Renderer
{
  public RenderMobileLayout(JXPathContext doc, CdfRunJsDashboardWriteContext context)
  {
    super(doc, context);
  }

  public String render(String alias) throws Exception 
  {
    StringBuffer layout = new StringBuffer("");

    try 
    {
      @SuppressWarnings("unchecked")
      final Iterator<Pointer> rootRows = doc.iteratePointers("/layout/rows[parent='UnIqEiD']");

      layout.append(NEWLINE + getIndent(2) + "<div class='container'>");
      renderRows(doc, rootRows, layout, 4);
      layout.append(NEWLINE + getIndent(2) + "</div>");
    } 
    catch (RenderException e) 
    {
      layout = new StringBuffer(e.getMessage());
    }

    return layout.toString();
  }

  private void renderRows(
          final JXPathContext doc, 
          final Iterator<Pointer> nodeIterator, 
          final StringBuffer layout, 
          final int ident) 
          throws Exception 
  {
    while (nodeIterator.hasNext()) 
    {
      final Pointer pointer = nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext(pointer);

      final String rowId = (String) context.getValue("id");
      
      final Render renderer = (Render)getRender(context);
      renderer.processProperties();
      
      layout.append(NEWLINE + getIndent(ident));
      layout.append(renderer.renderStart());
      
      // Render Child Rows
      renderRows(
          context, 
          context.iteratePointers("/layout/rows[parent='" + rowId + "']"), 
          layout, 
          ident + 2);
      
      layout.append(NEWLINE + getIndent(ident));
      layout.append(renderer.renderClose());
    }
  }

	@Override
	protected String getRenderClassName(final String type) 
  {
		return "pt.webdetails.cdf.dd.render.layout." + type.replace("Layout", "") + "Render";
	}
}