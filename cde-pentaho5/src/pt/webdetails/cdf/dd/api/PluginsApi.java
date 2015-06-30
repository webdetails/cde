/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import pt.webdetails.cdf.dd.CdePlugins;

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
