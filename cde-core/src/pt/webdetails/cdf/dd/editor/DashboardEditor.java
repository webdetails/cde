package pt.webdetails.cdf.dd.editor;

import pt.webdetails.cdf.dd.ResourceManager;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.packager.Packager;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriter;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cpf.repository.IRepositoryAccess;



import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: diogomariano
 * Date: 06/09/13
 */
public class DashboardEditor {   //TODO: remove packager from content generator when no need to get solutionpath of plugin
  public static String getEditor(String wcdfPath, boolean debugMode, String scheme, Packager packager) throws IOException {


    final ResourceManager resMgr = ResourceManager.getInstance();

    final HashMap<String, String> tokens = new HashMap<String, String>();

    final String cdeDeps = DependenciesManager.getInstance().getEngine(DependenciesManager.Engines.CDFDD).getDependencies();
    tokens.put(CdeConstants.DESIGNER_HEADER_TAG, cdeDeps);

    // Decide whether we're in debug mode (full-size scripts) or normal mode (minified scripts)
    final String scriptDeps, styleDeps;
    if(debugMode){
      scriptDeps = resMgr.getResourceAsString(CdeConstants.DESIGNER_SCRIPTS_RESOURCE);
      styleDeps  = resMgr.getResourceAsString(CdeConstants.DESIGNER_STYLES_RESOURCE );
    } else {
      String stylesHash  = packager.minifyPackage("styles" );
      String scriptsHash = packager.minifyPackage("scripts");

      styleDeps  = "<link href=\"css/styles.css?version=" + stylesHash + "\" rel=\"stylesheet\" type=\"text/css\" />";
      scriptDeps = "<script type=\"text/javascript\" src=\"js/scripts.js?version=" + scriptsHash + "\"></script>";
    }

    tokens.put(CdeConstants.DESIGNER_STYLES_TAG,  styleDeps );
    tokens.put(CdeConstants.DESIGNER_SCRIPTS_TAG, scriptDeps);


    final String cdfDeps = CdfRunJsDashboardWriter.getCdfIncludes("empty", "desktop", debugMode, null, scheme);
    tokens.put(CdeConstants.DESIGNER_CDF_TAG, cdfDeps);
    tokens.put(CdeConstants.FILE_NAME_TAG,    DashboardWcdfDescriptor.toStructurePath(wcdfPath));
    tokens.put(CdeConstants.SERVER_URL_TAG,   CdeConstants.SERVER_URL_VALUE);
    tokens.put(CdeConstants.DATA_URL_TAG,     CdeConstants.DATA_URL_VALUE);

    return resMgr.getResourceAsString(CdeConstants.DESIGNER_RESOURCE, tokens);
  }
}
