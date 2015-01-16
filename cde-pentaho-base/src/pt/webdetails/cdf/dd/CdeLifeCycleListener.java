/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/
package pt.webdetails.cdf.dd;

//import java.io.IOException;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.PluginLifecycleException;
//import org.pentaho.platform.repository.hibernate.HibernateUtil;

import pt.webdetails.cdf.dd.util.CdeEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;

public class CdeLifeCycleListener extends SimpleLifeCycleListener {

  static Log logger = LogFactory.getLog( CdeLifeCycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CDE" );
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    super.loaded();
    //CdeEngine.getInstance().ensureBasicDirs();

    /**
    * IMPORTANT: ensureBasicDirs() functionality was working in 5.0.1 but stopped working in 5.1.0;
    *
    * This is because in 5.1.0 's pentaho-solutions/system/systemListeners.xml
    * bean SecuritySystemListener (responsible for booting up AuthenticationProvider)
    * is being declared AFTER pluginSystemListener.
    *
    * Therefore, if by any chance a plugin desires to access the repository ON STARTUP using a login such as
    * SecurityHelper.getInstance().runAsSystem(), it will be faced with a InvalidLoginCredentials exception
    *
    * Dev/Testing solution:
    *
    * in pentaho-solutions/system/systemListeners.xml place bean SecuritySystemListener BEFORE pluginSystemListener
    */
  }



  @Override
  public void unLoaded() throws PluginLifecycleException {
    logger.debug( "Unload for CDE" );
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return (PluginEnvironment) CdeEngine.getInstance().getEnvironment();
  }
}
