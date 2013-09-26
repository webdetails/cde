package pt.webdetails.cdf.dd.packager.input;

import org.apache.commons.lang.NotImplementedException;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

/**
 * Location in the repository, basePath from repository root 
 */
public class RepositoryOrigin extends PathOrigin {

  public RepositoryOrigin(String basePath) {
    super(basePath);
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getPluginRepositoryReader(RepositoryHelper.appendPath("..", basePath));
  }

  @Override
  public String getUrlPrepend() {
    // TODO 
    throw new NotImplementedException();
  }

}
