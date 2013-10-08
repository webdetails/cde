package pt.webdetails.cdf.dd.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONException;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.olap.OlapUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "pentaho-cdf-dd/api/olap" )
public class OlapApi {
  private static final Log logger = LogFactory.getLog( SyncronizerApi.class );

  @GET
  @Path( "/getCubes" )
  @Produces( "text/javascript" )
  public void getCubes( @QueryParam( MethodParams.CATALOG ) String catalog, @Context HttpServletRequest request,
      @Context HttpServletResponse response ) throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getOlapCubes();
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getCubeStructure" )
  @Produces( "text/javascript" )
  public void getCubeStructure( @QueryParam( MethodParams.CATALOG ) String catalog,
      @QueryParam( MethodParams.CUBE ) String cube, @QueryParam( MethodParams.JNDI ) String jndi,
      @Context HttpServletResponse response ) throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getCubeStructure( catalog, cube, jndi );
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/getLevelMembersStructure" )
  @Produces( "text/javascript" )
  public void getLevelMembersStructure( @QueryParam( MethodParams.CATALOG ) String catalog,
      @QueryParam( MethodParams.CUBE ) String cube, @QueryParam( MethodParams.MEMBER ) String member,
      @QueryParam( MethodParams.DIRECTION ) String direction, @Context HttpServletResponse response )
    throws IOException, JSONException {
    OlapUtils olapUtils = new OlapUtils();
    JSONObject result = olapUtils.getLevelMembersStructure( catalog, cube, member, direction );
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  private class MethodParams {
    public static final String CATALOG = "catalog";
    public static final String CUBE = "cube";
    public static final String JNDI = "jndi";
    public static final String MEMBER = "member";
    public static final String DIRECTION = "direction";
  }
}
