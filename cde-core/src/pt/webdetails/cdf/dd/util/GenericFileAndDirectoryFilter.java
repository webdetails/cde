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
package pt.webdetails.cdf.dd.util;

import pt.webdetails.cpf.repository.api.IBasicFile;

/**
 * this class extends on the existing GenericBasicFileFilter calculations,
 * adding on top of it some basic directory filtering
 */
public class GenericFileAndDirectoryFilter extends GenericBasicFileFilter {

  /**
   * directories to be filtered out
   */
  private String[] directories;

  public GenericFileAndDirectoryFilter( String fileName, String[] fileExtensions, String[] directories ) {
    this( fileName, fileExtensions, true, directories );
  }

  public GenericFileAndDirectoryFilter( String fileName, String[] fileExtensions, boolean acceptDirectories,
                                        String[] directories ) {
    super( fileName, fileExtensions, acceptDirectories );
    setDirectories( directories );
  }

  public GenericFileAndDirectoryFilter( GenericBasicFileFilter basicFileFilter ) {
    super( basicFileFilter.getFileName(), basicFileFilter.getFileExtensions(), basicFileFilter.isAcceptDirectories() );
  }

  public GenericFileAndDirectoryFilter( GenericBasicFileFilter basicFileFilter, String[] directories ) {
    super( basicFileFilter.getFileName(), basicFileFilter.getFileExtensions(), basicFileFilter.isAcceptDirectories() );
    setDirectories( directories );
  }

  @Override
  public boolean accept( IBasicFile file ) {

    // superclass outcome
    boolean acceptFile = super.accept( file );

    if ( acceptFile && isAcceptDirectories() && file.isDirectory() && directories != null ) {

      for ( String directory : directories ) {

        // filter out this folder if its path is contained in the directories array
        if ( file.getFullPath().endsWith( directory ) ) {
          return false;
        }
      }
    }

    return acceptFile;
  }

  public String[] getDirectories() {
    return directories;
  }

  public void setDirectories( String[] directories ) {
    this.directories = directories;
  }
}