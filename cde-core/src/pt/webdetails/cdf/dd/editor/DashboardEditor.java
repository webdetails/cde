package pt.webdetails.cdf.dd.editor;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.ResourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.render.DependenciesManager;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.repository.api.IReadAccess;

/**
 * Created with IntelliJ IDEA.
 * User: diogomariano
 * Date: 06/09/13
 */
public class DashboardEditor {

  private static Log logger = LogFactory.getLog( DashboardEditor.class );


  public static String getEditor(String wcdfPath, boolean debugMode, String scheme) throws Exception {

    final HashMap<String, String> tokens = new HashMap<String, String>();

    DependenciesManager depMgr = DependenciesManager.getInstance();
    final String cdeDeps = depMgr.getPackage(DependenciesManager.StdPackages.CDFDD).getDependencies(false);//TODO: check
    tokens.put(CdeConstants.DESIGNER_HEADER_TAG, cdeDeps);

    ResourceManager resMgr = ResourceManager.getInstance();
    
    //TODO: this is a tad confusing
    // Decide whether we're in debug mode (full-size scripts) or normal mode (minified scripts)
    final String scriptDeps, styleDeps;
    IReadAccess sysReader = CdeEnvironment.getPluginSystemReader();
    if(debugMode){
      
      if ( resMgr.existsInCache( CdeConstants.DESIGNER_SCRIPTS_RESOURCE ) ) {
        scriptDeps = resMgr.getResourceFromCache( CdeConstants.DESIGNER_SCRIPTS_RESOURCE );
      } else {

        scriptDeps = Util.toString(sysReader.getFileInputStream(CdeConstants.DESIGNER_SCRIPTS_RESOURCE));

        if(scriptDeps != null){
          resMgr.putResourceInCache(CdeConstants.DESIGNER_SCRIPTS_RESOURCE, scriptDeps);
        }
      }
      
      if(resMgr.existsInCache(CdeConstants.DESIGNER_STYLES_RESOURCE)){
        styleDeps = resMgr.getResourceFromCache(CdeConstants.DESIGNER_STYLES_RESOURCE);
      }else{
        styleDeps  = Util.toString(sysReader.getFileInputStream(CdeConstants.DESIGNER_STYLES_RESOURCE));
        
        if(styleDeps != null){
          resMgr.putResourceInCache(CdeConstants.DESIGNER_STYLES_RESOURCE, styleDeps);
        }
      }
      
    } else {
        styleDeps = depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_CSS_INCLUDES ).getDependencies( true );
        scriptDeps = depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_JS_INCLUDES ).getDependencies( true );
    }

    tokens.put(CdeConstants.DESIGNER_STYLES_TAG,  styleDeps );
    tokens.put(CdeConstants.DESIGNER_SCRIPTS_TAG, scriptDeps);

    try {
      final String cdfDeps = CdeEngine.getEnv().getCdfIncludes("empty", "desktop", debugMode, null, scheme);
      tokens.put(CdeConstants.DESIGNER_CDF_TAG, cdfDeps);
    } catch (Exception e) {
      logger.fatal( "Unable to get CDF dependencies", e );
    }
    tokens.put(CdeConstants.FILE_NAME_TAG,    DashboardWcdfDescriptor.toStructurePath(wcdfPath));
    //FIXME paths
    IUrlProvider urlProvider = CdeEngine.getEnv().getPluginEnv().getUrlProvider();
    final String apiPath = urlProvider.getPluginBaseUrl();
    tokens.put(CdeConstants.SERVER_URL_TAG, apiPath);
    // external api
    ICdeApiPathProvider extApi = CdeEngine.getEnv().getExtApi();
    tokens.put(CdeConstants.DATA_URL_TAG, CdeEngine.getInstance().getEnvironment().getApplicationBaseContentUrl() + "Syncronize");
    
    tokens.put(CdeConstants.Tags.Api.RENDERER, extApi.getRendererBasePath());

    String cacheKey = ResourceManager.buildCacheKey(wcdfPath, tokens);
    String resource  = null;
    
    if(resMgr.existsInCache(wcdfPath, tokens)){
      
      resource = resMgr.getResourceFromCache(cacheKey);
      
    }else{
    
      resource = Util.toString(sysReader.getFileInputStream(CdeConstants.DESIGNER_RESOURCE));
    
      if (tokens != null && tokens.size() > 0) {
        for (final String key : tokens.keySet())  {
          resource = StringUtils.replace(resource, key, tokens.get(key));
        }
    }
      
      // We have the resource. Should we cache it?
      if (resMgr.isCacheEnabled()) {
        resMgr.putResourceInCache(cacheKey , resource);
      }
    }
    
    return resource;
  }
}