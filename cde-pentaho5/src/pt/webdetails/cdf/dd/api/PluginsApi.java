package pt.webdetails.cdf.dd.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import pt.webdetails.cdf.dd.CdePlugins;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "/pentaho-cdf-dd/api/plugins" )
public class PluginsApi {

  @GET
  @Path( "/get" )
  @Produces( "text/javascript" )
  public String getCDEplugins() {
    CdePlugins plugins = new CdePlugins();
    return plugins.getCdePlugins();
  }
}
