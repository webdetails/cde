/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cdf.dd.bean.factory.CoreBeanFactory;
import pt.webdetails.cdf.dd.bean.factory.ICdeBeanFactory;
import pt.webdetails.cpf.Util;

public class CdeEngine {

    private static CdeEngine instance;
    protected static Log logger = LogFactory.getLog(CdeEngine.class);
    private ICdeEnvironment cdeEnv;


    private CdeEngine() {
        logger.debug("Starting ElementEngine");
    } 

    private CdeEngine(ICdeEnvironment environment) {
        this();
        this.cdeEnv = environment;
    }

    public static CdeEngine getInstance() {

        if (instance == null) {
            instance = new CdeEngine();
            try {
            	initialize();
            } catch (Exception ex) {
              logger.fatal("Error initializing CdeEngine: " + Util.getExceptionDescription(ex));
            }                                    
        }

        return instance;
    }
    
    public ICdeEnvironment getEnvironment() {
    	return getInstance().cdeEnv;
    }
    
    private static void initialize() throws InitializationException {
  	  if (instance.cdeEnv == null) {
  		  
  		  ICdeBeanFactory factory = new CoreBeanFactory();
  		  
  		  // try to get the environment from the configuration
  		  // will return the DefaultCdaEnvironment by default
  		  ICdeEnvironment env = instance.getConfiguredEnvironment(factory);
  		  
  		  if(env != null){
  			  env.init(factory);
  		  }
  		  
  		  instance.cdeEnv = env;
  	  }
    }

    public static ICdeEnvironment getEnv() {
      return getInstance().getEnvironment();
    }

    protected synchronized ICdeEnvironment getConfiguredEnvironment(ICdeBeanFactory factory) throws InitializationException {
    	
    	Object obj = new CoreBeanFactory().getBean(ICdeEnvironment.class.getSimpleName()); 
    	
    	if(obj != null && obj instanceof ICdeEnvironment){
    		return (ICdeEnvironment) obj;
    	}else{
    		logger.warn("No bean found for ICdeEnvironment, assuming DefaultCdeEnvironment");
    		return new DefaultCdeEnvironment();
    	}
    }
}
