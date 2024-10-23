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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import pt.webdetails.cdf.dd.CdePlugins;

@Path( "/pentaho-cdf-dd/api/plugins" )
public class PluginsApi {

  @GET
  @Path( "/get" )
  @Produces( JAVASCRIPT )
  public String getCDEplugins() {
    CdePlugins plugins = new CdePlugins();
    return plugins.getCdePlugins();
  }
}
