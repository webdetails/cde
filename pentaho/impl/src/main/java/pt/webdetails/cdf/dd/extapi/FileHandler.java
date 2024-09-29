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


package pt.webdetails.cdf.dd.extapi;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.PentahoCdeEnvironment;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.impl.FileContent;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

public class FileHandler implements IFileHandler {

  protected static Log logger = LogFactory.getLog( FileHandler.class );

  @Override
  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText, boolean isPreview )
    throws Exception {

    // 1. Read empty wcdf file or get original wcdf file if previewing dashboard
    InputStream wcdfFile;
    if ( isPreview ) {
      String wcdfPath = path.replace( "_tmp", "" );
      wcdfFile = Utils.getSystemOrUserRWAccess( wcdfPath ).getFileInputStream( wcdfPath );
    } else {
      wcdfFile = CdeEnvironment.getPluginSystemReader().getFileInputStream(
          DashboardStructure.SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH );

      // [CDE-130] CDE Dash saves file with name @DASHBOARD_TITLE@
      if ( CdeConstants.DASHBOARD_TITLE_TAG.equals( title ) ) {
        title = FilenameUtils.getBaseName( path );
      }
      if ( CdeConstants.DASHBOARD_DESCRIPTION_TAG.equals( description ) ) {
        description = FilenameUtils.getBaseName( path );
      }
    }

    String wcdfContentAsString = IOUtils.toString( wcdfFile, CharsetHelper.getEncoding() );

    // 2. Fill-in wcdf file title and description
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_TITLE_TAG,
        Matcher.quoteReplacement( title ) );
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_DESCRIPTION_TAG,
        Matcher.quoteReplacement( description ) );

    // 3. Publish new wcdf file
    ByteArrayInputStream bais = new ByteArrayInputStream( wcdfContentAsString.getBytes( CharsetHelper.getEncoding() ) );

    if ( isPreview ) {
      return Utils.getSystemOrUserRWAccess( path ).saveFile( path, bais );

    } else {
      FileContent file = new FileContent();
      file.setPath( path );
      file.setContents( bais );
      file.setTitle( title );
      file.setDescription( description );

      return PentahoCdeEnvironment.getInstance().getContentAccessFactory().getUserContentAccess( null )
        .saveFile( file );

    }
  }

  @Override
  /**
   * Implementation of the Basic CDE files creation; temporarily switches session to create folders as admin
   *
   * @see org.pentaho.platform.engine.security.SecurityHelper#runAsSystem()
   * @param access repositoryAccessor
   * @param file name of the basic CDE file ( widget.cdfde, widget.wcdf, widget.cda, widget.xml )
   * @param content content of the basic CDE file
   * @return operation success
   */
  public boolean createBasicFileIfNotExists( final IRWAccess access, final String file, final InputStream content ) {

    if ( access == null || StringUtils.isEmpty( file ) || content == null ) {
      return false;
    }

    // skip creation if file already exists
    if ( !access.fileExists( file ) ) {

      try {
        // current user may not have necessary create permissions; this is an admin task
        SecurityHelper.getInstance().runAsSystem( new Callable<Boolean>() {

          @Override
          public Boolean call() throws Exception {
            return access.saveFile( file, content );
          }
        } );

      } catch ( Exception e ) {
        logger.error( "Couldn't find or create CDE " + file + "  file", e );
        return false;
      }
    }

    return true;
  }

  @Override
  /**
   * Implementation of the Basic CDE folders creation; temporarily switches session to create folders as admin
   *
   * @see org.pentaho.platform.engine.security.SecurityHelper#runAsSystem()
   * @param access repositoryAccessor
   * @param relativeFolderPath name of the basic CDE folder ( styles, templates, components, wigdets )
   * @param isHidden if directory should be hidden
   * @return operation success
   */
  public boolean createBasicDirIfNotExists( final IRWAccess access, final String relativeFolderPath, final boolean isHidden ) {

    if ( access == null || StringUtils.isEmpty( relativeFolderPath ) ) {
      return false;
    }

    // skip creation if folder already exists
    if ( !access.fileExists( relativeFolderPath ) ) {

      try {
        // current user may not have necessary create permissions; this is an admin task
        SecurityHelper.getInstance().runAsSystem( new Callable<Boolean>() {

          @Override
          public Boolean call() throws Exception {
            return access.createFolder( relativeFolderPath, isHidden );
          }
        } );
      } catch ( Exception e ) {
        logger.error( "Couldn't find or create CDE " + relativeFolderPath + "  dir", e );
        return false;
      }
    }

    return true;
  }
}
