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


package pt.webdetails.cdf.dd.util;

import pt.webdetails.cdf.dd.CdeConstants;
import pt.webdetails.cdf.dd.CdeEngine;
import pt.webdetails.cpf.utils.AbstractCorsUtil;
import pt.webdetails.cpf.utils.CsvUtil;

import java.util.Collection;

/**
 * CDE CorsUtil implementation
 */
public class CorsUtil extends AbstractCorsUtil {

  private static CorsUtil instance;


  public static CorsUtil getInstance() {
    if ( instance == null ) {
      instance = new CorsUtil();
    }
    return instance;
  }

    /**
     * Retrieves a flag value from a plugin settings.xml
     * @return true if the flag is present and CORS is allowed, otherwise returns false
     */
  @Override protected boolean isCorsAllowed() {
    return "true".equals( CdeEngine.getInstance().getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
      CdeConstants.PLUGIN_SETTINGS_ALLOW_CROSS_DOMAIN_RESOURCES ) );
  }

    /**
     * Retrieves a list value from a plugin settings.xml
     * @return returns a domain white list, if it is present, otherwise returns an empty list
     */
  @Override protected Collection<String> getDomainWhitelist() {
    return CsvUtil.parseCsvString(
      CdeEngine.getInstance().getEnvironment().getResourceLoader().getPluginSetting( CorsUtil.class,
        CdeConstants.PLUGIN_SETTINGS_CROSS_DOMAIN_RESOURCES_WHITELIST ) );
  }
}
