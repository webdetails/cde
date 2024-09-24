/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
