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

package pt.webdetails.cdf.dd;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cdf.dd.model.core.reader.ThingReadException;
import pt.webdetails.cdf.dd.model.core.writer.ThingWriteException;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteResult;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;
import pt.webdetails.cdf.dd.util.JsonUtils;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DashboardManagerWrapper {

  private static final Log _logger = LogFactory.getLog( DashboardManager.class );

  // Cache
  private static final String CACHE_CFG_FILE = "ehcache.xml";
  private static final String CACHE_NAME = "pentaho-cde";

  public static DashboardManager getInstance() {
    return DashboardManager.getInstance();
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs( String wcdfFilePath,
      CdfRunJsDashboardWriteOptions options,
      boolean bypassCacheRead ) throws ThingWriteException {
    return getInstance().getDashboardCdfRunJs( wcdfFilePath, options, bypassCacheRead, "" );
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs(
      String wcdfFilePath,
      CdfRunJsDashboardWriteOptions options,
      boolean bypassCacheRead,
      String style )
      throws ThingWriteException {
    return getInstance().getDashboardCdfRunJs( wcdfFilePath, options, bypassCacheRead, style );
  }

  public DashboardWcdfDescriptor getPreviewWcdf( String cdfdePath )
      throws ThingWriteException {
    return getInstance().getPreviewWcdf( cdfdePath );
  }

  public CdfRunJsDashboardWriteResult getDashboardCdfRunJs(
      DashboardWcdfDescriptor wcdf,
      CdfRunJsDashboardWriteOptions options,
      boolean bypassCacheRead )
      throws ThingWriteException {
    return getInstance().getDashboardCdfRunJs( wcdf, options, bypassCacheRead );
  }

  public Dashboard getDashboard(
      String wcdfPath,
      boolean bypassCacheRead )
      throws ThingReadException {
    return getInstance().getDashboard( wcdfPath, bypassCacheRead );
  }

  public Dashboard getDashboard(
      DashboardWcdfDescriptor wcdf,
      boolean bypassCacheRead )
      throws ThingReadException {
    return getInstance().getDashboard( wcdf, bypassCacheRead );
  }

  public void invalidateDashboard( String wcdfPath ) {
    getInstance().invalidateDashboard( wcdfPath );
  }

  public void refreshAll() {
    getInstance().refreshAll( true );
  }

  public void refreshAll( boolean refreshDatasources ) {
    getInstance().refreshAll( refreshDatasources );
  }

  public static JXPathContext openDashboardAsJXPathContext(
      DashboardWcdfDescriptor wcdf )
    throws IOException, FileNotFoundException, JSONException {
    return DashboardManager.openDashboardAsJXPathContext( wcdf );
  }

  public static JXPathContext openDashboardAsJXPathContext( String dashboardLocation, DashboardWcdfDescriptor wcdf )
    throws IOException, FileNotFoundException, JSONException {
    InputStream input = null;
    String pathToFile = FilenameUtils.normalizeNoEndSeparator( System.getProperty( "user.dir" ) + dashboardLocation );
    try {
      input = new FileInputStream( pathToFile );
      final JSONObject json = JsonUtils.readJsonFromInputStream( input );

      if ( wcdf != null ) {
        json.put( "settings", wcdf.toJSON() );
      }

      return JsonUtils.toJXPathContext( json );
    } finally {
      IOUtils.closeQuietly( input );
    }
  }

}
