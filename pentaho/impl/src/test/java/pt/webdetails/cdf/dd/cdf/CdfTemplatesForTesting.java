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

package pt.webdetails.cdf.dd.cdf;

import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.repository.api.IBasicFile;

import java.io.IOException;

public class CdfTemplatesForTesting extends CdfTemplates {

  private String mockResourceEndpoint;

  public CdfTemplatesForTesting( String mockResourceEndpoint ) {
    super( mockResourceEndpoint );
    this.mockResourceEndpoint = mockResourceEndpoint;
    this.resourceUrl = mockResourceEndpoint;
  }

  @Override
  protected String getResourceUrl( String resourceEndpoint ) {
    return mockResourceEndpoint;
  }

  @Override
  protected JSONObject getStructure( IBasicFile file ) throws IOException, JSONException {
    return new JSONObject( "{fileName: \"" + file.getName() + "\"}" );
  }
}
