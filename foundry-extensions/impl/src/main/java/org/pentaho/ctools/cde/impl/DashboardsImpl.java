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
import pt.webdetails.cpf.repository.api.IReadAccess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DashboardsImpl {
  private static final Log logger = LogFactory.getLog( DashboardsImpl.class );
  private IResourceLoader resourceLoader = null;
  private IReadAccess readAccess = null;
  private String path = "";

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
      try {
        for ( IBasicFile file : fileList ) {
          jsonObject = new JSONObject();
          jsonObject.put( "path", file.getFullPath() );
          jsonObject.put( "name", getNameWithoutExtension( file ) );
          dashboardArray.put( jsonObject );
        }
      } catch ( JSONException jEx ) {
        logger.fatal( jEx );
      }
    }

    return dashboardArray;
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
