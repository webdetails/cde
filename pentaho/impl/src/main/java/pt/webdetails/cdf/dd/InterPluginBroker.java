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

import org.apache.commons.lang.StringUtils;

import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;

import java.util.Iterator;

/**
 * at least put cdf stuff here
 */
public class InterPluginBroker {

  public static final String DATA_SOURCE_DEFINITION_METHOD_NAME = "listDataAccessTypes";

  /**
   *
   * @param dashboard
   * @param type
   * @param debug
   * @param absolute
   * @param absRoot
   * @param scheme
   * @return
   * @throws Exception
   */
  public static String getCdfIncludes( String dashboard, String type, boolean debug, boolean absolute,
                                      String absRoot, String scheme ) throws Exception {

    CallParameters params = new CallParameters();
    params.put( "dashboardContent", dashboard );
    params.put( "debug", debug );
    if ( type != null ) {
      params.put( "dashboardType", type );
    }
    if ( !StringUtils.isEmpty( absRoot ) ) {
      params.put( "root", absRoot );
    }
    if ( !StringUtils.isEmpty( scheme ) ) {
      params.put( "scheme", scheme );
    }
    params.put( "absolute", absolute );

    //TODO: instantiate directly
    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), "xcdf", "getHeaders" );
    return pluginCall.call( params.getParameters() );
  }

  /**
   *
   * @param plugin
   * @param service
   * @param method
   * @param forceRefresh
   * @return
   * @throws Exception
   */
  public static String getDataSourceDefinitions( String plugin, String service, String method, boolean forceRefresh )
    throws Exception {
    CallParameters params = new CallParameters();
    params.put( "refreshCache", forceRefresh );

    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( plugin, null, method );
    return pluginCall.call( params.getParameters() );
  }

  /**
   *
   * @param dashboard
   * @param action
   * @param view
   * @param requestParams
   * @return
   * @throws Exception
   */
  public static String getCdfContext( String dashboard, String action, String view, IParameterProvider requestParams )
      throws Exception {
    CallParameters params = new CallParameters();
    params.put( "path", dashboard );
    params.put( "action", action );
    params.put( "view", view );

    if ( requestParams != null ) {
      Iterator<String> iterator = requestParams.getParameterNames();

      while ( iterator.hasNext() ) {
        String paramName = iterator.next();
        if ( StringUtils.isEmpty( paramName ) ) {
          continue;
        }
        if ( requestParams.hasParameter( paramName ) ) {
          Object paramValue = requestParams.getParameter( paramName );
          if ( paramValue == null ) {
            continue;
          }

          params.put( paramName, StringUtils.join( (String[]) paramValue, null, 0, 1 ) );
        }
      }
    }

    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), "xcdf", "getContext" );
    return pluginCall.call( params.getParameters() );
  }

  /**
   *
   * @param dashboard
   * @param requestParams
   * @return
   * @throws Exception
   */
  public static String getCdfRequireContext( String dashboard, IParameterProvider requestParams ) throws Exception {
    CallParameters params = new CallParameters();
    params.put( "path", dashboard );

    if ( requestParams != null ) {
      Iterator<String> iterator = requestParams.getParameterNames();

      while ( iterator.hasNext() ) {
        String paramName = iterator.next();
        if ( StringUtils.isEmpty( paramName ) ) {
          continue;
        }
        if ( requestParams.hasParameter( paramName ) ) {
          Object paramValue = requestParams.getParameter( paramName );
          if ( paramValue == null ) {
            continue;
          }

          params.put( paramName, StringUtils.join( (String[]) paramValue, null, 0, 1 ) );
        }
      }
    }

    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), "context", "get" );
    return pluginCall.call( params.getParameters() );
  }

  /**
   *
   * @param dashboard
   * @param requestParams
   * @return
   * @throws Exception
   */
  public static String getCdfRequireConfig( String dashboard, IParameterProvider requestParams ) throws Exception {
    CallParameters params = new CallParameters();
    params.put( "path", dashboard );

    if ( requestParams != null ) {
      Iterator<String> iterator = requestParams.getParameterNames();

      while ( iterator.hasNext() ) {
        String paramName = iterator.next();
        if ( StringUtils.isEmpty( paramName ) ) {
          continue;
        }
        if ( requestParams.hasParameter( paramName ) ) {
          Object paramValue = requestParams.getParameter( paramName );
          if ( paramValue == null ) {
            continue;
          }

          params.put( paramName, StringUtils.join( (String[]) paramValue, null, 0, 1 ) );
        }
      }
    }

    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), "context", "getConfig" );
    return pluginCall.call( params.getParameters() );
  }

  public static String getCdfEmbed( String protocol, String name, int port, int inactiveInterval, String locale,
                                    IParameterProvider requestParams ) throws Exception {
    return getCdfEmbed( protocol, name, port, inactiveInterval, locale, false, requestParams );
  }

  public static String getCdfEmbed( String protocol, String name, int port, int inactiveInterval, String locale,
                                    boolean secure, IParameterProvider requestParams ) throws Exception {
    CallParameters params = new CallParameters();
    params.put( "protocol", protocol );
    params.put( "name", name );
    params.put( "port", port );
    params.put( "inactiveInterval", inactiveInterval );
    params.put( "locale", locale );
    params.put( "secure", secure );

    if ( requestParams != null ) {
      Iterator<String> iterator = requestParams.getParameterNames();

      while ( iterator.hasNext() ) {
        String paramName = iterator.next();
        if ( StringUtils.isEmpty( paramName ) ) {
          continue;
        }
        if ( requestParams.hasParameter( paramName ) ) {
          Object paramValue = requestParams.getParameter( paramName );
          if ( paramValue == null ) {
            continue;
          }

          params.put( paramName, StringUtils.join( (String[]) paramValue, null, 0, 1 ) );
        }
      }
    }

    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDF.getId(), "cdfApi",
        "buildCdfEmbedContextSecure" );
    return pluginCall.call( params.getParameters() );
  }
}
