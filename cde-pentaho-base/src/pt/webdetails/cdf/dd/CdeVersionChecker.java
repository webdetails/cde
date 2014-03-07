package pt.webdetails.cdf.dd;

import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.VersionChecker;

/**
 * User: diogomariano
 * Date: 05/09/13
 */
public class CdeVersionChecker extends VersionChecker {
  private static String TRUNK_URL = "http://ci.analytical-labs.com/job/Webdetails-CDE/lastSuccessfulBuild/artifact/server/plugin/dist/marketplace.xml";
  private static String STABLE_URL = "http://ci.analytical-labs.com/job/Webdetails-CDE-Release/lastSuccessfulBuild/artifact/server/plugin/dist/marketplace.xml";

  public CdeVersionChecker(PluginSettings settings){
    super(settings);
  }

  @Override
  /**
   * @param branch Branch Url
   * @return Returns the url for the desired <code>branch</code>
   */
  protected String getVersionCheckUrl(VersionChecker.Branch branch) {
    switch (branch) {
      case TRUNK: return TRUNK_URL;
      case STABLE: return STABLE_URL;
      default: return null;
    }
  }

}
