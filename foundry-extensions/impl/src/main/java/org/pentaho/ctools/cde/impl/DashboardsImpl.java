/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

package org.pentaho.ctools.cde.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import pt.webdetails.cdf.dd.reader.factory.IResourceLoader;
import pt.webdetails.cdf.dd.reader.factory.ResourceLoaderFactory;
import pt.webdetails.cdf.dd.util.GenericBasicFileFilter;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileExtended;
import pt.webdetails.cpf.repository.api.IReadAccess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class DashboardsImpl {
  private static final Log logger = LogFactory.getLog( DashboardsImpl.class );
  private IResourceLoader resourceLoader = null;
  private IReadAccess readAccess = null;
  private String path = "";
  private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

  //BACKLOG - 25778
  private static final String IGNORE_END_PATTERN = "_tmp.wcdf";

  public DashboardsImpl() {
    init();
  }

  public DashboardsImpl( IResourceLoader resourceLoader ) {
    this.resourceLoader = resourceLoader;
    init();
  }

  private void init() {
    if ( resourceLoader == null ) {
      resourceLoader = new ResourceLoaderFactory().getResourceLoader( path );
    }
    readAccess = resourceLoader.getReader();
  }

  public JSONArray getDashboardList( int maxDepth, boolean showHiddenFiles ) {

    List<IBasicFile> fileList = readAccess.listFiles( path, getFilterForCDEDashboardFiles(),
      maxDepth, false, showHiddenFiles );

    JSONArray dashboardArray = null;

    if ( fileList != null && fileList.size() > 0 ) {
      dashboardArray = new JSONArray();
      JSONObject jsonObject;
      IBasicFileExtended fileExtended;
      try {
        for ( IBasicFile file : fileList ) {
          if ( !file.getFullPath().endsWith( IGNORE_END_PATTERN ) ) {
            jsonObject = new JSONObject();
            jsonObject.put( "path", file.getFullPath() );
            jsonObject.put( "name", getNameWithoutExtension( file ) );

            /**
             * Using cpf-pentaho-rca implementation which we know
             * it returns IBasicFileExtended instead of IBasicFile
             * BACKLOG - 25778
             */
            if ( file instanceof IBasicFileExtended ) {
              fileExtended = (IBasicFileExtended) file;
              jsonObject.put( "title", fileExtended.getTitle() != null
                ? fileExtended.getTitle() : JSONObject.NULL );
              jsonObject.put( "description", fileExtended.getDescription() != null
                ? fileExtended.getDescription() : JSONObject.NULL );
              jsonObject.put( "owner", fileExtended.getOwner() != null
                ? fileExtended.getOwner() : JSONObject.NULL );
              jsonObject.put( "created", fileExtended.getCreatedDate() != null
                ? getPrettyDate( fileExtended.getCreatedDate() ) : JSONObject.NULL );
              jsonObject.put( "modified", fileExtended.getLastModifiedDate() != null
                ? getPrettyDate( fileExtended.getLastModifiedDate() ) : JSONObject.NULL );
              jsonObject.put( "size", fileExtended.getFileSize() );
            }

            dashboardArray.put( jsonObject );
          }
        }
      } catch ( JSONException jEx ) {
        logger.fatal( jEx );
      }
    }

    return dashboardArray;
  }

  private String getPrettyDate( String timeDate ) {
    Date date = new Date();
    date.setTime( Long.parseLong( timeDate ) );
    LocalDateTime localDate = LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );
    return localDate.format( dtf );
  }

  public GenericBasicFileFilter getFilterForCDEDashboardFiles() {
    return new GenericBasicFileFilter( null, "wcdf", true );
  }

  private String getNameWithoutExtension( IBasicFile file ) {
    String fileName = "";
    String fileExtension = "";
    if ( file != null ) {
      fileName = file.getName();
      fileExtension = file.getExtension();
      if ( !fileExtension.startsWith( "." ) ) {
        fileExtension = "." + fileExtension;
      }
    }
    if ( fileName != null && !fileName.equals( "" ) && fileExtension != null && !fileExtension.equals( "" ) ) {
      fileName = fileName.replace( fileExtension, "" );
    }
    return fileName;
  }

  public String getPath() {
    return path;
  }

  public void setReadAccess( IReadAccess readAccess ) {
    this.readAccess = readAccess;
  }
}
