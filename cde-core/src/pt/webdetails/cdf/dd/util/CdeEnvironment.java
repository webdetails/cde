package pt.webdetails.cdf.dd.util;

import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cdf.dd.IPluginResourceLocationManager;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class CdeEnvironment {

	private static IContentAccessFactory getContentAccessFactory() {
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
	
	public static IReadAccess getPluginSystemReader(){
		return getContentAccessFactory().getPluginSystemReader(null);
	}
	
	public static IReadAccess getPluginSystemReader(String initialPath){
		return getContentAccessFactory().getPluginSystemReader(initialPath);
	}
	
	public static IRWAccess getPluginSystemWriter(){
		return getContentAccessFactory().getPluginSystemWriter(null);
	}
	
	public static IPluginResourceLocationManager getPluginResourceLocationManager(){
		return CdeEngine.getInstance().getEnvironment().getPluginResourceLocationManager();
	}
}
			
	
