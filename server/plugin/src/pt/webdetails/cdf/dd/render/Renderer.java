package pt.webdetails.cdf.dd.render;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.DashboardFactory;
import pt.webdetails.cdf.dd.Widget;

@SuppressWarnings("unchecked")
public abstract class Renderer
{

  protected JXPathContext doc;
  protected static Log logger;
  Class<JXPathContext>[] rendererConstructorArgs = new Class[]
  {
    JXPathContext.class
  };

  public Renderer()
  {
    logger = LogFactory.getLog(Renderer.class);
  }

  public abstract String render(JXPathContext doc) throws Exception;

  public abstract String render(JXPathContext doc, String alias) throws Exception;

  public abstract String getRenderClassName(String Type);

  protected Map<String, Widget> getWidgets(String alias)
  {
    Map<String, Widget> locations = new HashMap<String, Widget>();
    Iterator<Pointer> widgets = doc.iteratePointers("/components/rows[meta_widget='true']");
    Pointer pointer;
    while (widgets.hasNext())
    {
      pointer = widgets.next();
      final JXPathContext context = doc.getRelativeContext(pointer);
      String widgetPath,
              widgetContainer = context.getValue("properties[name='htmlObject']/value").toString().replaceAll("\\$\\{.*:(.*)\\}", "$1"),
              newAlias = getWidgetAlias(context, alias);

      try
      {
        widgetPath = context.getValue("properties[name='path']/value").toString();
      }
      catch (Exception e)
      {
        widgetPath = context.getValue("meta_wcdf").toString();
      }
      
      try
      {
        Widget widget = DashboardFactory.getInstance().loadWidget(widgetPath, newAlias);
        locations.put(widgetContainer, widget);
      }
      catch (FileNotFoundException e)
      {
        logger.error("Couldn't find widget " + widgetPath);
      }
    }
    return locations;
  }

  public String getWidgetAlias(Pointer pointer, String alias)
  {
    return getWidgetAlias(doc.getRelativeContext(pointer), alias);
  }

  public String getWidgetAlias(JXPathContext context, String alias)
  {
    String widgetName = context.getValue("properties[name='name']/value").toString(),
            newAlias = alias.length() == 0 ? widgetName : alias + "_" + widgetName;
    return newAlias;
  }

  public Object getRender(JXPathContext context) throws Exception
  {

    Object renderer = null;
    String renderType = null;
    try
    {

      renderType = (String) context.getValue("type");

      if (!renderType.equals("Label"))
      {
        Class<?> rendererClass = Class.forName(getRenderClassName(renderType));

        Constructor<?> constructor = rendererClass.getConstructor(rendererConstructorArgs);
        renderer = constructor.newInstance(new Object[]
                {
                  context
                });
      }

    }
    catch (InstantiationException e)
    {
      logger.error(e.getStackTrace());
    }
    catch (IllegalAccessException e)
    {
      // TODO Auto-generated catch block
      logger.error(e.getStackTrace());
    }
    catch (ClassNotFoundException e)
    {
      // TODO Auto-generated catch block
      logger.error("Class not found: " + renderType);
      //throw new RenderException("Render not found for: " + renderType);
    }

    return renderer;
  }

  public String getIndent(int ident)
  {

    switch (ident)
    {
      case 0:
        return "";
      case 1:
        return " ";
      case 2:
        return "  ";
      case 3:
        return "   ";
      case 4:
        return "    ";
      case 8:
        return "        ";

    }

    StringBuilder identStr = new StringBuilder();
    for (int i = 0; i < ident; i++)
    {
      identStr.append(" ");
    }
    return identStr.toString();

  }

  public JXPathContext getDoc()
  {
    return doc;
  }

  public void setDoc(JXPathContext doc)
  {
    this.doc = doc;
  }

  static public String aliasName(String alias, String name)
  {
    String parsedAlias = alias == null || alias.length() == 0 ? "" : alias + "_";
    return parsedAlias + name;
  }
}
