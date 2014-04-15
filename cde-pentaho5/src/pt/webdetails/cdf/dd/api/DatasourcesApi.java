package pt.webdetails.cdf.dd.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;

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
  @Produces( "text/javascript" )
  public String listCdaSources( @QueryParam( MethodParams.DASHBOARD ) @DefaultValue( "" ) String dashboard ) {

    if( dashboard.isEmpty() ) {
      return "[]";
    } else {
      dashboard = DashboardWcdfDescriptor.toStructurePath( dashboard );

      List<CdaDataSourceReader.CdaDataSource> dataSourcesList =  getCdaDataSources(dashboard);
      CdaDataSourceReader.CdaDataSource[] dataSources =
              dataSourcesList.toArray( new CdaDataSourceReader.CdaDataSource[dataSourcesList.size()] );
      return "[" + StringUtils.join( dataSources, "," ) + "]";
    }
  }

  private class MethodParams {
    public static final String DASHBOARD = "dashboard";
  }
  protected List<CdaDataSourceReader.CdaDataSource> getCdaDataSources (String dashboard) {
    return CdaDataSourceReader.getCdaDataSources( dashboard );
  }
}
