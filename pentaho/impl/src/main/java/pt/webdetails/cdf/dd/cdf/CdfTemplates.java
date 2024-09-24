/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cdf.dd.cdf;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

@SuppressWarnings( "unchecked" )
public class CdfTemplates {

  private static String SYSTEM_CDF_DD_TEMPLATES = "/resources/templates";
  protected String resourceUrl;

  private static String REPOSITORY_CDF_DD_TEMPLATES_CUSTOM = "templates";
  private static final String DEFAULT_RENDERER_TYPE = "bootstrap";
  private static Log logger = LogFactory.getLog( CdfTemplates.class );

  public CdfTemplates( String getResourceEndpoint ) {
    this.resourceUrl = getResourceUrl( getResourceEndpoint );
  }

  public void save( String file, String structure ) throws DashboardStructureException,
    IOException {
    save( file, structure, DEFAULT_RENDERER_TYPE );
  }

  public void save( String file, String structure, String rendererType ) throws DashboardStructureException,
    IOException {
    if ( StringUtils.isEmpty( rendererType ) ) {
      rendererType = DEFAULT_RENDERER_TYPE;
    }
    logger.info( "Saving File:" + file );
    IRWAccess access = CdeEnvironment.getPluginRepositoryWriter();

    String templatesFolder = Utils.joinPath( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM, rendererType );

    if ( !access.fileExists( templatesFolder ) ) {
      access.createFolder( templatesFolder, false );
    }

    byte[] fileData = structure.getBytes( CharsetHelper.getEncoding() );
    if ( !access.saveFile( Utils.joinPath( templatesFolder, file ), new ByteArrayInputStream(
        fileData ) ) ) {
      throw new DashboardStructureException( Messages
        .getString( "DashboardStructure.ERROR_006_SAVE_FILE_ADD_FAIL_EXCEPTION" ) );
    }
  }

  public Object load() {
    return load( DEFAULT_RENDERER_TYPE );
  }

  public Object load( String rendererType ) {
    if ( StringUtils.isEmpty( rendererType ) ) {
      rendererType = DEFAULT_RENDERER_TYPE;
    }
    Object result = new JSONArray();

    try {
      GenericBasicFileFilter jsonFilter = new GenericBasicFileFilter( null, ".cdfde" );

      List<IBasicFile> defaultTemplatesList =
          CdeEnvironment.getPluginSystemReader( Utils.joinPath( SYSTEM_CDF_DD_TEMPLATES, rendererType ) )
            .listFiles( null, jsonFilter, IReadAccess.DEPTH_ALL );

      if ( defaultTemplatesList != null ) {
        loadFiles( defaultTemplatesList.toArray( new IBasicFile[] {} ), (JSONArray) result, "default", rendererType );
      } else {
        result = Messages.getString( "CdfTemplates.ERROR_002_LOADING_TEMPLATES_EXCEPTION" );
      }

      List<IBasicFile> customTemplatesList =
          CdeEnvironment.getPluginRepositoryReader( Utils.joinPath( REPOSITORY_CDF_DD_TEMPLATES_CUSTOM, rendererType ) )
            .listFiles( null, jsonFilter, IReadAccess.DEPTH_ALL );
      if ( customTemplatesList != null ) {
        loadFiles( customTemplatesList.toArray( new IBasicFile[] {} ), (JSONArray) result, "custom", rendererType );
      }

    } catch ( IOException e ) {
      logger.error( e );
      result = Messages.getString( "CdfTemplates.ERROR_002_LOADING_EXCEPTION" );
    } catch ( JSONException e ) {
      logger.error( e );
      result = Messages.getString( "CdfTemplates.ERROR_002_LOADING_EXCEPTION" );
    }
    return result;
  }

  private void loadFiles( final IBasicFile[] jsonFiles, final JSONArray result, final String type,
                          final String rendererType ) throws IOException, JSONException {

    Arrays.sort( jsonFiles, new Comparator<IBasicFile>() {

      @Override
      public int compare( IBasicFile file1, IBasicFile file2 ) {
        if ( file1 == null && file2 == null ) {
          return 0;
        } else if ( file1 == null ) {
          return 1;
        } else if ( file2 == null ) {
          return -1;
        } else {
          return file1.getFullPath().toLowerCase().compareTo( file2.getFullPath().toLowerCase() );
        }
      }
    } );

    IReadAccess access =
        CdeEnvironment.getPluginSystemReader( Utils.joinPath( SYSTEM_CDF_DD_TEMPLATES, rendererType ) );

    for ( int i = 0; i < jsonFiles.length; i++ ) {
      final JSONObject template = new JSONObject();

      String imgResourcePath = resourceUrl + "unknown.png";

      if ( access.fileExists( jsonFiles[ i ].getName().replace( ".cdfde", ".png" ) ) ) {
        imgResourcePath = resourceUrl
          + Utils.joinPath( rendererType, jsonFiles[ i ].getName().replace( ".cdfde", ".png" ) );
      }

      template.put( "img", imgResourcePath );
      template.put( "type", type );
      template.put( "structure", getStructure( jsonFiles[ i ] ) );
      result.put( template );
    }
  }

  // useful to mock the resource endpoint when unit testing CdfTemplates
  protected String getResourceUrl( String resourceEndpoint ) {
    return CdeEngine.getInstance().getEnvironment()
      .getApplicationBaseContentUrl() + resourceEndpoint + SYSTEM_CDF_DD_TEMPLATES + "/";
  }

  protected JSONObject getStructure( IBasicFile file ) throws IOException, JSONException {
    return JsonUtils.readJsonFromInputStream( file.getContents() );
  }
}

