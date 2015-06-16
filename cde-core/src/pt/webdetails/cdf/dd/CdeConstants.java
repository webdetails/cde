/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
    public static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
    public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
    public static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";
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
  public static final String DASHBOARD_HEADER_TAG = "\\@HEADER\\@";
  public static final String DASHBOARD_CONTENT_TAG = "\\@CONTENT\\@";
  public static final String DASHBOARD_FOOTER_TAG = "\\@FOOTER\\@";

  public static final String DASHBOARD_ALIAS_TAG = "@ALIAS@";

  public static final String CUSTOM_COMPONENT_CONFIG_FILENAME = "component.xml";

  public enum REQUIREJS_PLUGIN {
    CSS( "css!" ), NONAMD( "amd!" );

    private final String plugin;

    REQUIREJS_PLUGIN( String plugin ) {
      this.plugin = plugin;
    }

    public String toString() {
      return this.plugin;
    }
  }

  public static final String RESOURCE_AMD_NAMESPACE = "cde/resources";

  public static class Writer {
    public static final String NEWLINE = System.getProperty( "line.separator" );
    public static final String INDENT1 = "  ";
    public static final String INDENT2 = "    ";
    public static final String INDENT3 = "      ";
    public static final String INDENT4 = "        ";
    // make the dashboard variable available in the global scope to facilitate debugging
    public static final String DASHBOARD_DECLARATION = "window.dashboard = new Dashboard({0});";
    public static final String DASHBOARD_INIT = "dashboard.init();" + NEWLINE;
    public static final String REQUIRE_START = "require([''{0}'']," + NEWLINE + "function({1}) '{'";
    public static final String REQUIRE_STOP = "return dashboard;" + NEWLINE + "});";
    public static final String DEFINE_START = "define([''{0}'']," + NEWLINE + INDENT1 + "function({1}) '{'" + NEWLINE;
    public static final String DEFINE_STOP = "return CustomDashboard;" + NEWLINE + "});";
    public static final String DASHBOARD_MODULE_START_EMPTY_ALIAS =
        "var CustomDashboard = Dashboard.extend('{'" + NEWLINE
        + INDENT1 + "constructor: function(element, opts) '{'" + NEWLINE
        + INDENT2 + "var alias;" + NEWLINE
        + INDENT2 + "if(typeof opts === \"string\") '{'" + NEWLINE
        + INDENT3 + "alias = opts;" + NEWLINE
        + INDENT2 + "'}'" + NEWLINE
        + INDENT2 + "var extendedOpts = '{}';" + NEWLINE
        + INDENT2 + "$.extend(extendedOpts, {0}, opts);" + NEWLINE
        + INDENT2 + "this.base.apply(this, [extendedOpts]);" + NEWLINE
        + INDENT2 + "CustomDashboard.aliasCounter = (CustomDashboard.aliasCounter || 0 ) + 1;" + NEWLINE
        + INDENT2 + "this.phElement = element;" + NEWLINE
        + INDENT2 + "this._alias = alias ? alias : " + NEWLINE
        + INDENT3 + "(opts.alias ? opts.alias : \"alias\" + CustomDashboard.aliasCounter);" + NEWLINE
        + INDENT2 + "this.layout = ''{1}''.replace(/" + DASHBOARD_ALIAS_TAG + "/g, this._alias);" + NEWLINE
        + INDENT1 + "'}'," + NEWLINE;
    public static final String DASHBOARD_MODULE_START = "var CustomDashboard = Dashboard.extend('{'" + NEWLINE
        + INDENT1 + "constructor: function(element, opts) '{'" + NEWLINE
        + INDENT2 + "this.phElement = element; " + NEWLINE
        + INDENT2 + "var extendedOpts = '{}';" + NEWLINE
        + INDENT2 + "$.extend(extendedOpts, {0}, opts);" + NEWLINE
        + INDENT2 + "this.base.apply(this, [extendedOpts]); '}'," + NEWLINE;
    public static final String DASHBOARD_MODULE_LAYOUT = INDENT1 + "layout: ''{0}''," + NEWLINE;
    public static final String DASHBOARD_MODULE_SETUP_DOM = "setupDOM: function() {" + NEWLINE
        + INDENT2 + "var target, isId;" + NEWLINE
        + INDENT2 + "if(typeof this.phElement === \"string\") {" + NEWLINE
        + INDENT3 + "target = $('#' + this.phElement);" + NEWLINE
        + INDENT3 + "isId = true;" + NEWLINE
        + INDENT2 + "} else {" + NEWLINE
        + INDENT3 + "target = this.phElement && this.phElement[0] ? $(this.phElement[0]) : $(this.phElement);" + NEWLINE
        + INDENT2 + "} " + NEWLINE
        + INDENT2 + "if(!target.length) { " + NEWLINE
        + INDENT3 + "if(isId) {" + NEWLINE
        + INDENT4 + "Logger.warn('Invalid target element id: ' + this.phElement);" + NEWLINE
        + INDENT3 + "} else {" + NEWLINE
        + INDENT4 + "Logger.warn('Target DOM object empty');" + NEWLINE
        + INDENT3 + "} " + NEWLINE
        + INDENT2 + "return;} " + NEWLINE
        + INDENT2 + "target.empty();" + NEWLINE
        + INDENT2 + "target.html(this.layout);" + NEWLINE
        + " },";
    public static final String DASHBOARD_MODULE_RENDERER = "render: function() {" + NEWLINE
        + INDENT2 + "this.setupDOM();" + NEWLINE
        + INDENT2 + "this.renderDashboard();" + NEWLINE
        + INDENT1 + "}," + NEWLINE
        + INDENT2 + "renderDashboard: function() {" + NEWLINE
        + INDENT2 + "this._processComponents();" + NEWLINE
        + INDENT2 + "this.init();" + NEWLINE
        + "},";
    public static final String DASHBOARD_MODULE_PROCESS_COMPONENTS =
        INDENT1 + "_processComponents: function() '{'" + NEWLINE
        + INDENT2 + "var dashboard = this;" + NEWLINE
        + INDENT2 + "{0}" + NEWLINE
        + INDENT1 + "'}'" + NEWLINE;
    public static final String DASHBOARD_MODULE_STOP = INDENT1 + "});";
    public static final String CDF_AMD_BASE_COMPONENT_PATH = "cdf/components/";
    public static final String CDE_AMD_BASE_COMPONENT_PATH = "cde/components/";
    public static final String CDE_AMD_REPO_COMPONENT_PATH = "cde/repo/components/";
    public static final String PLUGIN_COMPONENT_FOLDER = "/components/";
    public static final String REQUIRE_PATH_CONFIG = "requireCfg[''paths''][''{0}''] = "
        + "CONTEXT_PATH + ''plugin/pentaho-cdf-dd/api/resources{1}'';";
    public static final String REQUIRE_PATH_CONFIG_FULL_URI = "requireCfg[''paths''][''{0}''] = ''{1}''";
    public static final String REQUIRE_CONFIG = "require.config(requireCfg);";
  }
}
