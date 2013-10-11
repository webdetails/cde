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
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@SuppressWarnings( "unchecked" )
public class CdfTemplates {

  private static String SYSTEM_CDF_DD_TEMPLATES = "/resources/templates";
  public static String SYSTEM_RESOURCE_TEMPLATE_DIR = CdeEngine.getInstance().getEnvironment()
      .getApplicationBaseContentUrl() + "api/resources/get?resource=" + SYSTEM_CDF_DD_TEMPLATES + "/" ;
  public static String UNKNOWN_IMAGE = SYSTEM_RESOURCE_TEMPLATE_DIR + "unknown.png";
  
  private static String REPOSITORY_CDF_DD_TEMPLATES_CUSTOM = "/templates";
  private static Log logger = LogFactory.getLog( CdfTemplates.class );

  public CdfTemplates() {
  }

  public void save( String file, String structure ) throws DashboardStructureException,
    IOException {
    logger.info( "Saving File:" + file );
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();

    if ( !access.fileExists( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM ) ) {
      access.createFolder( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM );
    }

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

    IReadAccess access = CdeEnvironment.getPluginSystemReader( SYSTEM_RESOURCE_TEMPLATE_DIR );

    for ( int i = 0; i < jsonFiles.length; i++ ) {
      final JSONObject template = new JSONObject();

      String imgResourcePath = UNKNOWN_IMAGE;

      if ( access.fileExists( jsonFiles[i].getName().replace( ".cdfde", ".png" ) ) ) {
        imgResourcePath = jsonFiles[i].getFullPath().replace( ".cdfde", ".png" );
      }

      template.put( "img", imgResourcePath );
      template.put( "type", type );
      template.put( "structure", JsonUtils.readJsonFromInputStream( jsonFiles[i].getContents() ) );
      result.add( template );
    }
  }
}
