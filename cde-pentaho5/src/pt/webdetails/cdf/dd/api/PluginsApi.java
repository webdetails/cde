package pt.webdetails.cdf.dd.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;

import pt.webdetails.cdf.dd.CdePlugins;

/**
 * Created with IntelliJ IDEA. User: diogomariano Date: 07/10/13
 */
@Path( "/pentaho-cdf-dd/api/plugins" )
public class PluginsApi {

  @GET
  @Path( "/get" )
  @Produces( "text/javascript" )
  public void getCDEplugins( @Context HttpServletResponse response ) throws IOException {
    CdePlugins plugins = new CdePlugins();
    IOUtils.write( plugins.getCdePlugins(), response.getOutputStream() );
  }
}
