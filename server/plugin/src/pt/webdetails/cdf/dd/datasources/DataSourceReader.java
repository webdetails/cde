/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cdf.dd.datasources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang.StringUtils;

import pt.webdetails.cdf.dd.DashboardManager;
import pt.webdetails.cdf.dd.util.XPathUtils;
import pt.webdetails.cpf.repository.PentahoRepositoryAccess;

public class DataSourceReader {

  public static class CdaDataSource {
    String cdaSettings;
    String dataAccessId;
    
    CdaDataSource(JXPathContext dataSourceContext){
      this(dataSourceContext,null);
    }
    
    CdaDataSource(JXPathContext dataSourceContext, String cdaSettings)
    {
      this.dataAccessId = XPathUtils.getStringValue(dataSourceContext, "properties/value[../name='name']");
      
      if(cdaSettings != null)
      {
        this.cdaSettings = cdaSettings;
      }
      else {
        if(XPathUtils.exists(dataSourceContext, "properties/value[../name='cdaPath']")){
          this.cdaSettings = XPathUtils.getStringValue(dataSourceContext, "properties/value[../name='cdaPath']"); 
        }
        else {
          this.cdaSettings = XPathUtils.getStringValue(dataSourceContext, "properties/value[../name='solution']") + '/' +
                             XPathUtils.getStringValue(dataSourceContext, "properties/value[../name='path']") + '/' +
                             XPathUtils.getStringValue(dataSourceContext, "properties/value[../name='file']");
        }
      }
    }
    
    public CdaDataSource(String cdaSettings, String dataAccessId){
      this.cdaSettings = cdaSettings;
      this.dataAccessId = dataAccessId;
    }
    
    public String toString(){
      return "{cdaSettingsId:'" + cdaSettings + "'" + (dataAccessId != null? (", dataAccessId:'" + dataAccessId + "'") : "") + "}";
    }
    
  }
  
  // TODO: The instance model 
  // already has a DataSourceComponent list in the Dashboar object...
  public static List<CdaDataSource> getCdaDataSources(String dashboard){

    JXPathContext context;
    try {
      context = DashboardManager.openDashboardAsJXPathContext(
              PentahoRepositoryAccess.getRepository(), 
              dashboard, 
              /*wcdf*/null);
    } catch (FileNotFoundException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
    return getCdaDataSources(context);
  }
  
  private static List<CdaDataSource> getCdaDataSources(JXPathContext docContext){
    
    ArrayList<CdaDataSource> dataSources = new ArrayList<CdaDataSource>();
    //external
    @SuppressWarnings("unchecked")
    Iterator<Pointer> extDataSources = docContext.iteratePointers("/datasources/rows[properties/name='dataAccessId']");
    while(extDataSources.hasNext()){
      Pointer source = extDataSources.next();
      if(!(source instanceof NullPointer)){
        dataSources.add(new CdaDataSource(docContext.getRelativeContext(source)));
      }
    }
    
    @SuppressWarnings("unchecked")
    Iterator<Pointer> builtInDataSources = docContext.iteratePointers("/datasources/rows[meta='CDA']");
    if(builtInDataSources.hasNext()){
      //built-in
      String fileName = XPathUtils.getStringValue(docContext, "/filename");
      String toReplace = ".cdfde";
      String replaceWith = ".cda";
      if(StringUtils.endsWith(fileName,".wcdf")){
        toReplace = ".wcdf";
      }
      fileName = StringUtils.replace(fileName, toReplace, replaceWith);
      //just add cda name
      dataSources.add(new CdaDataSource(fileName, null));
//      
//  
//      while(builtInDataSources.hasNext()){
//        Pointer source = builtInDataSources.next();
//        if(!(source instanceof NullPointer)){
//          dataSources.add(new CdaDataSource(docContext.getRelativeContext(source), fileName));
//        }
//      }
    }
    
    return dataSources;
  }
  
}
