/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.dependencies;

/**
 * A raw code snippet. 
 */
public class SnippetDependency extends Dependency
{

  private String content;

  public SnippetDependency(String version, String contents)
  {
    super(version);
    this.content = contents;
  }

  /**
   * @return raw snippet
   */
  public String getDependencyInclude()
  {
    return content;
  }

  @Override
  public String getContents() {
    return content;
  }
}
