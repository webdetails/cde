/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cdf.dd.api;

import static pt.webdetails.cpf.utils.MimeTypes.JAVASCRIPT;

import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
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
