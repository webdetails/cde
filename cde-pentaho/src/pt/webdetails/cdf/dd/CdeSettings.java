/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.pentaho.SystemPluginResourceAccess;
import pt.webdetails.cpf.utils.CharsetHelper;

public class CdeSettings{
  
  protected static Log logger = LogFactory.getLog(CdeSettings.class);
  
  private CdeSettings(){}
  
  private static CdfDDSettings settings = new CdfDDSettings(CdeSettings.class);
  
  static CdfDDSettings getSettings(){
    return settings;
  }
  
  public static String[] getComponentLocations(){

    ArrayList<String> paths = new ArrayList<String>();
    CdfDDSettings settings = getSettings();
    for(Element element : settings.getComponentLocations()){
      String path = element.getText();
      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(path);
      File file = new File(solutionPath);
      if(file.exists() && file.isDirectory()){
        //files.add(file);
        paths.add(path);
      }
      else {
        logger.warn("Components directory '" + file.getAbsolutePath() + "' was not found.");
      }
    }
    return paths.toArray(new String[paths.size()]);
  }
  
  public static String getEncoding(){
    return CharsetHelper.getEncoding();
  }
  
  private static class CdfDDSettings extends PluginSettings {

	public CdfDDSettings(Class c){
		super(new SystemPluginResourceAccess(c.getClassLoader(), null));
	}
    
    List<Element> getComponentLocations(){
      return getSettingsXmlSection("custom-components/path");
    }
  }
}
