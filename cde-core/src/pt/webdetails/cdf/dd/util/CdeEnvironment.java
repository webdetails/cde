package pt.webdetails.cdf.dd.util;

import org.apache.commons.lang.StringUtils;

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
public class CdeEnvironment {

	public static IContentAccessFactory getContentAccessFactory() {
		return CdeEngine.getInstance().getEnvironment().getContentAccessFactory();
	}		
			
	public static IUserContentAccess getUserContentAccess(){
		return getContentAccessFactory().getUserContentAccess(null);
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
	
	public static IReadAccess getAppropriateReadAccess(String resource){
		
		if(StringUtils.isEmpty(resource)){
			return null;
		}
		
		String res = StringUtils.strip(resource.toLowerCase(), "/");
		
		if(res.startsWith(getSystemDir())){
		
			res = StringUtils.strip(res, getSystemDir() + "/");
			
			// system dir - this plugin
			if(res.startsWith(getPluginId())){
				return getPluginSystemReader();
				
			} else {
				// system dir - other plugin
				String pluginId = res.substring(0, resource.indexOf("/"));
				return getOtherPluginSystemReader(pluginId);
			
			}
			
		} else if(res.startsWith(getPluginRepositoryDir())) {
			
			// plugin repository dir
			return getPluginRepositoryReader();
			
		} else {
			
			// one of two: already trimmed system resource (ex: 'resources/templates/1-empty-structure.cdfde')
			// or a user solution resource (ex: 'plugin-samples/pentaho-cdf-dd/styles/my-style.css')
			
			if(getPluginSystemReader().fileExists(res)){
				return getPluginSystemReader();
			} else {
				// user solution dir
				return getUserContentAccess();
			}
		}
	}
}
			
	
