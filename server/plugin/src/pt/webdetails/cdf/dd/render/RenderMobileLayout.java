package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.render.layout.Render;


public class RenderMobileLayout extends Renderer{

    Class<?>[] rendererConstructorArgs = new Class[]{JXPathContext.class};

    public RenderMobileLayout() {
    	super();
    }
    public String render(final JXPathContext doc) throws Exception {
      return render(doc,null);
    }
    public String render(final JXPathContext doc, String alias) throws Exception {

        StringBuffer layout = new StringBuffer("");

        try {
            @SuppressWarnings("unchecked")
            final Iterator<Pointer> rootRows = doc.iteratePointers("/layout/rows[parent='UnIqEiD']");

            layout.append(System.getProperty("line.separator") + getIndent(2) + "<div class='container'>");
            renderRows(doc, rootRows, layout, 4);
            layout.append(System.getProperty("line.separator") + getIndent(2) + "</div>");

        } catch (RenderException e) {
            layout = new StringBuffer(e.getMessage());
        }

        return layout.toString();
        
    }

    private void renderRows(final JXPathContext doc, final Iterator<Pointer> nodeIterator, final StringBuffer layout, final int ident) throws Exception {

        while (nodeIterator.hasNext()) {

            final Pointer pointer = nodeIterator.next();
            final JXPathContext context = doc.getRelativeContext(pointer);

            final String rowId = (String) context.getValue("id");
            @SuppressWarnings("unchecked")
            final Iterator<Pointer> childrenIterator = context.iteratePointers("/layout/rows[parent='" + rowId + "']");
            
            final Render renderer = (Render) getRender(context);
            renderer.processProperties();

            layout.append(System.getProperty("line.separator") + getIndent(ident));
            layout.append(renderer.renderStart());
            renderRows(context, childrenIterator, layout, ident + 2);
            layout.append(System.getProperty("line.separator") + getIndent(ident));
            layout.append(renderer.renderClose());

        }

    }

	@Override
	public String getRenderClassName(final String type) {
		return "pt.webdetails.cdf.dd.render.layout." + type.replace("Layout", "") + "Render";
	}
}