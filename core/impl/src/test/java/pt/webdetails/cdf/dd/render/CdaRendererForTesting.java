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


package pt.webdetails.cdf.dd.render;


import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class CdaRendererForTesting extends CdaRenderer {

  private static final String CDA_DATASOURCE_DEFINITIONS = "src/test/resources/cda-resources/datasource-definitions.js";

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
