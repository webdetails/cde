package pt.webdetails.cdf.dd.packager.input;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.util.RepositoryHelper;

public class PluginRepositoryOrigin extends PathOrigin {



  public PluginRepositoryOrigin(String basePath) {
    super(basePath);
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getPluginRepositoryReader(basePath);
  }

  @Override
  public String getUrlPrepend(String localPath) {
    return RepositoryHelper.joinPaths( getRepositoryBaseUrlPath(), basePath, localPath);
  }

  protected String getRepositoryBaseUrlPath() {
    return "res";
  }
}
