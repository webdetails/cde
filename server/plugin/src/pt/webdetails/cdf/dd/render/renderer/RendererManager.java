/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render.renderer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Hashtable;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.jxpath.JXPathContext;

import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.dom4j.Document;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;
import pt.webdetails.cdf.dd.render.components.GenericComponent;

/**
 *
 * @author pdpi
 */
public class RendererManager {

  public static final String PLUGIN_DIR = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME + "/");
  private static RendererManager _engine;
  private String path;
  private Hashtable<String, GenericRenderer> rendererPool;

  public RendererManager() {
    init(PLUGIN_DIR + "resources/");
  }

  public RendererManager(String path) {
    init(path);
  }

  public static RendererManager getInstance() {
    if (_engine == null) {
      _engine = new RendererManager();
    }
    return _engine;
  }

  public void refresh() {
    init(this.path);
  }

  private void indexBaseComponents() {
    File dir = new File(path + "base/renderers/");
    FilenameFilter subFolders = new FilenameFilter() {

      public boolean accept(File systemFolder, String name) {
        File plugin = new File(systemFolder.getPath() + "/" + name + "/renderer.xml");
        return plugin.exists() && plugin.canRead();

      }
    };
    String[] files = dir.list(subFolders);
    for (String file : files) {
      try {
        Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file + "/renderer.xml", null);
        GenericRenderer renderer = new GenericRenderer("resources/base/renderers/" + file + "/", doc);
        if (renderer != null) {
          rendererPool.put(renderer.getName(), renderer);
        }
      } catch (Exception e) {
        Logger.getLogger(RendererManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void indexCustomComponents() {
    File dir = new File(path + "custom/components/");
    FilenameFilter subFolders = new FilenameFilter() {

      public boolean accept(File systemFolder, String name) {
        File plugin = new File(systemFolder.getPath() + "/" + name + "/renderer.xml");
        return plugin.exists() && plugin.canRead();

      }
    };
    String[] files = dir.list(subFolders);
    for (String file : files) {
      try {
        Document doc = XmlDom4JHelper.getDocFromFile(dir.getPath() + "/" + file + "/renderer.xml", null);
        GenericRenderer renderer = new GenericRenderer("resources/custom/renderers/" + file + "/", doc);
        if (renderer != null) {
          rendererPool.put(renderer.getName(), renderer);
        }
      } catch (Exception e) {
        Logger.getLogger(RendererManager.class.getName()).log(Level.SEVERE, null, e);
      }
    }
  }

  private void init(String path) {
    this.path = path;
    this.rendererPool = new Hashtable<String, GenericRenderer>();

    indexBaseComponents();
    indexCustomComponents();
  }

  /*public String getEntry() {
  String entry = "";
  Collection<GenericRenderer> components = rendererPool.values();
  for (GenericRenderer renderer : components) {
  entry += renderer.getEntry();
  }
  return entry;
  }*/

  GenericRenderer getRenderer(String name) {
    return rendererPool.get(name);
  }
}
