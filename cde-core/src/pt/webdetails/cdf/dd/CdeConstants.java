/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

/**
 * Just constants. Inner classes serve as namespaces
 */
public class CdeConstants {
  public static final String DEFAULT_STYLE = "Clean";
  public static final String DESIGNER_HEADER_TAG = "@HEADER@";
  public static final String DESIGNER_CDF_TAG = "@CDF@";
  public static final String DESIGNER_STYLES_TAG = "@STYLES@";
  public static final String DESIGNER_SCRIPTS_TAG = "@SCRIPTS@";
  public static final String DASHBOARD_TITLE_TAG = "@DASBOARD_TITLE@";
  public static final String DASHBOARD_DESCRIPTION_TAG = "@DASBOARD_DESCRIPTION@";
  public static final String DASHBOARD_SUPPORT_TAG = "@SUPPORT_TYPE@";
  public static final String DESIGNER_RESOURCE = "resources/cdf-dd.html";
  public static final String DESIGNER_RESOURCE_DEFAULT = "resources/cdf-dd-default.html";
  public static final String DESIGNER_STYLES_RESOURCE = "resources/styles.html";
  public static final String DESIGNER_SCRIPTS_RESOURCE = "resources/scripts.html";

  public static final String LEGACY_UNSAVED_FILE_PATH = "/null/null/null";

  /**
   * Inner paths relative to plugin repository dir
   */
  public static class SolutionFolders {
    public static final String COMPONENTS = "components";
    public static final String WIDGETS = "widgets";
    public static final String STYLES = "styles";
    public static final String TEMPLATES = "templates";
  }
  public static class MethodParams {
    public static final String DEBUG = "debug";
    public static final String ROOT = "root";
    public static final String SOLUTION = "solution";
    public static final String PATH = "path";
    public static final String FILE = "file";
    public static final String INFERSCHEME = "inferScheme";
    public static final String ABSOLUTE = "absolute";
    public static final String BYPASSCACHE = "bypassCache";
    public static final String VIEWID = "viewId";
    public static final String STYLE = "style";
    public static final String SCHEME = "scheme";
    public static final String SUPPORTS = "supports";
    public static final String ALIAS = "alias";
    /**
     * JSON structure
     */
    public static final String CDF_STRUCTURE = "cdfstructure";

    public static final String DATA = "data";
  }
  public static class DashboardSupportedTypes {
    public static final String AMD = "amd";
    public static final String LEGACY = "legacy";
  }
  public static class Tags {
    public static final String DASHBOARD_HEADER_TAG  = "\\@HEADER\\@";
    public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
    public static final String DASHBOARD_FOOTER_TAG  = "\\@FOOTER\\@";
    public static final String FILE_NAME_TAG = "@FILENAME@";
    public static final String SERVER_URL_TAG = "@SERVERURL@";
    public static class Api {
      public static final String RENDERER = "@CDE_RENDERER_API@";
    }
  }
  public static final String FILE_NAME_TAG = "@FILENAME@";
  public static final String SERVER_URL_TAG = "@SERVERURL@";
  public static final String DATA_URL_TAG = "@DATAURL@"; //formerly known as cdf-structure.js
  public static final String RESOURCE_FOOTER = "resources/patch-footer.html";
  public static final String RESOURCE_FOOTER_REQUIRE = "resources/patch-footer-require.html";
  public static final String DASHBOARD_HEADER_TAG  = "\\@HEADER\\@";
  public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  public static final String DASHBOARD_FOOTER_TAG  = "\\@FOOTER\\@";

  public static final String DASHBOARD_ALIAS_TAG = "@ALIAS@";
}
