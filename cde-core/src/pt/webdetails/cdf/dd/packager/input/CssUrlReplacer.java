package pt.webdetails.cdf.dd.packager.input;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.repository.util.RepositoryHelper;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class CssUrlReplacer {

//  private static final Log logger = LogFactory.getLog(CssUrlReplacer.class);

//  private String rootPath;
  private static final Pattern thyOldeIEUrl = 
      Pattern.compile("(progid:DXImageTransform.Microsoft.AlphaImageLoader\\(src=')");
  private static final Pattern cssUrl = Pattern.compile("(url\\(['\"]?)");
  
  public CssUrlReplacer() {
  }

  private String replaceUrls( String fileContents, String newLocation ) {
    String replacement = "$1" + newLocation;
    String replacedContents = cssUrl.matcher( fileContents ).replaceAll( replacement );
    replacedContents = makeSillyIEReplacement( newLocation, replacedContents );
    return replacedContents;
  }

  /**
   * When this is removed there will be much rejoicing.
   */
  private String makeSillyIEReplacement( String newLocation, String replacedContents ) {
    String replacement = newLocation;
    // src attribute for AlphaImageLoader is relative to the page where the css will be used
    // instead of the css file itself (go microsoft!).
    // They will be on the same level for CDE/CDF's content generators,
    // so as long as the new location starts with '../' we'll be fine; //TODO: what?!
    if ( !StringUtils.startsWith( replacement, ".." ) ) {
      String firstFolder = replacement.substring( 0, replacement.indexOf( "/" ) );
      firstFolder = RepositoryHelper.appendPath( "..", firstFolder );
      replacement = RepositoryHelper.appendPath( firstFolder, replacement );
    }
    // however, the original files where this is used assume it will be referenced in the parent folder
    // hence the need for this misterious final '../'.
    // on the bright side, this will produce some humorous paths
    replacement = RepositoryHelper.appendPath( replacement, "../" );
    replacedContents = thyOldeIEUrl.matcher( replacedContents ).replaceAll( "$1" + replacement );
    return replacedContents;
  }

  public String processContents(final String fileContents, String pathDisplacement) {
    return replaceUrls(fileContents, pathDisplacement);
  }

}
