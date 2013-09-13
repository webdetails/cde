/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;

public class CdeLifeCycleListener extends SimpleLifeCycleListener {

  static Log logger = LogFactory.getLog(CdeLifeCycleListener.class);

  @Override
  public void init() throws PluginLifecycleException
  {
    logger.debug("Init for CDE");
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    
    IUserContentAccess access = CdeEnvironment.getUserContentAccess();
    
    if (!access.fileExists("cde")){

        if(access.createFolder("cde")){
        	HibernateUtil.closeSession(); //solves http://redmine.webdetails.org/issues/2094
        }else {
        	logger.error("Error while creating folder cde for cde plugin. CDE may not work as expected", null);
        }
    }

    if (!access.fileExists("cde/styles")) {
    
        if(access.createFolder("cde/styles")){
        	HibernateUtil.closeSession(); //solves http://redmine.webdetails.org/issues/2094
        } else {
        	logger.error("Error while creating folder cde/styles for cde plugin. CDE may not work as expected", null);
        }
    }
    
    if (!access.fileExists("cde/components")) {      
    	
        if(access.createFolder("cde/components")){
        	HibernateUtil.closeSession(); //solves http://redmine.webdetails.org/issues/2094
        } else {
	       logger.error("Error while creating folder cde/components for cde plugin. CDE may not work as expected", null);
	    }
    }

    if (!access.fileExists("cde/templates")) {
    
        if(access.createFolder("cde/templates")){
        	HibernateUtil.closeSession(); //solves http://redmine.webdetails.org/issues/2094
        } else {
        	logger.error("Error while creating folder cde/templates for cde plugin. CDE may not work as expected", null);
        }
    }

    if (!access.fileExists("cde/widgets")) {
      
    	try {
	        if(access.createFolder("cde/widgets")){
	        	HibernateUtil.closeSession(); //solves http://redmine.webdetails.org/issues/2094
	        }
        
	        IReadAccess pluginSystemReader = CdeEnvironment.getPluginSystemReader("resources/samples/");
	        IRWAccess pluginRepositoryWriter = CdeEnvironment.getPluginRepositoryWriter("cde/widgets/");
        
	        if(pluginSystemReader.fileExists("widget.cdfde")){
	        	IBasicFile file = pluginSystemReader.fetchFile("widget.cdfde");
	        	pluginRepositoryWriter.saveFile(file.getFullPath().replace("widget.cdfde", "sample.cdfde"), file.getContents());
	        }
	        
	        if(pluginSystemReader.fileExists("widget.wcdf")){
	        	IBasicFile file = pluginSystemReader.fetchFile("widget.wcdf");
	        	pluginRepositoryWriter.saveFile(file.getFullPath().replace("widget.wcdf", "sample.wcdf"), file.getContents());
	        }
	        
	        if(pluginSystemReader.fileExists("widget.cda")){
	        	IBasicFile file = pluginSystemReader.fetchFile("widget.cda");
	        	pluginRepositoryWriter.saveFile(file.getFullPath().replace("widget.cda", "sample.cda"), file.getContents());
	        }
	        
	        if(pluginSystemReader.fileExists("widget.xml")){
	        	IBasicFile file = pluginSystemReader.fetchFile("widget.xml");
	        	pluginRepositoryWriter.saveFile(file.getFullPath().replace("widget.xml", "sample.component.xml"), file.getContents());
	        }
	    } catch (IOException ioe) {
	        logger.error("Error while creating folder cde/widgets for cde plugin. CDE may not work as expected", ioe);
	    }
     }
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    logger.debug("Unload for CDE");
  }

@Override
public PluginEnvironment getEnvironment() {
	return (PluginEnvironment)CdeEngine.getInstance().getEnvironment();
}
}
