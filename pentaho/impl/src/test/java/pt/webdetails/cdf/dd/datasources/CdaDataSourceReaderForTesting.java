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

package pt.webdetails.cdf.dd.datasources;


import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONException;
import pt.webdetails.cdf.dd.DashboardManagerWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class CdaDataSourceReaderForTesting extends CdaDataSourceReader {

  public static List<CdaDataSource> getCdaDataSources( String dashboard ) {

    JXPathContext context = null;

    try {
      context = DashboardManagerWrapper.openDashboardAsJXPathContext( dashboard, null );
    } catch ( FileNotFoundException e ) {
      return null;
    } catch ( IOException e ) {
      return null;
    } catch ( JSONException e ) {
      return null;
    }
    return getCdaDataSources( context );
  }

}
