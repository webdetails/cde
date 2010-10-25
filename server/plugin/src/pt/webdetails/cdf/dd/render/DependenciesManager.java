/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render;

import java.util.HashMap;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.DashboardDesignerContentGenerator;

/**
 *
 * @author pdpi
 */
public class DependenciesManager
{

  private static DependenciesManager _engine;
  private HashMap<String, DependenciesEngine> engines;

  public DependenciesManager()
  {
    engines = new HashMap<String, DependenciesEngine>();
  }

  public static void refresh()
  {
    _engine = null;
  }

  public static DependenciesManager getInstance()
  {

    if (_engine == null)
    {

      // Raw Filter is really just an identity function for when we want the dependency contents to be included as-is.
      StringFilter rawFilter = new StringFilter()
      {

        public String filter(String input)
        {
          return input;
        }
      };
      StringFilter jsFilter = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"getJsResource/" + input + "\"></script>";
        }
      };
      StringFilter cssFilter = new StringFilter()
      {

        public String filter(String input)
        {
          //input = input.replaceAll("\\?", "&");
          return "\t\t<link href='getCssResource/" + input + "' rel='stylesheet' type='text/css' />";
        }
      };
      _engine = new DependenciesManager();
      String basePath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + DashboardDesignerContentGenerator.PLUGIN_NAME);
      _engine.registerEngine("CDF-CSS", new DependenciesEngine("CDF-CSS", cssFilter, basePath, "CSS"));
      _engine.registerEngine("CDF", new DependenciesEngine("CDF", jsFilter, basePath, "JS"));
      _engine.registerEngine("CDF-RAW", new DependenciesEngine("CDF-RAW", rawFilter, basePath, "JS"));
      _engine.registerEngine("CDFDD", new DependenciesEngine("CDFDD", jsFilter, basePath, "JS"));
    }
    return _engine;
  }

  public DependenciesEngine getEngine(String id)
  {
    return engines.get(id);
  }

  public boolean hasEngine(String id)
  {
    return engines.containsKey(id);
  }

  public void registerEngine(String id, DependenciesEngine engine)
  {
    engines.put(id, engine);
  }
}
