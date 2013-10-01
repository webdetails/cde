/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

import java.io.IOException;


public abstract class Dependency
{
  protected String version;

  protected Dependency(String version)
  {
    this.version = version;
  }

  public boolean isOlderVersionThan(Dependency other) {
    // assuming version numberings always increase lexicographically
    return this.version == null || this.version.compareTo( other.version ) < 0;
  }

  //TODO: does it make sense to have the same for both?
  abstract public String getDependencyInclude();

  abstract public String getContents() throws IOException;
}
