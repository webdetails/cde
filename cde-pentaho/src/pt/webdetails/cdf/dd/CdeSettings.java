/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.pentaho.SystemPluginResourceAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class CdeSettings{
  
  protected static Log logger = LogFactory.getLog(CdeSettings.class);
  
  private CdeSettings(){}
  
  private static CdfDDSettings settings = new CdfDDSettings(CdeSettings.class);
  
  static CdfDDSettings getSettings(){
    return settings;
  }
  
  public static IReadAccess[] getComponentLocations(){

    ArrayList<IReadAccess> componentAccesses = new ArrayList<IReadAccess>();
    CdfDDSettings settings = getSettings();
    for(Element element : settings.getComponentLocations()){
      String path = element.getText();
      
      if(path != null){
      
    	  path = StringUtils.strip(path, "/");
    	  
    	  //ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>, <path>system/cdc/cdeComponents</path>
	      if(path.startsWith(CdeEnvironment.getSystemDir() + "/")){
	    	  
	    	  path = path.replaceFirst(CdeEnvironment.getSystemDir() + "/", "");
	    	  
	    	  //ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>
	    	  if(path.startsWith(CdeEnvironment.getPluginId() + "/")){
	    		  
	    		  path = path.replaceFirst(CdeEnvironment.getPluginId() + "/", "");

	    		  if(CdeEnvironment.getPluginSystemReader().fileExists(path) && CdeEnvironment.getPluginSystemReader().fetchFile(path).isDirectory()){
	    			  componentAccesses.add(CdeEnvironment.getPluginSystemReader(path));
	    		  }
	    		  
	    	  }else{
	    		
	    		//ex: <path>system/cdc/cdeComponents</path>
	    		String pluginId = path.substring(0, path.indexOf("/"));
	    		path = path.replaceFirst(pluginId + "/", "");
	    		
	    		if(CdeEnvironment.getOtherPluginSystemReader(pluginId).fileExists(path) && CdeEnvironment.getOtherPluginSystemReader(pluginId).fetchFile(path).isDirectory()){
	    			componentAccesses.add(CdeEnvironment.getOtherPluginSystemReader(pluginId, path));
	    		}
	    		
	    	  }
	    	  
	      } else if(path.startsWith(CdeEnvironment.getPluginRepositoryDir() + "/")){
	    	  
	    	  //ex: <path>cde/components</path>
	    	  path = path.replaceFirst(CdeEnvironment.getPluginRepositoryDir() + "/", "");
	    	  
	    	  if(CdeEnvironment.getPluginRepositoryReader().fileExists(path) && CdeEnvironment.getPluginRepositoryReader().fetchFile(path).isDirectory()){
	    		  componentAccesses.add(CdeEnvironment.getPluginRepositoryReader(path));
	    	  }
	      } else {
	    	  logger.warn("Components directory '" + element.getText() + "' was not found.");
	      }
      }
    }
    return componentAccesses.toArray(new IReadAccess[componentAccesses.size()]);
  }
  
  public static String getEncoding(){
    return CharsetHelper.getEncoding();
  }
  
  private static class CdfDDSettings extends PluginSettings {

	public CdfDDSettings(@SuppressWarnings("rawtypes") Class c){
		super(new SystemPluginResourceAccess(c.getClassLoader(), null));
	}
    
    List<Element> getComponentLocations(){
      return getSettingsXmlSection("custom-components/path");
    }
  }
}
