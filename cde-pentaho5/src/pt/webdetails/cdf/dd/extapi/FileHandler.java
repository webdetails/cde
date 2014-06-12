/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/
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

public class FileHandler implements IFileHandler {

  protected static Log logger = LogFactory.getLog( FileHandler.class );

  @Override
  public boolean saveDashboardAs( String path, String title, String description, String cdfdeJsText, boolean isPreview )
    throws Exception {

    // 1. Read empty wcdf file
    InputStream wcdfFile =
      CdeEnvironment.getPluginSystemReader().getFileInputStream(
        DashboardStructure.SYSTEM_PLUGIN_EMPTY_WCDF_FILE_PATH );

    String wcdfContentAsString = IOUtils.toString( wcdfFile, CharsetHelper.getEncoding() );

    // [CDE-130] CDE Dash saves file with name @DASHBOARD_TITLE@
    if ( CdeConstants.DASHBOARD_TITLE_TAG.equals( title ) ) {
      title = FilenameUtils.getBaseName( path );
    }
    if ( CdeConstants.DASHBOARD_DESCRIPTION_TAG.equals( description ) ) {
      description = FilenameUtils.getBaseName( path );
    }

    // 2. Fill-in wcdf file title and description
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_TITLE_TAG, title );
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_DESCRIPTION_TAG, description );

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
  public boolean ensureFileExists( final IRWAccess access, final String file, final InputStream content ) {

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
  public boolean ensureDirExists( final IRWAccess access, final String relativeFolderPath ) {

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
            return access.createFolder( relativeFolderPath );
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
