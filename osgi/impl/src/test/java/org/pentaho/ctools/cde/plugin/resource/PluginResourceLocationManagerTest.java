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

public class PluginResourceLocationManagerTest {
  private final String DEFAULT_STYLE_PATH = "styles/";
  private final String DEFAULT_STYLE = "Clean";
  private final String DEFAULT_STYLE_EXTENSION= ".html";
  private final String DEFAULT_STYLE_FULL_PATH = DEFAULT_STYLE_PATH + DEFAULT_STYLE + DEFAULT_STYLE_EXTENSION;
  private final String DEFAULT_MESSAGES_LOCATION = "lang/messages.properties";
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
    assertEquals( DEFAULT_MESSAGES_LOCATION, pluginResourceLocationManager.getMessagePropertiesResourceLocation() );
  }

  @Test
  public void getStyleResourceLocation() {
    assertEquals( DEFAULT_STYLE_FULL_PATH, pluginResourceLocationManager.getStyleResourceLocation( null ) );
    assertEquals( DEFAULT_STYLE_FULL_PATH, pluginResourceLocationManager.getStyleResourceLocation( "" ) );
    final String styleName = "aStyle";
    assertEquals(
      DEFAULT_STYLE_PATH + styleName + DEFAULT_STYLE_EXTENSION,
      pluginResourceLocationManager.getStyleResourceLocation( styleName ) );
  }

  @Test
  public void testGetCustomComponentsLocations() {
    List<PathOrigin> expected = Collections.emptyList();
    assertEquals( expected, pluginResourceLocationManager.getCustomComponentsLocations() );
  }
}
