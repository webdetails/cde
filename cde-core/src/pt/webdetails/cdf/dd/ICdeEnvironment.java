package pt.webdetails.cdf.dd;

import java.util.Locale;

import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cdf.dd.extapi.ICdeApiPathProvider;
import pt.webdetails.cdf.dd.extapi.IFileHandler;
import pt.webdetails.cdf.dd.model.core.writer.IThingWriterFactory;
import pt.webdetails.cdf.dd.model.inst.Dashboard;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteContext;
import pt.webdetails.cdf.dd.model.inst.writer.cdfrunjs.dashboard.CdfRunJsDashboardWriteOptions;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.resources.IResourceLoader;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;


public interface ICdeEnvironment {

  public void init( ICdeBeanFactory factory ) throws InitializationException;

  public void refresh();

  public String getApplicationBaseUrl();

  public Locale getLocale();

  // public IPluginCall getInterPluginCall();

  public IResourceLoader getResourceLoader();

  public IDataSourceManager getDataSourceManager();

  public IPluginResourceLocationManager getPluginResourceLocationManager();

  public IContentAccessFactory getContentAccessFactory();

  public String getPluginRepositoryDir();

  public String getSystemDir();

  public String getPluginId();

  PluginEnvironment getPluginEnv();

  public ICdeApiPathProvider getExtApi();

  /**
   * TODO: replace with urlprovider 
   * @return Base content URL <u>for this plugin</u>
   */
  public String getApplicationBaseContentUrl();

  public String getRepositoryBaseContentUrl();

  String getCdfIncludes( String dashboard, String type, boolean debug, String absRoot, String scheme) throws Exception;

  //String getCdfContext( String dashboard, String action, String viewId ) throws Exception;
  
  public IFileHandler getFileHandler();

  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext(IThingWriterFactory factory, String indent,
      boolean bypassCacheRead, Dashboard dash, CdfRunJsDashboardWriteOptions options);

  public CdfRunJsDashboardWriteContext getCdfRunJsDashboardWriteContext(CdfRunJsDashboardWriteContext factory, String indent);

  public IBasicFile getCdeXml();
}
