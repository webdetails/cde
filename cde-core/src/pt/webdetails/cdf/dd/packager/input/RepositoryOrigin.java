package pt.webdetails.cdf.dd.packager.input;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * Location in the repository, basePath from repository root 
 */
public class RepositoryOrigin extends PathOrigin {

  private static String REPO_BASE = "res";//TODO: add a new one that only goes to the solution

  public RepositoryOrigin(String basePath) {
    super(basePath);
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getPluginRepositoryReader(RepositoryHelper.appendPath("..", basePath));
  }

  @Override
  public String getUrlPrepend(String localPath) {
    // XXX hcoded!
    // want it resolving to: <host>/pentaho/content/pentaho-cdf-dd/res/<basePath>/<localPath>
    return RepositoryHelper.joinPaths(REPO_BASE, basePath, localPath);
    //throw new NotImplementedException();
  }

}
