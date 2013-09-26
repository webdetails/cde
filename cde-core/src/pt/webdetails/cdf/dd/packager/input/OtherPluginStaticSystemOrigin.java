package pt.webdetails.cdf.dd.packager.input;

import org.apache.commons.lang.NotImplementedException;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;


public class OtherPluginStaticSystemOrigin extends PathOrigin {

  private String pluginId;

  public OtherPluginStaticSystemOrigin(String pluginId, String basePath) {
    super(basePath);
    this.pluginId = pluginId;
  }

  public String getPluginId() {
    return pluginId;
  }

  @Override
  public String getUrlPrepend() {
    // FIXME need to translate to static path of another plugin
    throw new NotImplementedException();
  }

  @Override
  public IReadAccess getReader(IContentAccessFactory factory) {
    return factory.getOtherPluginSystemReader(pluginId, basePath);
  }
}
