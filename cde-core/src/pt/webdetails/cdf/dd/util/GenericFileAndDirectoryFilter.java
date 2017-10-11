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

import pt.webdetails.cpf.repository.api.IBasicFile;

/**
 * this class extends on the existing GenericBasicFileFilter calculations, adding on top of it some basic directory
 * filtering
 */
public class GenericFileAndDirectoryFilter extends GenericBasicFileFilter {

  /**
   * type of directory filtering
   * <p/>
   * FILTER_IN means the directories list will act as a white-list;
   * <p/>
   * FILTER_OUT means the directories list will act as a black-list;
   * <p/>
   */
  public static enum FilterType {
    FILTER_IN, FILTER_OUT
  }

  /**
   * directories to be filtered
   */
  private String[] directories;

  /**
   * type of directory filtering; default is FILTER_IN
   */
  private FilterType filterType;

  public GenericFileAndDirectoryFilter( String fileName, String[] fileExtensions, String[] directories,
                                        FilterType filterType ) {
    this( fileName, fileExtensions, true, directories, filterType );
  }

  public GenericFileAndDirectoryFilter( String fileName, String[] fileExtensions, boolean acceptDirectories,
                                        String[] directories, FilterType filterType ) {
    super( fileName, fileExtensions, acceptDirectories );
    setDirectories( directories );
    setFilterType( filterType != null ? filterType : /* default */ FilterType.FILTER_IN );
  }

  public GenericFileAndDirectoryFilter( GenericBasicFileFilter basicFileFilter ) {
    super( basicFileFilter.getFileName(), basicFileFilter.getFileExtensions(), basicFileFilter.isAcceptDirectories() );
  }

  public GenericFileAndDirectoryFilter( GenericBasicFileFilter basicFileFilter, String[] directories,
                                        FilterType filterType ) {
    this( basicFileFilter.getFileName(), basicFileFilter.getFileExtensions(), basicFileFilter.isAcceptDirectories(),
      directories, filterType );

  }

  @Override
  public boolean accept( IBasicFile file ) {

    // superclass outcome
    boolean acceptFile = super.accept( file );

    if ( acceptFile && isAcceptDirectories() && file.isDirectory() && directories != null ) {

      boolean directoryExists = false;

      for ( String directory : directories ) {
        // check if its path is contained in the directories array
        directoryExists |= file.getFullPath().endsWith( directory );
      }

      if ( FilterType.FILTER_IN == filterType ) {
        // acceptFile = true if directory exists in the white-list, false otherwise
        acceptFile = directoryExists;

      } else if ( FilterType.FILTER_OUT == filterType ) {
        // acceptFile = false if directory exists in the black-list, true otherwise
        acceptFile = !directoryExists;
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

  public FilterType getFilterType() {
    return filterType;
  }

  public void setFilterType( FilterType filterType ) {
    this.filterType = filterType;
  }
}
