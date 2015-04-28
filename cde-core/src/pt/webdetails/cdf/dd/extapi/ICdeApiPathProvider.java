package pt.webdetails.cdf.dd.extapi;

public interface ICdeApiPathProvider {

  /**
   * @return abs path to renderer api, no trailing slash
   */
  public String getRendererBasePath();
  
  /**
   * @return abs path to static content access
   */
  public String getPluginStaticBaseUrl();

  /**
   * @return plugin resource url
   */
  public String getResourcesBasePath();

}
