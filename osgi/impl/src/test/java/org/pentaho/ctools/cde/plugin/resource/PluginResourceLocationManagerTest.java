/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.ctools.cde.plugin.resource;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pt.webdetails.cpf.packager.origin.PathOrigin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PluginResourceLocationManagerTest {
  private PluginResourceLocationManager pluginResourceLocationManager;

  @Before
  public void setUp() {
    pluginResourceLocationManager = new PluginResourceLocationManager();
  }

  @After
  public void tearDown() {
    pluginResourceLocationManager = null;
  }

  @Test
  public void testGetMessagePropertiesResourceLocation() {
    assertNull( pluginResourceLocationManager.getMessagePropertiesResourceLocation() );
  }

  @Test
  public void getStyleResourceLocationNullStyleName() {
    assertNull( pluginResourceLocationManager.getStyleResourceLocation( null ) );
  }

  @Test
  public void getStyleResourceLocationEmptyStyleName() {
    assertNull( pluginResourceLocationManager.getStyleResourceLocation( "" ) );
  }

  @Test
  public void testGetCustomComponentsLocations() {
    List<PathOrigin> expected = Collections.emptyList();
    assertEquals( expected, pluginResourceLocationManager.getCustomComponentsLocations() );
  }
}
