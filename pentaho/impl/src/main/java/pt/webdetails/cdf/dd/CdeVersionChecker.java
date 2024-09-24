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

package pt.webdetails.cdf.dd;

import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.VersionChecker;

public class CdeVersionChecker extends VersionChecker {
  private static String TRUNK_URL =
    "http://ci.analytical-labs.com/job/Webdetails-CDE/lastSuccessfulBuild/artifact/server/plugin/dist/marketplace.xml";
  private static String STABLE_URL =
    "http://ci.analytical-labs.com/job/Webdetails-CDE-Release/lastSuccessfulBuild/artifact/server/plugin/dist"
      + "/marketplace.xml";

  public CdeVersionChecker( PluginSettings settings ) {
    super( settings );
  }

  @Override
  /**
   * @param branch Branch Url
   * @return Returns the url for the desired <code>branch</code>
   */
  protected String getVersionCheckUrl( VersionChecker.Branch branch ) {
    switch( branch ) {
      case TRUNK: return TRUNK_URL;
      case STABLE: return STABLE_URL;
      default: return null;
    }
  }

}
