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

package pt.webdetails.cdf.dd.api;

import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import pt.webdetails.cdf.dd.datasources.CdaDataSourceReader;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;


@Path( "pentaho-cdf-dd/api/datasources" )
public class DatasourcesApi {

  @GET
  @Path( "/get" )
  @Produces( JAVASCRIPT )
  public String listCdaSources( @QueryParam( MethodParams.DASHBOARD ) @DefaultValue( "" ) String dashboard )
    throws JSONException {

    if ( dashboard.isEmpty() ) {
      return "[]";
    } else {
      dashboard = DashboardWcdfDescriptor.toStructurePath( dashboard );

      List<CdaDataSourceReader.CdaDataSource> dataSourcesList = getCdaDataSources( dashboard );
      CdaDataSourceReader.CdaDataSource[] dataSources =
        dataSourcesList.toArray( new CdaDataSourceReader.CdaDataSource[ dataSourcesList.size() ] );
      return "[" + StringUtils.join( dataSources, "," ) + "]";
    }
  }

  private class MethodParams {
    public static final String DASHBOARD = "dashboard";
  }

  protected List<CdaDataSourceReader.CdaDataSource> getCdaDataSources( String dashboard ) throws JSONException {
    return CdaDataSourceReader.getCdaDataSources( dashboard );
  }
}
