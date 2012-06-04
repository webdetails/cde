/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;


public abstract class PluginSettings {
  
  public static final String ENCODING = "utf-8";

  protected static Log logger = LogFactory.getLog(PluginSettings.class);
  private static IPluginManager pluginManager;
  
  public abstract String getPluginName();
  
  public String getPluginSystemDir(){
    return getPluginName() + "/";
  }
  
  protected static final String SETTINGS_FILE = "settings.xml"; 
  
  private static IPluginManager getPluginManager(){
    if(pluginManager == null){
      pluginManager = PentahoSystem.get(IPluginManager.class);
    }
    return pluginManager;
  }
  
  protected boolean getBooleanSetting(String section, boolean nullValue){
    String setting = getStringSetting(section, null);
    if(setting != null){
      return Boolean.parseBoolean(setting);
    }
    return nullValue;
  }
  
  protected String getStringSetting(String section, String defaultValue){
    return (String) getPluginManager().getPluginSetting(getPluginName(), section, defaultValue);
  }

  protected static String getSolutionPath(String path){
    return PentahoSystem.getApplicationContext().getSolutionPath(path);
  }
  
  
  
  /**
   * Writes a setting directly to .xml and refresh global config.
   * @param section
   * @param value
   * @return whether value was written
   */
  protected boolean writeSetting(String section, String value){
    Document settings = null;
    String settingsFilePath = PentahoSystem.getApplicationContext().getSolutionPath("system/" + getPluginSystemDir() + SETTINGS_FILE);
    File settingsFile = new File(settingsFilePath); 
    String nodePath = "settings/" + section;
    
    try {
      settings = XmlDom4JHelper.getDocFromFile(settingsFile, null);
    } catch (DocumentException e) {
      logger.error(e);
    } catch (IOException e) {
      logger.error(e);
    }
    if(settings != null){
      Node node = settings.selectSingleNode(nodePath);
      if(node != null){
        String oldValue = node.getText();
          node.setText(value);
          FileWriter writer = null;
          try {
            writer = new FileWriter(settingsFile);
            settings.write(writer);
            writer.flush();
            //TODO: in future should only refresh relevant cache, not the whole thing
            logger.debug("changed '" + section + "' from '" + oldValue + "' to '" + value +"'");
            PentahoSystem.refreshSettings();
            return true;
          } catch (IOException e) {
            logger.error(e);
          }
          finally {
            IOUtils.closeQuietly(writer);
          }
      }
      else {
        logger.error("Couldn't find node");
      }
    }
    else {
      logger.error("Unable to open " + settingsFilePath);
    }
    return false;    
  }

  @SuppressWarnings("unchecked")
  protected List<Element> getSettingsXmlSection(String section) {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    List<Element> elements = settings.getSystemSettings(getPluginSystemDir() + SETTINGS_FILE, section);
    return elements;
  }
  
}
