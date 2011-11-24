/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.renderer;

import java.util.Hashtable;


import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;

/**
 *
 * @author pdpi
 */
public class RendererManager
{

  public static final String PLUGIN_DIR = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/");
  private static RendererManager _engine;
  private String path;
  private Hashtable<String, GenericRenderer> rendererPool;

  public RendererManager()
  {
    init(PLUGIN_DIR + "resources/");
  }

  public RendererManager(String path)
  {
    init(path);
  }

  public static RendererManager getInstance()
  {
    if (_engine == null)
    {
      _engine = new RendererManager();
    }
    return _engine;
  }

  public void refresh()
  {
    init(this.path);
  }

  

  private void init(String path)
  {
    this.path = path;
    this.rendererPool = new Hashtable<String, GenericRenderer>();
  }

  /*public String getEntry() {
  String entry = "";
  Collection<GenericRenderer> components = rendererPool.values();
  for (GenericRenderer renderer : components) {
  entry += renderer.getEntry();
  }
  return entry;
  }*/
  GenericRenderer getRenderer(String name)
  {
    return rendererPool.get(name);
  }

  public String getDefinitions()
  {
    StringBuilder defs = new StringBuilder();
    for (GenericRenderer renderer : rendererPool.values())
    {
      defs.append(renderer.getDefinition());
    }
    return defs.toString();
  }
  
    public void registerRenderer(GenericRenderer renderer)
  {
    rendererPool.put(renderer.getName().toLowerCase(), renderer);
  }
}
