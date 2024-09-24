/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
