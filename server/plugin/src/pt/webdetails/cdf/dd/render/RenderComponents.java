package pt.webdetails.cdf.dd.render;


import java.util.Iterator;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import pt.webdetails.cdf.dd.render.components.ComponentManager;
import pt.webdetails.cdf.dd.render.components.BaseComponent;

@SuppressWarnings("unchecked")
public class RenderComponents extends Renderer {

  public static final String newLine = System.getProperty("line.separator");
  Class[] rendererConstructorArgs = new Class[]{JXPathContext.class};

  public RenderComponents() {
    super();
  }

  public String render(JXPathContext doc) throws Exception {

    StringBuffer result = new StringBuffer(newLine + "<script language=\"javascript\" type=\"text/javascript\">" + newLine);

    final JSONObject settings = (JSONObject) doc.getValue("/settings");
    result.append("wcdfSettings = ");
    result.append(settings.toString(2));
    result.append(';');
    Iterator components = doc.iteratePointers("/components/rows");

    String componentsIds = "";
    ComponentManager engine = ComponentManager.getInstance();
    while (components.hasNext()) {

      Pointer pointer = (Pointer) components.next();
      JXPathContext context = doc.getRelativeContext(pointer);

      BaseComponent renderer = engine.getRenderer(context);
      if (renderer != null) {
        // Discard everything that's not an actual renderable component
        renderer.setNode(context);
        if (renderer.getId().startsWith("render_")) {
          componentsIds += renderer.getId().length() > 0 ? renderer.getId() + "," : "";
        }
        result.append(newLine);
        result.append(renderer.render(context));
      }
    }

    if (componentsIds.length() > 0) {
      result.append(newLine + "Dashboards.init([" + componentsIds.replaceAll(",$", "]") + ");");
    }

    result.append(newLine);
    result.append("</script>");
    result.append(newLine);

    return result.toString();
  }

  @Override
  public String getRenderClassName(String type) {
    return "pt.webdetails.cdf.dd.render.components." + type.replace("Components", "") + "Render";
  }
}
