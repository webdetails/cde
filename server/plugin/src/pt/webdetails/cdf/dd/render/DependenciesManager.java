/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cdf.dd.render;

import java.util.HashMap;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cdf.dd.packager.Packager;

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

      String basePath = PentahoSystem.getApplicationContext().getSolutionPath("");
      _engine.registerEngine(Engines.CDF_CSS, new DependenciesEngine(Engines.CDF_CSS, cssFilter, basePath, Packager.Filetype.CSS));
      _engine.registerEngine(Engines.CDF, new DependenciesEngine(Engines.CDF, jsFilter, basePath, Packager.Filetype.JS));
      _engine.registerEngine(Engines.CDF_RAW, new DependenciesEngine(Engines.CDF_RAW, rawFilter, basePath, Packager.Filetype.JS));
      _engine.registerEngine(Engines.CDFDD, new DependenciesEngine(Engines.CDFDD, jsFilter, basePath, Packager.Filetype.JS));
    }
    return _engine;
  }
  
  
  public static final class Engines {
    public final static String CDF = "CDF";
    public final static String CDF_CSS = "CDF-CSS";
    public final static String CDF_RAW = "CDF-RAW";
    public final static String CDFDD = "CDFDD";
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
