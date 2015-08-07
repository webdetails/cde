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

package pt.webdetails.cdf.dd.cdf;


import net.sf.json.JSON;
import net.sf.json.JSONObject;
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
  protected JSON getStructure( IBasicFile file ) throws IOException {
    return JSONObject.fromObject( "{fileName: \"" + file.getName() + "\"}" );
  }
}
