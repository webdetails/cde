/*!
 * Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package pt.webdetails.cdf.dd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;

public class CdeLifeCycleListener extends SimpleLifeCycleListener implements IPlatformReadyListener {

  private static Log logger = LogFactory.getLog( CdeLifeCycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "Init for CDE" );

    super.init();
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    super.loaded();
  }

  @Override
  public void ready() {
    logger.debug( "Ready Event for CDE" );

    CdeEngine.getInstance().ensureBasicDirs();
  }

  @Override
  public void unLoaded() {
    logger.debug( "Unload for CDE" );
  }

  @Override
  public PluginEnvironment getEnvironment() {
    return (PluginEnvironment) CdeEngine.getInstance().getEnvironment();
  }
}
