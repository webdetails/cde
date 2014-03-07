package pt.webdetails.cdf.dd.api;

import pt.webdetails.cdf.dd.CdeSettings;
import pt.webdetails.cdf.dd.CdeVersionChecker;
import pt.webdetails.cdf.dd.util.JsonUtils;
import pt.webdetails.cpf.VersionChecker;
import pt.webdetails.cpf.VersionChecker.CheckVersionResponse;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.IOException;



/**
 * Created with IntelliJ IDEA.
 * User: diogomariano
 * Date: 07/10/13
 */

@Path( "pentaho-cdf-dd/api/version" )
public class VersionApi {

  @GET
  @Path( "/check" )
  @Produces( "text/plain" )
  public void checkVersion(@Context HttpServletResponse response) throws IOException {
    VersionChecker versionChecker = new CdeVersionChecker( CdeSettings.getSettings() );
    CheckVersionResponse result = versionChecker.checkVersion();
    JsonUtils.buildJsonResult( response.getOutputStream(), result != null, result );
  }

  @GET
  @Path( "/get" )
  @Produces( "text/plain" )
  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public String getVersion() {
    VersionChecker versionChecker = new CdeVersionChecker( CdeSettings.getSettings() );
    return versionChecker.getVersion();
  }
}
