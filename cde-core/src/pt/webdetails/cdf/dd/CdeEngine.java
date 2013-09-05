/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cdf.dd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

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


    public static CdeEngine getInstanceWithEnv(ICdeEnvironment environment) {
        if (instance == null) {
            instance = new CdeEngine(environment);
            try {
              instance.initialize();
            } catch (Exception ex) {
              logger.fatal("Error initializing CdeEngine: " + Util.getExceptionDescription(ex));
            }                                    
            
        }
        return instance;
    }

    public static CdeEngine getInstance() {

        if (instance == null) {
            instance = new CdeEngine();
            try {
              instance.initialize();
            } catch (Exception ex) {
              logger.fatal("Error initializing CdeEngine: " + Util.getExceptionDescription(ex));
            }                                    
        }

        return instance;

    }

    protected synchronized void initialize() throws DocumentException, IOException {
        logger.info("Initializing CDE Plugin " + cdeEnv.getPluginUtils().getPluginName().toUpperCase());
    }
    
    public ICdeEnvironment getEnvironment() {
      return this.cdeEnv;
    }
}
