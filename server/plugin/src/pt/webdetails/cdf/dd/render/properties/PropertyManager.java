/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.properties;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.render.components.BaseComponent;
import pt.webdetails.cdf.dd.render.renderer.RendererManager;

/**
 *
 * @author pdpi
 */
public class PropertyManager
{

  public static final String PLUGIN_DIR = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/");
  public static final String PACKAGE_HEADER = "pt.webdetails.cdf.dd.render.properties.";
  private static PropertyManager _engine;
  private String path;
  private Hashtable<String, GenericProperty> propertyPool;

  public PropertyManager()
  {
    init(PLUGIN_DIR + "resources/");
  }

  public PropertyManager(String path)
  {
    init(path);
  }

  public static PropertyManager getInstance()
  {
    if (_engine == null)
    {
      _engine = new PropertyManager();
    }
    return _engine;
  }

  public void refresh()
  {
    // Start by refreshing the dependencies
    RendererManager.getInstance().refresh();
    init(this.path);
  }

  private void indexBaseProperties()
  {
    File dir = new File(path + "base/properties/");
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

        String className = XmlDom4JHelper.getNodeText("/DesignerProperty/Override", doc);
        if (className != null)
        {
          GenericProperty renderer = rendererFromClass(className);
          if (renderer != null)
          {
            propertyPool.put(renderer.getName().toLowerCase(), renderer);
          }
        }
        else
        {
          GenericProperty renderer = new GenericProperty(dir.getPath() + "/" + file, doc.selectSingleNode("/DesignerProperty"));
          if (renderer != null)
          {
            registerProperty(renderer);
          }
        }
      }
      catch (Exception e)
      {
        Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void indexCustomProperties()
  {
    File dir = new File(path + "custom/properties/");
    FilenameFilter subFolders = new FilenameFilter()
    {

      public boolean accept(File systemFolder, String name)
      {
        File plugin = new File(systemFolder.getPath() + "/" + name + "/property.xml");
        return plugin.exists() && plugin.canRead();

      }
    };
    String[] files = dir.list(subFolders);
    for (String file : files)
    {
      try
      {
        Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file + "/property.xml", null);

        // To support multiple definitions on the same file, we'll iterate through all
        // the //DesignerComponent nodes
        List<Node> properties = doc.selectNodes("//DesignerProperty");

        for (Node property : properties)
        {

          String className = XmlDom4JHelper.getNodeText("Override", property);
          if (className != null)
          {
            GenericProperty renderer = rendererFromClass(className);
            if (renderer != null)
            {
              propertyPool.put(renderer.getName().toLowerCase(), renderer);
            }
          }
          else
          {
            GenericProperty renderer = new GenericProperty(dir.getPath() + "/" + file, property);
            if (renderer != null)
            {
              registerProperty(renderer);
            }
          }

        }
      }
      catch (Exception e)
      {
        Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void init(String path)
  {
    // We need the renderers to be initialized here.
    // getInstance is enough to initialize the manager
    RendererManager.getInstance();
    this.path = path;
    this.propertyPool = new Hashtable<String, GenericProperty>();

    indexBaseProperties();
    indexCustomProperties();
  }

  public void registerProperty(GenericProperty property)
  {
    propertyPool.put(property.getName().toLowerCase(), property);
  }

  public void registerPrivateProperty(BaseComponent component, GenericProperty property)
  {
    propertyPool.put((component.getName() + "_" + property.getName()).toLowerCase(), property);
  }

  public GenericProperty getProperty(String name)
  {
    return propertyPool.get(name.toLowerCase());
  }

  public GenericProperty getPrivateProperty(BaseComponent component, String name)
  {
    return propertyPool.get((component.getName() + "_" + name).toLowerCase());
  }

  public String getDefinitions()
  {
    StringBuilder defs = new StringBuilder();
    defs.append(RendererManager.getInstance().getDefinitions());
    for (GenericProperty prop : propertyPool.values())
    {
      defs.append(prop.getDefinition());
    }
    return defs.toString();
  }

  private GenericProperty rendererFromClass(String className)
  {
    GenericProperty renderer = null;
    try
    {
      renderer = (GenericProperty) Class.forName(PACKAGE_HEADER + className).newInstance();
    }
    catch (ClassNotFoundException ex)
    {
      Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SecurityException ex)
    {
      Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (InstantiationException ex)
    {
      Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IllegalAccessException ex)
    {
      Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IllegalArgumentException ex)
    {
      Logger.getLogger(PropertyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return renderer;
  }
}
