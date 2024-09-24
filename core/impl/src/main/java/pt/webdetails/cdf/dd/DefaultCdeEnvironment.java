/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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

import pt.webdetails.cdf.dd.util.Utils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IBasicFile;

public abstract class DefaultCdeEnvironment extends PluginEnvironment implements ICdeEnvironment {

  private static final String PLUGIN_REPOSITORY_DIR = "cde";
  private static final String PLUGIN_ID = "pentaho-cdf-dd";
  private static final String SYSTEM_DIR = "system";
  private static final String CONTENT = "content";

  public String getPluginRepositoryDir() {
    return PLUGIN_REPOSITORY_DIR;
  }

  public String getPluginId() {
    return PLUGIN_ID;
  }

  public String getSystemDir() {
    return SYSTEM_DIR;
  }

  public String getApplicationBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/";
  }

  public String getRepositoryBaseContentUrl() {
    return Utils.joinPath( getApplicationBaseUrl(), CONTENT, getPluginId() ) + "/res/";
  }

  @Override
  public IBasicFile getCdeXml() {
    return null;
  }
}
