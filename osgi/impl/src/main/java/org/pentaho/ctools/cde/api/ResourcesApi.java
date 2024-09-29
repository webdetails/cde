/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.ctools.cde.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.utils.MimeTypes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.io.IOException;

@Path( "resources" )
public class ResourcesApi {
  private static final Log logger = LogFactory.getLog( ResourcesApi.class );

  private String resourceMaxAge;

  public void setResourceMaxAge( String maxAge ) {
    this.resourceMaxAge = maxAge;
  }

  public String getResourceMaxAge() {
    return this.resourceMaxAge;
  }

  @GET
  @Path( "/{resource: [^?]+ }" )
  public Response resource( @PathParam( "resource" ) String resource ) throws IOException {
    IBasicFile file = getFile( resource );
    if ( file == null ) {
      logger.error( "resource not found:" + resource );

      return Response.serverError().build();
    }

    final String filename = file.getName();

    Response.ResponseBuilder response = Response.ok( file.getContents() );

    response
      .header( "Content-Type", getResourceMimeType( filename ) )
      .header( "content-disposition", "inline; filename=\"" + filename + "\"" );

    String maxAge = getResourceMaxAge();
    if ( maxAge != null ) {
      response.header( "Cache-Control", "max-age=" + maxAge );
    }

    return response.build();
  }

  IBasicFile getFile( String path ) {
    return Utils.getFileViaAppropriateReadAccess( path );
  }

  private String getResourceMimeType( String filename ) {
    return MimeTypes.getMimeType( filename );
  }

}
