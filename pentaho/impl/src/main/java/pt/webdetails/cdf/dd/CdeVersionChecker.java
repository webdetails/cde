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
