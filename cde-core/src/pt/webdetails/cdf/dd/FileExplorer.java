/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

public class FileExplorer
{

  private static FileExplorer fileExplorer = null;

  static FileExplorer getInstance()
  {
    if (fileExplorer == null)
    {
      fileExplorer = new FileExplorer();
    }
    return fileExplorer;
  }

  public String getJqueryFileTree(final String dir, final String fileExtensions, final String access)
  {
	  return CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getJqueryFileTree(dir, fileExtensions, access);
  }

  public String getJSON(final String dir, final String fileExtensions, final String access)
  {
	  return CdeEngine.getInstance().getEnvironment().getRepositoryAccess().getJSON(dir, fileExtensions, access);
  }
}
