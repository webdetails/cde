package pt.webdetails.cdf.dd.editor;

import java.io.IOException;
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


  public static String getEditor(String wcdfPath, boolean debugMode, String scheme, boolean isDefault) throws Exception {

    ResourceManager resMgr = ResourceManager.getInstance();
    IReadAccess sysReader = CdeEnvironment.getPluginSystemReader();

    final HashMap<String, String> tokens = buildReplacementTokenMap( wcdfPath, scheme, debugMode, resMgr, sysReader );

    return getProcessedEditor( wcdfPath, resMgr, tokens, sysReader, isDefault );

  }

  private static HashMap<String, String> buildReplacementTokenMap(
      String wcdfPath,
      String scheme,
      boolean debugMode,
      ResourceManager resMgr,
      IReadAccess sysReader ) throws IOException
  {

    DependenciesManager depMgr = DependenciesManager.getInstance();
    final HashMap<String, String> tokens = new HashMap<String, String>();

    // Decide whether we're in debug mode (full-size scripts) or normal mode (minified scripts)
    final String scriptDeps = debugMode ?
        getResource( resMgr, sysReader, CdeConstants.DESIGNER_SCRIPTS_RESOURCE ) :
        depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_JS_INCLUDES ).getDependencies( true );
        
    final String styleDeps = debugMode ?
        getResource( resMgr, sysReader, CdeConstants.DESIGNER_STYLES_RESOURCE ) :
        depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_CSS_INCLUDES ).getDependencies( true );

    final String cdeDeps = depMgr.getPackage( DependenciesManager.StdPackages.CDFDD ).getDependencies( debugMode );
    tokens.put( CdeConstants.DESIGNER_HEADER_TAG, cdeDeps );
    tokens.put( CdeConstants.DESIGNER_STYLES_TAG, styleDeps );
    tokens.put( CdeConstants.DESIGNER_SCRIPTS_TAG, scriptDeps );

    try {
      final String cdfDeps = CdeEngine.getEnv().getCdfIncludes( "empty", "blueprint", debugMode, null, scheme );
      tokens.put( CdeConstants.DESIGNER_CDF_TAG, cdfDeps );
    } catch ( Exception e ) {
      logger.fatal( "Unable to get CDF dependencies", e );
    }
    tokens.put( CdeConstants.FILE_NAME_TAG, DashboardWcdfDescriptor.toStructurePath( wcdfPath ) );

    IUrlProvider urlProvider = CdeEngine.getEnv().getPluginEnv().getUrlProvider();
    final String apiPath = urlProvider.getPluginBaseUrl();
    tokens.put( CdeConstants.SERVER_URL_TAG, apiPath );
    // external api
    ICdeApiPathProvider extApi = CdeEngine.getEnv().getExtApi();
    tokens.put( CdeConstants.DATA_URL_TAG, CdeEngine.getInstance().getEnvironment().getApplicationBaseContentUrl()
        + "Syncronize" );

    tokens.put( CdeConstants.Tags.Api.RENDERER, extApi.getRendererBasePath() );
    return tokens;
  }


  private static String getProcessedEditor( String wcdfPath, ResourceManager resMgr,
      final HashMap<String, String> tokens, IReadAccess sysReader, boolean isDefault ) throws IOException {
    String cacheKey = ResourceManager.buildCacheKey( wcdfPath, tokens );
    String editorPage;
    if ( resMgr.existsInCache( cacheKey ) ) {

      editorPage = resMgr.getResourceFromCache( cacheKey );

    } else {

      if( isDefault ) {
        editorPage = Util.toString( sysReader.getFileInputStream( CdeConstants.DESIGNER_RESOURCE_DEFAULT ) );
      } else {
        editorPage = Util.toString( sysReader.getFileInputStream( CdeConstants.DESIGNER_RESOURCE ) );
      }

      if ( tokens != null && tokens.size() > 0 ) {
        for ( final String key : tokens.keySet() ) {
          editorPage = StringUtils.replace( editorPage, key, tokens.get( key ) );
        }
      }

      // We have the resource. Should we cache it?
      if ( resMgr.isCacheEnabled() ) {
        resMgr.putResourceInCache( cacheKey, editorPage );
      }
    }
    return editorPage;
  }


  private static String getResource( ResourceManager resMgr, IReadAccess sysReader, String path) throws IOException {
    final String resource;
    if(resMgr.existsInCache(path)){
      resource = resMgr.getResourceFromCache(path);
    }else{
      resource  = Util.toString(sysReader.getFileInputStream(path));
      
      if(resource != null){
        resMgr.putResourceInCache(path, resource);
      }
    }
    return resource;
  }
}