/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package pt.webdetails.cdf.dd.editor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
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
import pt.webdetails.cpf.utils.CharsetHelper;

public class DashboardEditor {

  private static Log logger = LogFactory.getLog( DashboardEditor.class );

  private static  final String WEBAPP_PATH = "#{WEBAPP_PATH}";

  public static String getEditor(
      String wcdfPath,
      boolean debugMode,
      String scheme,
      boolean isDefault) throws Exception {

    return getEditor( wcdfPath, debugMode, scheme, isDefault, false );

  }

  public static String getEditor(
      String wcdfPath,
      boolean debugMode,
      String scheme,
      boolean isDefault,
      boolean isRequire ) throws Exception {

    ResourceManager resMgr = ResourceManager.getInstance();
    IReadAccess sysReader = CdeEnvironment.getPluginSystemReader();

    final HashMap<String, String> tokens = buildReplacementTokenMap( wcdfPath, scheme, debugMode, resMgr, sysReader );

    return getProcessedEditor( wcdfPath, resMgr, tokens, sysReader, isDefault, isRequire );

  }

  private static HashMap<String, String> buildReplacementTokenMap(
      String wcdfPath,
      String scheme,
      boolean debugMode,
      ResourceManager resMgr,
      IReadAccess sysReader ) throws IOException {

    DependenciesManager depMgr = DependenciesManager.getInstance();
    final HashMap<String, String> tokens = new HashMap<String, String>();

    // Decide whether we're in debug mode (full-size scripts) or normal mode (minified scripts)
    final String scriptDeps = debugMode
        ? getResource( resMgr, sysReader, CdeConstants.DESIGNER_SCRIPTS_RESOURCE )
        : depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_JS_INCLUDES ).getDependencies( true );

    final String styleDeps = debugMode
        ? getResource( resMgr, sysReader, CdeConstants.DESIGNER_STYLES_RESOURCE )
        : depMgr.getPackage( DependenciesManager.StdPackages.EDITOR_CSS_INCLUDES ).getDependencies( true );

    final String cdeDeps = depMgr.getPackage( DependenciesManager.StdPackages.CDFDD ).getDependencies( debugMode );
    tokens.put( CdeConstants.DESIGNER_HEADER_TAG, cdeDeps );
    tokens.put( CdeConstants.DESIGNER_STYLES_TAG, styleDeps );
    tokens.put( CdeConstants.DESIGNER_SCRIPTS_TAG, scriptDeps );
    tokens.put( CdeConstants.DASHBOARD_TITLE_TAG, getDashboardTitle( wcdfPath ) );

    try {
      final String cdfDeps = CdeEngine.getEnv().getCdfIncludes( "empty", "blueprint", debugMode, false, null, scheme );
      tokens.put( CdeConstants.DESIGNER_CDF_TAG, cdfDeps );
    } catch ( Exception e ) {
      logger.fatal( "Unable to get CDF dependencies", e );
    }
    tokens.put( CdeConstants.FILE_NAME_TAG,
        URLEncoder.encode( DashboardWcdfDescriptor.toStructurePath( wcdfPath ), CharsetHelper.getEncoding() ) );

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


  private static String getProcessedEditor(
      String wcdfPath,
      ResourceManager resMgr,
      final HashMap<String, String> tokens,
      IReadAccess sysReader,
      boolean isDefault,
      boolean isRequire ) throws IOException {
    String cacheKey = ResourceManager.buildCacheKey( wcdfPath, tokens );
    String editorPage;
    if ( resMgr.existsInCache( cacheKey ) ) {

      editorPage = resMgr.getResourceFromCache( cacheKey );

    } else {

      if ( isDefault ) {
        editorPage = Util.toString( sysReader.getFileInputStream( CdeConstants.DESIGNER_RESOURCE_DEFAULT ) );
      } else {
        editorPage = Util.toString( sysReader.getFileInputStream( CdeConstants.DESIGNER_RESOURCE ) );
      }

      editorPage = processDashboardSupportTag( editorPage, isRequire );

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

  protected static String processDashboardSupportTag( String editorPage, boolean isRequire ) {
    if ( isRequire ) {
      return editorPage.replaceFirst(
        CdeConstants.DASHBOARD_SUPPORT_TAG,
        CdeConstants.DashboardSupportedTypes.AMD );
    } else {
      return editorPage.replaceFirst(
        CdeConstants.DASHBOARD_SUPPORT_TAG,
        CdeConstants.DashboardSupportedTypes.LEGACY );
    }
  }

  private static String getResource( ResourceManager resMgr, IReadAccess sysReader, String path ) throws IOException {
    final String resource;
    if ( resMgr.existsInCache( path ) ) {
      resource = resMgr.getResourceFromCache( path );
    } else {
      resource = Util.toString( sysReader.getFileInputStream( path ) );

      if ( resource != null ) {
        resMgr.putResourceInCache( path, resource );
      }
    }
    if ( resource.contains( WEBAPP_PATH ) ) {
      return resource.replace( WEBAPP_PATH,
        CdeEngine.getInstance().getEnvironment().getApplicationReposUrl()
          + CdeEngine.getInstance().getEnvironment().getPluginId() + "/" );
    }
    return resource;
  }

  private static String getDashboardTitle( String wcdfPath ) throws IOException {
    String title;

    if ( wcdfPath.isEmpty() || wcdfPath.equals( CdeConstants.LEGACY_UNSAVED_FILE_PATH ) ) {
      title = "New Dashboard";
    } else {
      DashboardWcdfDescriptor wcdf = DashboardWcdfDescriptor.load( wcdfPath );
      if ( wcdf == null ) {
        throw new FileNotFoundException( wcdfPath );
      }
      title = wcdf.getTitle();
    }

    return title;
  }
}
