package pt.webdetails.cdf.dd.render;

import java.util.Iterator;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.json.JSONArray;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;

@SuppressWarnings("unchecked")
public class RenderComponents extends Renderer
{
  public RenderComponents(JXPathContext doc, CdfRunJsDashboardWriteContext context, IPentahoSession userSession)
  {
    super(doc, context, userSession);
  }
  
  public String render(String alias) throws Exception
  {
    //Map<String, Widget> widgetsByContainerId = getWidgets(alias);
    
//    StringBuffer widgetContent = new StringBuffer(),
//                 result = new StringBuffer(
//            NEWLINE + "<script language=\"javascript\" type=\"text/javascript\">" + NEWLINE);
//
//    final JSONObject settings = (JSONObject)doc.getValue("/settings");
//    result.append("wcdfSettings = ");
//    result.append(settings.toString(2));
//    result.append(';');
//    
//    String componentsIds = "";
//    
//    ComponentManager engine = ComponentManager.getInstance();
//    
//    Iterator<Pointer> components = doc.iteratePointers("/components/rows");
//    while(components.hasNext())
//    {
//      Pointer pointer = components.next();
//      JXPathContext context = doc.getRelativeContext(pointer);
//      Object metaWidget = context.getValue("meta_widget"),
//             htmlObject;
//      
//      /* If the htmlObject doesn't exist, looking for it will throw an exception. */
//      try
//      {
//        htmlObject = context.getValue("properties[name='htmlObject']/value");
//      }
//      catch (Exception e)
//      {
//        htmlObject = null;
//      }
//      
//      boolean isWidget = metaWidget != null && metaWidget.toString().equals("true");
//      
//      String id = htmlObject != null ? 
//                  htmlObject.toString().replaceAll("\\$\\{.*:(.*)\\}", "$1") : 
//                  "";
//      
//      if (isWidget && widgetsByContainerId.containsKey(id))
//      {
//        // Detect whether we're handling a Widget Component,
//        // which has a single-property parameter blob, 
//        // or a generated component,
//        // which has a separate property per parameter.
//        
//        // TODO: How can an «isWidget=true» component have a parameter blob?
//        // Is this to support components created in the component editor?
//        // WidgetComponent components, "generated" by "Save As Widget",
//        // are created in XmlStructure#savesettings,
//        // and these have separate Parameter properties.
//        widgetContent.append(NEWLINE);
//        
//        Widget widget = widgetsByContainerId.get(id);
//        
//        widgetContent.append(widget.getComponents());
//        
//        String widgetAlias = getWidgetAlias(context, alias);
//        if(context.getValue("meta_wcdf") != null)
//        {
//          renderDiscreteParameters(widgetContent, context, id, widgetAlias);
//        }
//        else
//        {
//          renderParameterBlob(widgetContent, context, id, widgetAlias);
//        }
//      }
//      else
//      {
//        BaseComponent renderer = engine.getRenderer(context);
//        if (renderer != null)
//        {
//          // Discard everything that's not an actual renderable component
//          synchronized (renderer) {
//            renderer.setAlias(alias);
//            renderer.setNode(context);
//            if(renderer.getId().startsWith("render_"))
//            {
//              componentsIds += renderer.getId() + ",";
//            }
//            
//            result.append(NEWLINE);
//            result.append(renderer.render());
//          }
//        }
//      }
//    } // while
//    
//    if (componentsIds.length() > 0)
//    {
//      result.append(NEWLINE + "Dashboards.addComponents([" + componentsIds.replaceAll(",$", "]") + ");");
//    }
//
//    result.append(NEWLINE);
//    result.append("</script>");
//    result.append(NEWLINE);
//    result.append(widgetContent);
//    return result.toString();
    return null;
  }

  @Override
  protected String getRenderClassName(String type)
  {
    return "pt.webdetails.cdf.dd.render.components." + type.replace("Components", "") + "Render";
  }

  private void renderParameterBlob(StringBuffer widgetContent, JXPathContext context, String id, String widgetAlias) throws org.json.JSONException
  {
    JSONArray params = new JSONArray(context.getValue("properties[name='xActionArrayParameter']/value").toString());
    for (int i = 0 ; i < params.length() ; i++)
    {
      JSONArray line = params.getJSONArray(i);
      
      String widgetParam    = aliasName(widgetAlias, line.getString(0));
      String dashboardParam = line.getString(1);
      
      widgetContent.append(NEWLINE + "<script language=\"javascript\" type=\"text/javascript\">" + NEWLINE);
      widgetContent.append("Dashboards.syncParametersOnInit('${p:" + dashboardParam + "}','" + widgetParam + "');\n");
      widgetContent.append("</script>\n");
    }
  }

  private void renderDiscreteParameters(StringBuffer widgetContent, JXPathContext context, String id, String widgetAlias) throws org.json.JSONException
  {
    Iterator<Pointer> it = context.iteratePointers("properties[type='Parameter']");
    while(it.hasNext())
    {
      Pointer pointer = it.next();
      
      JXPathContext ctx = doc.getRelativeContext(pointer);
      String dashboardParam = ctx.getValue("value").toString();
      String widgetParam    = aliasName(widgetAlias, ctx.getValue("name").toString());
      
      widgetContent.append(NEWLINE + "<script language=\"javascript\" type=\"text/javascript\">" + NEWLINE);
      widgetContent.append("Dashboards.syncParametersOnInit('" + dashboardParam + "','" + widgetParam + "');\n");
      widgetContent.append("</script>\n");
    }
  }
} 