/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.cdf;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.Messages;
import pt.webdetails.cdf.dd.structure.DashboardStructureException;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cdf.dd.util.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@SuppressWarnings( "unchecked" )
public class CdfTemplates {

  private static String SYSTEM_CDF_DD_TEMPLATES = "/resources/templates";
  private String resoureUrl;
  
  private static String REPOSITORY_CDF_DD_TEMPLATES_CUSTOM = "templates";
  private static Log logger = LogFactory.getLog( CdfTemplates.class );

  public CdfTemplates( String getResourceEndpoint ) {
    this.resoureUrl = getResourceUrl( getResourceEndpoint );
  }

  public void save( String file, String structure ) throws DashboardStructureException,
    IOException {
    logger.info( "Saving File:" + file );
    IRWAccess access = CdeEnvironment.getPluginRepositoryWriter();

    if ( !access.fileExists( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM ) ) {
      access.createFolder( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM );
    }

    structure = addDashboardStyleAndRendererTypeToTemplate( structure );

    byte[] fileData = structure.getBytes( CharsetHelper.getEncoding() );
    if ( !access.saveFile( Utils.joinPath( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM, file ), new ByteArrayInputStream(
        fileData ) ) ) {
      throw new DashboardStructureException( Messages
          .getString( "DashboardStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION" ) );
    }
  }

  public Object load() {
    Object result = new JSONArray();

    try {
      GenericBasicFileFilter jsonFilter = new GenericBasicFileFilter( null, ".cdfde" );

      List<IBasicFile> defaultTemplatesList =
          CdeEnvironment.getPluginSystemReader( SYSTEM_CDF_DD_TEMPLATES ).listFiles( null, jsonFilter,
              IReadAccess.DEPTH_ALL );

      if ( defaultTemplatesList != null ) {
        loadFiles( defaultTemplatesList.toArray( new IBasicFile[] {} ), (JSONArray) result, "default" );
      } else {
        result = Messages.getString( "CdfTemplates.ERROR_002_LOADING_TEMPLATES_EXCEPTION" );
      }

      List<IBasicFile> customTemplatesList =
          CdeEnvironment.getPluginRepositoryReader( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM ).listFiles( null, jsonFilter,
              IReadAccess.DEPTH_ALL );
      if ( customTemplatesList != null ) {
        loadFiles( customTemplatesList.toArray( new IBasicFile[] {} ), (JSONArray) result, "custom" );
      }

    } catch ( IOException e ) {
      logger.error(e);
      result = Messages.getString( "CdfTemplates.ERROR_002_LOADING_EXCEPTION" );
    }
    return result;
  }

  private void loadFiles( final IBasicFile[] jsonFiles, final JSONArray result, final String type ) throws IOException {

    Arrays.sort( jsonFiles, new Comparator<IBasicFile>() {

      @Override
      public int compare( IBasicFile file1, IBasicFile file2 ) {
        if ( file1 == null && file2 == null ) {
          return 0;
        } else {
          return file1.getFullPath().toLowerCase().compareTo( file2.getFullPath().toLowerCase() );
        }
      }
    } );

    IReadAccess access = CdeEnvironment.getPluginSystemReader( SYSTEM_CDF_DD_TEMPLATES );

    for ( int i = 0; i < jsonFiles.length; i++ ) {
      final JSONObject template = new JSONObject();

      String imgResourcePath = resoureUrl+"unknown.png";

      if ( access.fileExists( jsonFiles[i].getName().replace( ".cdfde", ".png" ) ) ) {
        imgResourcePath = resoureUrl+jsonFiles[i].getName().replace( ".cdfde", ".png" );
      }

      template.put( "img", imgResourcePath );
      template.put( "type", type );
      template.put( "structure", JsonUtils.readJsonFromInputStream( jsonFiles[i].getContents() ) );
      result.add( template );
    }
  }

  /**
   * This method updates the template structure by adding to it the current dashboard's style and renderer type.
   * <p/>
   * This is done by getting the current dashboard from within the json structure, loading it's wcdfDescriptor and
   * fetching its stored style and renderer type.
   * <p/>
   * These values then are added to the template structure itself.
   * <p/>
   *
   * @param origStructure original template structure
   * @return original template structure updated to include the dashboard's style and renderer type
   * @throws DashboardStructureException
   */
  protected String addDashboardStyleAndRendererTypeToTemplate( String origStructure ) throws DashboardStructureException {

    if( origStructure == null ){
      return origStructure; // nothing to do here
    }

    try {

      String updatedStructure = origStructure;  // starts off as the original one

      JSONObject jsonObj = JSONObject.fromObject( origStructure );

      if( jsonObj != null && jsonObj.containsKey( "filename" ) ){

        DashboardWcdfDescriptor wcdf = loadWcdfDescriptor( jsonObj.getString( "filename" ) );

        if( wcdf != null ){

          // update the template structure
          jsonObj.put( "style" , wcdf.getStyle() );
          jsonObj.put( "rendererType" , wcdf.getRendererType() );

          updatedStructure = jsonObj.toString( 2 );
        }
      }

      return updatedStructure;

    } catch( Exception e ){
      logger.error( e );
      throw new DashboardStructureException( e.getMessage() );
    }
  }

  // useful to mock the DashboardWcdfDescriptor when unit testing CdfTemplates
  protected DashboardWcdfDescriptor loadWcdfDescriptor( String wcdfFile ) throws IOException {
    return DashboardWcdfDescriptor.load( wcdfFile );
  }

  // useful to mock the resource endpoint when unit testing CdfTemplates
  protected String getResourceUrl( String resourceEndpoint ){
    return CdeEngine.getInstance().getEnvironment()
      .getApplicationBaseContentUrl() + resourceEndpoint + SYSTEM_CDF_DD_TEMPLATES + "/" ;
  }
}

