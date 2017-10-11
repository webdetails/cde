/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package pt.webdetails.cdf.dd.extapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.structure.DashboardStructure;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.repository.api.IRWAccess;

public class LegacyFileHandler implements IFileHandler {

  @Override
  public boolean saveDashboardAs( String path, String title, String description,
                                  String cdfdeJsText, boolean isPreview ) throws Exception {

    // 1. Read empty wcdf file or get original wcdf file if previewing dashboard
    InputStream wcdfFile;
    if ( isPreview ) {
      String wcdfPath = path.replace( "_tmp", "" );
      wcdfFile = CdeEnvironment.getUserContentAccess().getFileInputStream( wcdfPath );
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
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_TITLE_TAG, title );
    wcdfContentAsString = wcdfContentAsString.replaceFirst( CdeConstants.DASHBOARD_DESCRIPTION_TAG, description );

    // 3. Publish new wcdf file

    ByteArrayInputStream bais = new ByteArrayInputStream( wcdfContentAsString.getBytes( CharsetHelper.getEncoding() ) );

    return CdeEnvironment.getUserContentAccess().saveFile( path, bais );
  }

  @Override
  public boolean createBasicFileIfNotExists( final IRWAccess access, final String file, final InputStream content ) {

    if ( access == null || StringUtils.isEmpty( file ) || content == null ) {
      return false;
    }

    // skip creation if file already exists
    if ( !access.fileExists( file ) ) {
      access.saveFile( file, content );
    }

    return true;
  }

  @Override
  public boolean createBasicDirIfNotExists( final IRWAccess access, final String relativeFolderPath, boolean isHidden ) {

    if ( access == null || StringUtils.isEmpty( relativeFolderPath ) ) {
      return false;
    }

    // skip creation if dir already exists
    if ( !access.fileExists( relativeFolderPath ) ) {
      access.createFolder( relativeFolderPath, isHidden );
    }

    return true;
  }
}
