package pt.webdetails.cdf.dd.util;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cdf.dd.datasources.IDataSourceManager;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IUserContentAccess;


/**
 * this is a simple util class that reduces the length of a call, nothing more
 * ex:
 * from
 * CdeEngine.getInstance().getEnvironment().getContentAccessFactory().getUserContentAccess(null).getFileInputStream(arg0)
 * to
 * CdeEnvironment.getUserContentAccess().getFileInputStream(arg0)
 * 
 * 
 * @author pedroteixeira
 *
 */
public abstract class CdeEnvironment {

	public static IContentAccessFactory getContentAccessFactory() {
		return CdeEngine.getInstance().getEnvironment().getContentAccessFactory();
	}

	public static IUserContentAccess getUserContentAccess(){
		return getContentAccessFactory().getUserContentAccess("/");
	}
	
	public static IReadAccess getPluginRepositoryReader(){
		return getContentAccessFactory().getPluginRepositoryReader(null);
	}
	
	public static IReadAccess getPluginRepositoryReader(String initialPath){
		return getContentAccessFactory().getPluginRepositoryReader(initialPath);
	}
	
	public static IRWAccess getPluginRepositoryWriter(){
		return getContentAccessFactory().getPluginRepositoryWriter(null);
	}
	
	public static IRWAccess getPluginRepositoryWriter(String initialPath){
		return getContentAccessFactory().getPluginRepositoryWriter(initialPath);
	}
	
	public static IReadAccess getPluginSystemReader(){
		return getContentAccessFactory().getPluginSystemReader(null);
	}
	
	public static IReadAccess getPluginSystemReader(String initialPath){
		return getContentAccessFactory().getPluginSystemReader(initialPath);
	}
	
	public static IRWAccess getPluginSystemWriter(){
		return getContentAccessFactory().getPluginSystemWriter(null);
	}
	
	public static IReadAccess getOtherPluginSystemReader(String pluginId){
		return getContentAccessFactory().getOtherPluginSystemReader(pluginId, null);
	}
	
	public static IReadAccess getOtherPluginSystemReader(String pluginId, String initialPath){
		return getContentAccessFactory().getOtherPluginSystemReader(pluginId, initialPath);
	}
	
	public static IPluginResourceLocationManager getPluginResourceLocationManager(){
		return CdeEngine.getInstance().getEnvironment().getPluginResourceLocationManager();
	}

	public static IDataSourceManager getDataSourceManager(){
		return CdeEngine.getInstance().getEnvironment().getDataSourceManager();
	}
	
	public static String getPluginRepositoryDir(){
		return CdeEngine.getInstance().getEnvironment().getPluginRepositoryDir();
	}
	
	public static String getPluginId(){
		return CdeEngine.getInstance().getEnvironment().getPluginId();
	}
	
	public static String getSystemDir(){
		return CdeEngine.getInstance().getEnvironment().getSystemDir();
	}
}

