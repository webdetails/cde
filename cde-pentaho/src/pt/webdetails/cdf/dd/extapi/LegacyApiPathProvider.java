package pt.webdetails.cdf.dd.extapi;

import org.apache.commons.lang.StringUtils;

import pt.webdetails.cpf.context.api.IUrlProvider;

public class LegacyApiPathProvider implements ICdeApiPathProvider {

  // in 4.x there is only ContentGenerator
  private String pluginPath;

  public LegacyApiPathProvider ( IUrlProvider urlProvider ) {
    this.pluginPath = StringUtils.removeEnd( urlProvider.getPluginBaseUrl(), "/");
  }

  @Override
  public String getRendererBasePath( ) {
    return pluginPath;
  }

}
