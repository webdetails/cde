/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.packager.input;

import java.util.regex.Pattern;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

public class CssUrlReplacer {

  private static final Pattern thyOldeIEUrl = 
      Pattern.compile("(progid:DXImageTransform.Microsoft.AlphaImageLoader\\(src=')");
  // find a quoted url not starting with /
  private static final Pattern cssUrl = Pattern.compile("(url\\(['\"])([^/])");
  
  public CssUrlReplacer() {
  }

  private String replaceUrls( String fileContents, String newLocation ) {
    String replacement = "$1" + newLocation + "$2" ;
    String replacedContents = cssUrl.matcher( fileContents ).replaceAll( replacement );
    replacedContents = makeSillyIEReplacement( newLocation, replacedContents );
    return replacedContents;
  }

  /**
   * When this is removed there will be much rejoicing.
   */
  private String makeSillyIEReplacement( String newLocation, String replacedContents ) {
    // src attribute for AlphaImageLoader is relative to the page where the css will be used
    // instead of the css file itself (go microsoft!).
    // the original files where this is used assume it will be referenced in the parent folder
    // hence the need for this misterious final '../'.
    // on the bright side this will produce some humorous paths
    String replacement = RepositoryHelper.appendPath( newLocation, "../" );
    replacedContents = thyOldeIEUrl.matcher( replacedContents ).replaceAll( "$1" + replacement );
    return replacedContents;
  }

  public String processContents(final String fileContents, String location) {
    return replaceUrls(fileContents, location);
  }

}
