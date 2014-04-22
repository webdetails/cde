package pt.webdetails.cdf.dd;

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;

public abstract class DefaultCdeEnvironment extends PluginEnvironment implements ICdeEnvironment {

	private static final String PLUGIN_REPOSITORY_DIR = "cde";
	private static final String PLUGIN_ID = "pentaho-cdf-dd";
	private static final String SYSTEM_DIR = "system";
	private static final String CONTENT = "content";

  public String getPluginRepositoryDir() {
    return PLUGIN_REPOSITORY_DIR;
  }

  public String getPluginId() {
    return PLUGIN_ID;
  }

  public String getSystemDir() {
    return SYSTEM_DIR;
  }

  public String getApplicationBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/";
  }

  public String getRepositoryBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/res/";
  }

  @Override public IBasicFile getCdeXml() {
    return null;
  }
}
