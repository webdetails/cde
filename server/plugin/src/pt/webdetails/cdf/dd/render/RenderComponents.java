package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.json.JSONArray;
import pt.webdetails.cdf.dd.Widget;
import pt.webdetails.cdf.dd.render.components.ComponentManager;
import pt.webdetails.cdf.dd.render.components.BaseComponent;

@SuppressWarnings("unchecked")
public class RenderComponents extends Renderer
{

  public static final String newLine = System.getProperty("line.separator");
  Class<JXPathContext>[] rendererConstructorArgs = new Class[]
  {
    JXPathContext.class
  };

  public RenderComponents()
  {
    super();
  }

  public String render(JXPathContext doc) throws Exception
  {
    return render(doc, null);
  }

  public String render(JXPathContext doc, String alias) throws Exception
  {
    setDoc(doc);
    Map<String, Widget> widgets = getWidgets(alias);
    StringBuffer widgetContent = new StringBuffer(),
            result = new StringBuffer(newLine + "<script language=\"javascript\" type=\"text/javascript\">" + newLine);

    final JSONObject settings = (JSONObject) doc.getValue("/settings");
    result.append("wcdfSettings = ");
    result.append(settings.toString(2));
    result.append(';');
    Iterator<Pointer> components = doc.iteratePointers("/components/rows");

    String componentsIds = "";
    ComponentManager engine = ComponentManager.getInstance();
    while (components.hasNext())
    {

      Pointer pointer = components.next();
      JXPathContext context = doc.getRelativeContext(pointer);
      Object metaWidget = context.getValue("meta_widget"),
              htmlObject;

      /* If the htmlObject doesn't exist, looking for it will throw an exception. */
      try
      {
        htmlObject = context.getValue("properties[name='htmlObject']/value");

      }
      catch (Exception e)
      {
        htmlObject = null;
      }
      boolean isWidget = metaWidget != null && metaWidget.toString().equals("true");
      String id = htmlObject != null ? htmlObject.toString().replaceAll("\\$\\{.*:(.*)\\}","$1") : "";
      if (isWidget && widgets.containsKey(id))
      {
        widgetContent.append(newLine);
        widgetContent.append(widgets.get(id).getComponents());
        JSONArray params = new JSONArray(context.getValue("properties[name='xActionArrayParameter']/value").toString());
        for (int i = 0; i < params.length(); i++)
        {
          JSONArray line = params.getJSONArray(i);
          String widgetAlias = getWidgetAlias(context, alias),
                  widgetParam = line.getString(0),
                  dashboardParam = line.getString(1);
          widgetParam = aliasName(widgetAlias,widgetParam);
          widgetContent.append(newLine + "<script language=\"javascript\" type=\"text/javascript\">" + newLine);
          widgetContent.append("Dashboards.parameterModel.on('change:" + widgetParam + "',function(model,value){Dashboards.fireChange('${p:" + dashboardParam + "}',value)});\n");
          widgetContent.append("Dashboards.parameterModel.on('change:${p:" + dashboardParam + "}',function(model,value){Dashboards.fireChange('" + widgetParam + "',value)});\n");
          widgetContent.append("</script>\n");
        }
      }
      else
      {
        BaseComponent renderer = engine.getRenderer(context);
        if (renderer != null)
        {
          // Discard everything that's not an actual renderable component
          renderer.setAlias(alias);
          renderer.setNode(context);
          if (renderer.getId().startsWith("render_"))
          {
            componentsIds += renderer.getId().length() > 0 ? renderer.getId() + "," : "";
          }
          result.append(newLine);
          result.append(renderer.render(context));
        }
      }
    }

    if (componentsIds.length() > 0)
    {
      result.append(newLine + "Dashboards.addComponents([" + componentsIds.replaceAll(",$", "]") + ");");
    }

    result.append(newLine);
    result.append("</script>");
    result.append(newLine);
    result.append(widgetContent);
    return result.toString();
  }

  @Override
  public String getRenderClassName(String type)
  {
    return "pt.webdetails.cdf.dd.render.components." + type.replace("Components", "") + "Render";
  }
}
