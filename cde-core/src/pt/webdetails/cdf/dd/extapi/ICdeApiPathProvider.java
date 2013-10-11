package pt.webdetails.cdf.dd.extapi;

public interface ICdeApiPathProvider {

  /**
   * @return abs path to renderer api, no trailing slash
   */
  public String getRendererBasePath();

}
