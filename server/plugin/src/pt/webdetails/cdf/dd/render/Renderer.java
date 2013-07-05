package pt.webdetails.cdf.dd.render;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Component;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.WidgetComponent;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;

@SuppressWarnings("unchecked")
public abstract class Renderer
{
  protected static final String NEWLINE = System.getProperty("line.separator");
  
  protected final JXPathContext doc;
  protected final CdfRunJsDashboardWriteContext _context;
  protected final IPentahoSession _userSession;
  
  public Renderer(JXPathContext doc, CdfRunJsDashboardWriteContext context, IPentahoSession userSession)
  {
    this.doc = doc;
    this._context = context;
    this._userSession = userSession;
  }
  
  protected static Log logger = LogFactory.getLog(Renderer.class);
  
  protected static final Class<JXPathContext>[] rendererConstructorArgs = new Class[] { JXPathContext.class };
  
  // ---------------
  
  public abstract String render(String alias) throws Exception;

  protected abstract String getRenderClassName(String Type);

  /**
   * Obtains Widgets (renderers) for contained components (usages).
   * 
   * All returned widgets are loaded with an alias that 
   * is prefixed by the specified aliasPrefix argument.
   */
  protected final Map<String, CdfRunJsDashboardWriteResult> getWidgets(String aliasPrefix)
  {
    Map<String, CdfRunJsDashboardWriteResult> widgetsByContainerId = 
            new HashMap<String, CdfRunJsDashboardWriteResult>();
    
    Dashboard dashboard = this._context.getDashboard();
    if(dashboard.getRegularCount() > 0)
    {
      DashboardManager dashMgr = DashboardManager.getInstance();
      CdfRunJsDashboardWriteOptions options = this._context.getOptions();
      
      Iterable<Component> components = dashboard.getRegulars();
      for(Component comp : components)
      {
        if(comp instanceof WidgetComponent)
        {
          WidgetComponent widgetComp = (WidgetComponent)comp;

          CdfRunJsDashboardWriteResult dashResult = null;
          try
          {
            dashResult = dashMgr.getDashboardCdfRunJs(
                    widgetComp.getWcdfPath(), 
                    options, 
                    this._userSession,
                    this._context.isBypassCacheRead());
          }
          catch (ThingWriteException ex)
          {
            logger.error("Could not render widget '" + widgetComp.getWcdfPath()  + "'", ex);
          }

          String containerId = widgetComp.tryGetPropertyValue("htmlObject", "")
                  .replaceAll("\\$\\{.*:(.*)\\}", "$1");

          widgetsByContainerId.put(containerId, dashResult);
        }
      }
    }
    
    return widgetsByContainerId;
  }
  /*
  private Widget getWidget(JXPathContext widgetContext, String aliasPrefix)
  {
    String widgetPath;
    try
    {
      widgetPath = widgetContext.getValue("properties[name='path']/value").toString();
    }
    catch(Exception ex)
    {
      widgetPath = widgetContext.getValue("meta_wcdf").toString();
    }

    String alias = getWidgetAlias(widgetContext, aliasPrefix);
    try
    {
      return DashboardFactory.getInstance().loadWidget(widgetPath, alias);
    }
    catch(FileNotFoundException ex)
    {
      logger.error("Couldn't find widget " + widgetPath);
    }
    catch (IOException ex)
    {
      logger.error("Couldn't load widget " + widgetPath, ex);
    }
    catch (Exception ex)
    {
      logger.error("Couldn't load widget " + widgetPath, ex);
    }
    
    return null;
  }
  */
  protected final Object getRender(JXPathContext context) throws Exception
  {
    String renderType = null;
    try
    {
      renderType = (String) context.getValue("type");
      if (!renderType.equals("Label"))
      {
        Class<?> rendererClass = Class.forName(getRenderClassName(renderType));

        Constructor<?> constructor = rendererClass.getConstructor(rendererConstructorArgs);
        return constructor.newInstance(new Object[]{ context });
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

    return null;
  }

  protected final String getIndent(int indent)
  {
    switch(indent)
    {
      case 0: return "";
      case 1: return " ";
      case 2: return "  ";
      case 3: return "   ";
      case 4: return "    ";
      case 8: return "        ";
    }

    StringBuilder identStr = new StringBuilder();
    for(int i = 0; i < indent; i++) { identStr.append(" "); }
    return identStr.toString();
  }

  public static String aliasName(String aliasPrefix, String name)
  {
    aliasPrefix = StringUtils.isEmpty(aliasPrefix) ? "" : (aliasPrefix + "_");
    
    return aliasPrefix + name;
  }
  
  protected static String getWidgetAlias(JXPathContext context, String aliasPrefix)
  {
    String widgetName = context.getValue("properties[name='name']/value").toString();
    return aliasName(aliasPrefix, widgetName);
  }
}
