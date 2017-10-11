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

package pt.webdetails.cdf.dd.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

public class GenericBasicFileFilter implements IBasicFileFilter {

  private String fileName;
  private String[] fileExtensions;
  private boolean acceptDirectories;

  public GenericBasicFileFilter( String fileName, String fileExtension ) {
    this( fileName, fileExtension, false );
  }

  public GenericBasicFileFilter( String fileName, String fileExtension, boolean acceptDirectories ) {
    this( fileName, !StringUtils.isEmpty( fileExtension ) ? new String[] { cleanDot( fileExtension ) } : null,
      acceptDirectories );
  }

  public GenericBasicFileFilter( String fileName, String[] fileExtensions ) {
    this( fileName, fileExtensions, false );
  }

  public GenericBasicFileFilter( String fileName, String[] fileExtensions, boolean acceptDirectories ) {
    this.fileName = fileName;
    this.fileExtensions = fileExtensions;
    this.acceptDirectories = acceptDirectories;
  }

  @Override
  public boolean accept( IBasicFile file ) {

    boolean fileNameOK = false;
    boolean fileExtensionOK = false;

    if ( file != null && file.getName() != null ) {

      if ( acceptDirectories && file.isDirectory() ) {
        return true;
      }

      // file name is equal ?
      if ( !StringUtils.isEmpty( fileName ) ) {

        String fileBaseName = FilenameUtils.getBaseName( file.getName() );

        fileNameOK = fileName.equalsIgnoreCase( fileBaseName )
          || ( !fileBaseName.startsWith( "." ) && fileBaseName.endsWith( "." + fileName ) );

        //ex: component.xml
        //ex: sample.component.xml
      } else {
        fileNameOK = true; //filename was not placed as filter
      }

      if ( fileExtensions != null && fileExtensions.length > 0 ) {
        // is file extension one of the allowed extensions ?
        for ( String fileExtension : fileExtensions ) {
          if ( !StringUtils.isEmpty( fileExtension ) ) {
            fileExtensionOK = cleanDot( fileExtension ).equalsIgnoreCase( cleanDot( file.getExtension() ) );
            if ( fileExtensionOK ) {
              break; //found a match
            }
          }
        }
      } else {
        fileExtensionOK = true; //file extension was not placed as filter
      }
    }

    return fileNameOK && fileExtensionOK;
  }

  private static String cleanDot( String extension ) {
    return !StringUtils.isEmpty( extension ) && extension.startsWith( "." ) ? extension.substring( 1 ) : extension;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public String[] getFileExtensions() {
    return fileExtensions;
  }

  public void setFileExtensions( String[] fileExtensions ) {
    this.fileExtensions = fileExtensions;
  }

  public boolean isAcceptDirectories() {
    return acceptDirectories;
  }

  public void setAcceptDirectories( boolean acceptDirectories ) {
    this.acceptDirectories = acceptDirectories;
  }
}
