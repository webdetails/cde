package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.Widget;
import pt.webdetails.cdf.dd.render.layout.Render;

public class RenderLayout extends Renderer
{

  Class<?>[] rendererConstructorArgs = new Class[]
  {
    JXPathContext.class
  };

  public RenderLayout()
  {
    super();
  }

  public String render(final JXPathContext doc) throws Exception
  {
    return render(doc, "");
  }

  public String render(final JXPathContext doc, String alias) throws Exception
  {
    setDoc(doc);
    StringBuffer layout = new StringBuffer("");
    Map<String, Widget> widgets = getWidgets(alias);
    try
    {
      @SuppressWarnings("unchecked")
      final Iterator<Pointer> rootRows = doc.iteratePointers("/layout/rows[parent='UnIqEiD']");

      layout.append(System.getProperty("line.separator") + getIndent(2) + "<div class='container'>");
      renderRows(doc, rootRows, widgets, alias, layout, 4);
      layout.append(System.getProperty("line.separator") + getIndent(2) + "</div>");

    }
    catch (RenderException e)
    {
      layout = new StringBuffer(e.getMessage());
    }

    return layout.toString();
  }

  private void renderRows(final JXPathContext doc, final Iterator<Pointer> nodeIterator, final Map<String, Widget> widgets, final String alias, final StringBuffer layout, final int indent) throws Exception
  {
    while (nodeIterator.hasNext())
    {
      final Pointer pointer = (Pointer) nodeIterator.next();
      final JXPathContext context = doc.getRelativeContext(pointer);

      String rowId = (String) context.getValue("id"),
              rowName;
      try
      {
        rowName = (String) context.getValue("properties[name='name']/value");
      }
      catch (JXPathException e)
      {
        rowName = "";
      }

      @SuppressWarnings("unchecked")
      final Iterator<Pointer> childrenIterator = context.iteratePointers("/layout/rows[parent='" + rowId + "']");
      final Render renderer = (Render) getRender(context);
      renderer.processProperties();
      renderer.aliasId(alias);
      layout.append(System.getProperty("line.separator") + getIndent(indent));
      layout.append(renderer.renderStart());
      if (widgets.containsKey(rowName))
      {
        layout.append(widgets.get(rowName).getLayout());
      }
      else
      {
        renderRows(context, childrenIterator, widgets, alias, layout, indent + 2);
      }
      layout.append(System.getProperty("line.separator") + getIndent(indent));
      layout.append(renderer.renderClose());
    }
  }

  @Override
  public String getRenderClassName(final String type)
  {
    return "pt.webdetails.cdf.dd.render.layout." + type.replace("Layout", "") + "Render";
  }
}