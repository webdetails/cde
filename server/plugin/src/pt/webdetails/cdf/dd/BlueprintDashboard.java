/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.render.StringFilter;
import pt.webdetails.cdf.dd.structure.WcdfDescriptor;

/**
 *
 * @author pdpi
 */
public class BlueprintDashboard extends AbstractDashboard {
  /* CONSTANTS */

  private static final long serialVersionUID = 1L;

  private static Log logger = LogFactory.getLog(Dashboard.class);

  protected final static String TYPE = "blueprint";

  public BlueprintDashboard(IParameterProvider pathParams, IParameterProvider requestParams)
      throws FileNotFoundException {
    super(pathParams, requestParams);
  }

  public BlueprintDashboard(WcdfDescriptor wcdf, boolean absolute, String absRoot, boolean debug, String scheme,
      String alias) throws FileNotFoundException {
    super(wcdf, absolute, absRoot, debug, scheme, alias);
  }

  public BlueprintDashboard(WcdfDescriptor wcdf, boolean absolute, String absRoot, boolean debug, String scheme)
      throws FileNotFoundException {
    super(wcdf, absolute, absRoot, debug, scheme);
  }

  public BlueprintDashboard(String wcdfPath, boolean absolute, String absRoot, boolean debug, String scheme)
      throws FileNotFoundException {
    super(wcdfPath, absolute, absRoot, debug, scheme);
  }

  protected String renderHeaders(String contents) {
    String dependencies, styles, cdfDependencies;
    final String title = "<title>" + getWcdf().getTitle() + "</title>";
    
    // Acquire CDF headers
    try {
      cdfDependencies = DashboardDesignerContentGenerator.getCdfIncludes(contents, getType(), debug, absRoot, scheme);
    } catch (Exception e) {
      logger.error("Failed to get cdf includes");
      cdfDependencies = "";
    }

    // Acquire CDE-specific headers
    //    if (absolute) {
    //      final String adornedRoot = (scheme.equals("") ? "" : (scheme + "://")) + absRoot;
    //      StringFilter css = new StringFilter() {
    //        public String filter(String input) {
    //          // input = input.replaceAll("\\?", "&");
    //          return "\t\t<link href='" + adornedRoot + DashboardDesignerContentGenerator.SERVER_URL_VALUE
    //              + "getCssResource/" + input + "' rel='stylesheet' type='text/css' />\n";
    //        }
    //      };
    //      StringFilter js = new StringFilter() {
    //        public String filter(String input) {
    //          // input = input.replaceAll("\\?", "&"); 
    //          return "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"" + adornedRoot
    //              + DashboardDesignerContentGenerator.SERVER_URL_VALUE + "getJsResource/" + input + "\"></script>\n";
    //        }
    //      };
    //      if (debug) {
    //        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getDependencies(js);
    //        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getDependencies(css);
    //      } else {
    //        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF)
    //            .getPackagedDependencies(js);
    //        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS)
    //            .getPackagedDependencies(css);
    //      }
    //    } else {
    //      if (debug) {
    //        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getDependencies();
    //        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getDependencies();
    //      } else {
    //        dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF)
    //            .getPackagedDependencies();
    //        styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS)
    //            .getPackagedDependencies();
    //      }
    //    }

    final String adornedRoot = absolute ? ((scheme.equals("") ? "" : (scheme + "://")) + absRoot) : "";

    StringFilter js = new StringFilter() {

      @Override
      public String filter(String input) {
        return String.format(
            "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%s%s%s%s\"></script>\n", adornedRoot,
            DashboardDesignerContentGenerator.SERVER_URL_VALUE, "getJsResource/", input);
      }

    };

    StringFilter css = new StringFilter() {
      @Override
      public String filter(String input) {
        return String.format(
            "\t\t<script language=\"javascript\" type=\"text/javascript\" src=\"%s%s%s%s\"></script>\n", adornedRoot,
            DashboardDesignerContentGenerator.SERVER_URL_VALUE, "getCssResource/", input);
      }

    };

    if (debug) {
      dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF).getDependencies(js);
      styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS).getDependencies(css);
    } else {
      dependencies = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF)
          .getPackagedDependencies(js);
      styles = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_CSS)
          .getPackagedDependencies(css);
    }

    String raw = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDF_RAW).getDependencies();
    return title + cdfDependencies + raw + dependencies + styles;
  }

  public String getType() {
    return TYPE;
  }
}
