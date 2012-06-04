/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cpf;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.json.JSONException;
import org.json.JSONObject;


import pt.webdetails.cpf.repository.RepositoryAccess;
import pt.webdetails.cpf.repository.RepositoryAccess.FileAccess;


/**
 * Version checker for a standard marketplace plugin.
 * Checks the local version from version.xml in the plugin folder.
 * 
 * 
 */
public abstract class VersionChecker {
  
  protected Log logger = LogFactory.getLog(this.getClass());
  protected PluginSettings settings;
  
  public VersionChecker(PluginSettings pluginSettings){
    settings = pluginSettings;
  }

  /* ******************
   * Abstract methods */
  
  /**
   * @param branch The branch to check
   * @return The URL for the XML version file of the latest release in this <code>branch</code>
   */
  protected abstract String getVersionCheckUrl(Branch branch);
  
  
  /* abstract methods *
   ********************/
 
  private static String[] branches;
  
  static {
    ArrayList<String> branchList = new ArrayList<String>();
    for(Branch branch: Branch.values()){
      branchList.add(branch.toString());
    }
    branches = branchList.toArray(new String[branchList.size()]);
  }
  
  
  /* ****************
   * Public methods */
  
  public String[] getBranches(){
    return branches;
  }
  
  public CheckVersionResponse checkVersion() {
    
    //get installed version
    Version installed = null;
    try {
      Document versionXml = RepositoryAccess.getRepository().getResourceAsDocument("system/" + settings.getPluginSystemDir() + "version.xml", FileAccess.NONE);
      installed = new Version(versionXml);
    } catch (Exception e) {
      String msg = "Error attempting to read version.xml";
      logger.error(msg, e);
      return new CheckVersionResponse(CheckVersionResponse.Type.ERROR, msg);
    }

    String url = getVersionCheckUrl(installed.getBranch());
    
    if(url == null){
      String msg ="No URL found for this version."; 
      logger.info(msg);
      return new CheckVersionResponse(CheckVersionResponse.Type.INCONCLUSIVE, msg);
    }
    
    Version latest = null;
    try {
      SAXReader reader = new SAXReader();
      Document versionXml = reader.read(url);
      latest = new Version(versionXml);
    }
    catch(DocumentException e){
      String msg = "Could not parse remote file " + url; 
      logger.info(msg, e);
      return new CheckVersionResponse(CheckVersionResponse.Type.ERROR, msg);
    }

    if(installed.isSuperceededBy(latest)){
      return new CheckVersionResponse(CheckVersionResponse.Type.UPDATE, latest.downloadUrl);
    }
    else {
      return new CheckVersionResponse(CheckVersionResponse.Type.LATEST, null);
    }
    
  }
  
  public String getVersion(){
    Version installed = null;
    try {
      Document versionXml = RepositoryAccess.getRepository().getResourceAsDocument("system/" + settings.getPluginSystemDir() + "version.xml", FileAccess.NONE);
      installed = new Version(versionXml);
      return installed.toString(); //getShortVersion();
    } catch (Exception e) {
      String msg = "Error attempting to read version.xml";
      logger.error(msg, e);
      return "unknown version";
    }
  }
  
  /* public methods *
   ******************/
  
  public static class CheckVersionResponse implements JsonSerializable {
    
    
    public CheckVersionResponse(Type responseType, String message){
      type = responseType;
      msg = message;
    }
    
    public enum Type {
      LATEST,
      UPDATE,
      INCONCLUSIVE,
      ERROR
    }
    
    private Type type;
    private String msg;
    
    public String getMessage() { return msg; }
    public Type getType(){return type;}
    
    @Override
    public JSONObject toJSON() throws JSONException {
      JSONObject obj = new JSONObject();
      obj.put("result", type.toString().toLowerCase());
      switch(type){
        case LATEST:
        case INCONCLUSIVE:
        case ERROR:
          obj.put("msg", msg);
          break;
        case UPDATE:
          obj.put("downloadUrl",msg);
          break;
      }
      
      return obj;
    }
    
  }
  
  public enum Branch {
    STABLE,
    TRUNK,
    LOCAL,
    UNKNOWN;
  }
  
  public static class Version {
    
    
    private String branchStr;
    private String version;
    private String buildId;
    private String downloadUrl;
    
    private Branch branch;
    
    public Version(Document xml){
      
      if(xml == null){
        throw new IllegalArgumentException("no document");
      }
      
      Node versionNode = xml.selectSingleNode("//version"); 
      
      branchStr = getStringValue(versionNode, "@branch", branchStr);
      version = getStringValue(versionNode, "/version", version);
      version = getStringValue(versionNode, "version", version);
      buildId = getStringValue(versionNode, "buildId", buildId);
      buildId = getStringValue(versionNode, "@buildId", buildId);
      downloadUrl = getStringValue(versionNode, "downloadUrl", null);
      
//      if(StringUtils)//TODO:check if parse was valid
    }

    public Branch getBranch(){
      
      if(this.branch == null){
        if(StringUtils.equals(branchStr, "TRUNK")){
          if(StringUtils.startsWithIgnoreCase(buildId, "manual")){
            return Branch.LOCAL;
          }
          else return Branch.TRUNK;
        }
        else if(StringUtils.equals(branchStr, "STABLE")){
          return Branch.STABLE;
        }
        return Branch.UNKNOWN;
      }
      
      return branch;
    }

    //assumes we're comparing a replaceable version
    public boolean isSuperceededBy(Version other){
      if (getBranch().equals(other.getBranch())) {
        switch (getBranch()) {
          case STABLE:
            return this.version.compareTo(other.version) < 0;
          case TRUNK:
            return this.buildId.compareTo(other.buildId) < 0;
        }
      }
      return false;
    }
    
    
    public String toString(){
      switch(getBranch()){
        case LOCAL:
          return buildId;
        case TRUNK:
          return getBranch() + " build" + buildId;
        case STABLE:
          return "v" + version;
        case UNKNOWN:
        default:
          return getBranch().toString();
      }
    }

  }
  
  
  private static String getStringValue(Node node, String xpath, String defaultValue){
    Node valNode = node.selectSingleNode(xpath);
    if(valNode != null){
      String value = valNode.getText();
      if(!StringUtils.isEmpty(value)) return value;
    }
    return defaultValue;
  }
  
}
