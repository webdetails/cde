package pt.webdetails.cdf.dd.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.datasources.CdaDataSourceReader;
import pt.webdetails.cdf.dd.structure.DashboardWcdfDescriptor;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "pentaho-cdf-dd/api/datasources" )
public class DatasourcesApi {

  @GET
  @Path( "/get" )
  @Produces( "text/plain" )
  public void listCdaSources( @QueryParam( MethodParams.DASHBOARD ) String dashboard, @Context HttpServletResponse response )
    throws IOException {

    dashboard = DashboardWcdfDescriptor.toStructurePath( dashboard );

    List<CdaDataSourceReader.CdaDataSource> dataSourcesList = CdaDataSourceReader.getCdaDataSources( dashboard );
    CdaDataSourceReader.CdaDataSource[] dataSources =
        dataSourcesList.toArray( new CdaDataSourceReader.CdaDataSource[dataSourcesList.size()] );
    String result = "[" + StringUtils.join( dataSources, "," ) + "]";
    IOUtils.write( result, response.getOutputStream() );
  }

  private class MethodParams {
    public static final String DASHBOARD = "dashboard";
  }
}
