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

package pt.webdetails.cdf.dd.render;


import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class CdaRendererForTesting extends CdaRenderer {

  private static final String CDA_DATASOURCE_DEFINITIONS = "test-resources/cda-resources/datasource-definitions.js";

  public CdaRendererForTesting( String docJson ) throws JSONException {
    super( docJson );
  }

  /**
   * CDA datasource definitions are fetched via inter-plugin call;
   *
   * This is overridden and mocked, so that we do not have to depend
   * on a working CDA plugin ( in the context of a cde-core test )
   */
  @Override
  public JSONObject getCdaDefinitions() {
    try {
      return new JSONObject( FileUtils.readFileToString( new File( CDA_DATASOURCE_DEFINITIONS ) )  );
    } catch ( JSONException e ) {
      return null;
    } catch ( IOException e ) {
      return null;
    }
  }

  @Override
  public boolean isValidJsonArray( JXPathContext context, String paramName ) {
    return super.isValidJsonArray( context, paramName );
  }
}
