/*!
 * Copyright 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
