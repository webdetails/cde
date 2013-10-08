/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import pt.webdetails.cdf.dd.packager.PathOrigin;
import pt.webdetails.cdf.dd.packager.input.PluginRepositoryOrigin;
import pt.webdetails.cdf.dd.packager.input.StaticSystemOrigin;
import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class CdeSettings {
  
  protected static Log logger = LogFactory.getLog(CdeSettings.class);
  
  private CdeSettings(){}
  
  private static CdfDDSettings settings = new CdfDDSettings();
  
  public static CdfDDSettings getSettings(){
    return settings;
  }
  
  //<path origin="psystem"><>
//  public LocationSet getComponentLocations() {
//    
//  }
  public static Collection<PathOrigin> getCustomComponentLocations() {
    return settings.getComponentLocations();
  }

  /**
   * @deprecated
   */
  public static IReadAccess[] getComponentLocations(){

    ArrayList<IReadAccess> componentAccesses = new ArrayList<IReadAccess>();
    CdfDDSettings settings = getSettings();
    for(Element element : settings.getComponentLocationElements()){
      String path = element.getText();

      if (path != null) {

    	  path = StringUtils.strip(path, "/");
    	  
    	  //ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>, <path>system/cdc/cdeComponents</path>
	      if(path.startsWith(CdeEnvironment.getSystemDir() + "/")){
	    	  
	    	  path = path.replaceFirst(CdeEnvironment.getSystemDir() + "/", "");

          // ex: <path>system/pentaho-cdf-dd/resources/custom/components</path>
	    	  if(path.startsWith(CdeEnvironment.getPluginId() + "/")){
	    		  
            path = path.replaceFirst(CdeEnvironment.getPluginId() + "/", "");

            if (CdeEnvironment.getPluginSystemReader().fileExists(path) && CdeEnvironment.getPluginSystemReader().fetchFile(path).isDirectory()) {
              componentAccesses.add(CdeEnvironment.getPluginSystemReader(path));
            }

          } else {
            //XXX this isn't supposed to happen
            // ex: <path>system/cdc/cdeComponents</path>
            String pluginId = path.substring(0, path.indexOf("/"));
            path = path.replaceFirst(pluginId + "/", "");

            if (CdeEnvironment.getOtherPluginSystemReader(pluginId).fileExists(path) && CdeEnvironment.getOtherPluginSystemReader(pluginId).fetchFile(path).isDirectory()) {
              componentAccesses.add(CdeEnvironment.getOtherPluginSystemReader(pluginId, path));
            }

          }

	      } else if(path.startsWith(CdeEnvironment.getPluginRepositoryDir() + "/")){

          // ex: <path>cde/components</path>
          path = path.replaceFirst(CdeEnvironment.getPluginRepositoryDir() + "/", "");

          if (CdeEnvironment.getPluginSystemReader().fileExists(path) && CdeEnvironment.getPluginSystemReader().fetchFile(path).isDirectory()) {
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

    public CdfDDSettings(){
      super(CdeEnvironment.getPluginSystemWriter());
    }
    
    List<Element> getComponentLocationElements(){
      return getSettingsXmlSection("custom-components/path");
    }

    public List<PathOrigin> getComponentLocations() {
      List<Element> pathElements = getSettingsXmlSection("custom-components/path");
      ArrayList<PathOrigin> locations = new ArrayList<PathOrigin>();
      for (Element pathElement : pathElements) {
        String path = StringUtils.strip(pathElement.getTextTrim());
        String origin = pathElement.attributeValue("origin");
        if(!StringUtils.isEmpty(origin)) {
          if (StringUtils.equals(origin, "static")) {
            locations.add(new StaticSystemOrigin(path)); 
          }
          else if (StringUtils.equals(origin, "repo")) {
            locations.add(new PluginRepositoryOrigin(path));
          }
        }
        else {
          logger.error("Must specify origin (static|repo), location '" + path + " 'ignored.");
        }
      }
      return locations;
    }

  }

}
