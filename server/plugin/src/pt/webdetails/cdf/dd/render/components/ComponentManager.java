/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.components;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.pentaho.platform.api.engine.IContentGenerator;

import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.w3c.dom.traversal.NodeIterator;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
//import pt.webdetails.cdf.dd.render.datasources.CdaDatasource;
import pt.webdetails.cdf.dd.render.datasources.CdaDatasource;
import pt.webdetails.cdf.dd.render.properties.PropertyManager;

/**
 *
 * @author pdpi
 */
public class ComponentManager
{

  public static final String PLUGIN_DIR = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/");
  private static ComponentManager _engine;
  private String path;
  private Hashtable<String, BaseComponent> componentPool;
  private static final String PACKAGEHEADER = "pt.webdetails.cdf.dd.render.components.";
  private JSON cdaSettings = null;

  public ComponentManager()
  {
    init(PLUGIN_DIR + "resources/");
  }

  public ComponentManager(String path)
  {
    cdaSettings = null;
    init(path);
  }

  public static synchronized ComponentManager getInstance()
  {
    if (_engine == null)
    {
      _engine = new ComponentManager();
    }
    return _engine;
  }

  public void refresh()
  {
    // Start by refreshing the dependencies
    PropertyManager.getInstance().refresh();
    init(this.path);
  }

  private void indexBaseComponents()
  {
    File dir = new File(path + "base/components/");
    FilenameFilter xmlFiles = new FilenameFilter()
    {

      public boolean accept(File dir, String name)
      {
        return !name.startsWith(".") && name.endsWith(".xml");
      }
    };
    String[] files = dir.list(xmlFiles);
    for (String file : files)
    {
      try
      {
        Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file, null);

        // To support multiple definitions on the same file, we'll iterate through all
        // the //DesignerComponent nodes
        List<Node> components = doc.selectNodes("//DesignerComponent");

        for (Node component : components)
        {

          // To figure out whether the component is generic or has a special implementation,
          // we directly look for the class override in the definition
          String className = XmlDom4JHelper.getNodeText("Override", component);
          if (className != null)
          {
            BaseComponent renderer = rendererFromClass(className);
            if (renderer != null)
            {
              componentPool.put(renderer.getName(), renderer);
            }
          }
          else
          {
            GenericComponent renderer = new GenericComponent();
            if (renderer != null)
            {
              try
              {
                renderer.setDefinition(component);
                componentPool.put(renderer.getName(), renderer);
              }
              catch (Exception e)
              {
                Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, e);
              }
            }
          }

        }

      }
      catch (Exception e)
      {
        Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void indexCustomComponents()
  {
    File dir = new File(path + "custom/components/");
    FilenameFilter subFolders = new FilenameFilter()
    {

      public boolean accept(File systemFolder, String name)
      {
        File plugin = new File(systemFolder.getPath() + "/" + name + "/component.xml");
        return plugin.exists() && plugin.canRead();

      }
    };
    String[] files = dir.list(subFolders);
    for (String file : files)
    {
      try
      {
        Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file + "/component.xml", null);


        // To support multiple definitions on the same file, we'll iterate through all
        // the //DesignerComponent nodes
        List<Node> components = doc.selectNodes("//DesignerComponent");

        for (Node component : components)
        {

          // To figure out whether the component is generic or has a special implementation,
          // we directly look for the class override in the definition
          String className = XmlDom4JHelper.getNodeText("Override", component);
          if (className != null)
          {
            BaseComponent renderer = rendererFromClass(className);
            if (renderer != null)
            {
              componentPool.put(renderer.getName(), renderer);
            }
          }
          else
          {
            CustomComponent renderer = new CustomComponent("resources/custom/components/" + file + "/");
            if (renderer != null)
            {
              renderer.setDefinition(component);
              componentPool.put(renderer.getName(), renderer);
            }
          }

        }

      }
      catch (Exception e)
      {
        Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void init(String path)
  {
    this.path = path;
    this.componentPool = new Hashtable<String, BaseComponent>();
    // we need the properties to be initialized. Calling getInstance() is enough.
    PropertyManager.getInstance();
    indexBaseComponents();
    indexCustomComponents();
  }

  private BaseComponent rendererFromClass(String className)
  {
    BaseComponent renderer = null;
    try
    {
      renderer = (BaseComponent) Class.forName(PACKAGEHEADER + className).newInstance();
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SecurityException ex)
    {
      Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (InstantiationException ex)
    {
      Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IllegalAccessException ex)
    {
      Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IllegalArgumentException ex)
    {
      Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return renderer;
  }

  public String getEntry()
  {
    StringBuilder entry = new StringBuilder();
    Collection<BaseComponent> components = componentPool.values();
    for (IComponent render : components)
    {
      entry.append(render.getEntry());
    }
    return entry.toString();
  }

  public String getModel()
  {
    StringBuilder model = new StringBuilder();
    Collection<BaseComponent> components = componentPool.values();
    for (IComponent render : components)
    {
      model.append(render.getModel());
    }
    return model.toString();
  }

  public String getDefinitions()
  {
    StringBuilder defs = new StringBuilder();
    defs.append(PropertyManager.getInstance().getDefinitions());
    defs.append(getEntry());
    defs.append(getModel());
    return defs.toString().replaceAll(",\n(\t*)}", "\n$1}");

  }

  public String getImplementations()
  {
    return "";
  }

  public BaseComponent getRenderer(JXPathContext context)
  {
    String renderType = ((String) context.getValue("type")).replace("Components", "");
    return componentPool.get(renderType);
  }

  public void parseCdaDefinitions(JSON json) throws Exception
  {
    cdaSettings = json;
    final JXPathContext doc = JXPathContext.newContext(json);
    Iterator<Pointer> pointers = doc.iteratePointers("*");
    while (pointers.hasNext())
    {
      Pointer pointer = pointers.next();
      CdaDatasource ds = new CdaDatasource(pointer);
      componentPool.put(ds.getName(), ds);
    }
  }

  public JSON getCdaDefinitions()
  {
    return cdaSettings;
  }
}
