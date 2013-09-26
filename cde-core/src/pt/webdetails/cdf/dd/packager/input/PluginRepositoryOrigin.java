package pt.webdetails.cdf.dd.packager.input;

import org.apache.commons.lang.NotImplementedException;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class PluginRepositoryOrigin extends PathOrigin {

  public PluginRepositoryOrigin(String basePath) {
    super(basePath);
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getPluginRepositoryReader(basePath);
  }

  @Override
  public String getUrlPrepend() {
    // TODO 
    throw new NotImplementedException();
  }

}
