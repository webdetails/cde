/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package pt.webdetails.cdf.dd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugincall.api.IPluginCall;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class InterPluginBrokerTest {

  private static final String PARAMETER_NAME = "parameter";
  private static final String FIRST_PARAMETER_VALUE = "first";
  private static final String PLUGIN_RESULT = "result";

  private MockedStatic<PluginEnvironment> pluginEnvironmentMockedStatic;
  private IParameterProvider requestParams;
  private AtomicReference<Map<String, String[]>> pluginCallParams;

  @Before
  public void setup() throws Exception {
    PluginEnvironment pluginEnvironment = mock( PluginEnvironment.class );
    IPluginCall pluginCall = mock( IPluginCall.class );
    requestParams = mock( IParameterProvider.class );
    pluginCallParams = new AtomicReference<>();

    pluginEnvironmentMockedStatic = mockStatic( PluginEnvironment.class );
    pluginEnvironmentMockedStatic.when( PluginEnvironment::env ).thenReturn( pluginEnvironment );

    when( pluginEnvironment.getPluginCall( anyString(), anyString(), anyString() ) ).thenReturn( pluginCall );
    when( pluginCall.call( anyMap() ) ).thenAnswer( invocation -> {
      pluginCallParams.set( invocation.getArgument( 0 ) );
      return PLUGIN_RESULT;
    } );
    when( requestParams.getParameterNames() )
      .thenAnswer( invocation -> Collections.singleton( PARAMETER_NAME ).iterator() );
    when( requestParams.hasParameter( PARAMETER_NAME ) ).thenReturn( true );
    when( requestParams.getParameter( PARAMETER_NAME ) )
      .thenReturn( new String[] { FIRST_PARAMETER_VALUE, "second" } );
  }

  @After
  public void tearDown() {
    pluginEnvironmentMockedStatic.close();
  }

  @Test
  public void getCdfContext() throws Exception {
    assertFirstRequestParameterValueIsForwarded(
      () -> InterPluginBroker.getCdfContext( "dashboard", "action", "view", requestParams ) );
  }

  @Test
  public void getCdfRequireContext() throws Exception {
    assertFirstRequestParameterValueIsForwarded(
      () -> InterPluginBroker.getCdfRequireContext( "dashboard", requestParams ) );
  }

  @Test
  public void getCdfRequireConfig() throws Exception {
    assertFirstRequestParameterValueIsForwarded(
      () -> InterPluginBroker.getCdfRequireConfig( "dashboard", requestParams ) );
  }

  @Test
  public void getCdfEmbed() throws Exception {
    assertFirstRequestParameterValueIsForwarded(
      () -> InterPluginBroker.getCdfEmbed( "http", "localhost", 8080, 30, "en_US", requestParams ) );
  }

  private void assertFirstRequestParameterValueIsForwarded( Callable<String> brokerCall ) throws Exception {
    assertEquals( PLUGIN_RESULT, brokerCall.call() );
    assertArrayEquals(
      new String[] { FIRST_PARAMETER_VALUE },
      pluginCallParams.get().get( PARAMETER_NAME ) );
  }
}