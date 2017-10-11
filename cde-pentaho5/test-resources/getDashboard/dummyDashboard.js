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

define('cde/test/dummyDashboard', ['cdf/Dashboard.Bootstrap', 'cdf/Logger', 'cdf/lib/jquery', 'amd!cdf/lib/underscore', 'cdf/lib/moment', 'cdf/components/ButtonComponent'], 
  function(Dashboard, Logger, $, _, moment, ButtonComponent) {
  var CustomDashboard = Dashboard.extend({
    constructor: function(element, alias) {
      this.base.apply(this, arguments);
      CustomDashboard.aliasCounter = (CustomDashboard.aliasCounter || 0) + 1;
      this.phElement = element;
      this._alias = alias ? alias : "alias" + CustomDashboard.aliasCounter;
      this.layout = '  <div class=\'dummyDashboard_@ALIAS@_container\'><div id=\'dummyDashboard_@ALIAS@_5c85372a-4980-a42d-ed8e-142e811ffadf\'  class=\'row clearfix \' ><div class=\' last\'><div id=\'dummyDashboard_@ALIAS@_column\' ><\/div><\/div><\/div>  <\/div>'.replace(/@ALIAS@/g, this._alias);
    },
    render: function() {
      this.setupDOM();
      this.renderDashboard();
    },
    renderDashboard: function() {
      this._processComponents();
      this.init();
    },
    setupDOM: function() {
      var target, isId;
      if (typeof this.phElement === "string") {
        target = $('#' + this.phElement);
        isId = true;
      } else {
        target = this.phElement && this.phElement[0] ? $(this.phElement[0]) : $(this.phElement);
      }
      if (!target.length) {
        if (isId) {
          Logger.warn('Invalid target element id: ' + this.phElement);
        } else {
          Logger.warn('Target DOM object empty');
        }
        return;
      }
      target.empty();
      target.html(this.layout);
    },
    _processComponents: function() {
      var dashboard = this;
      var wcdfSettings = {
        "title": "dummyDashboard",
        "author": "",
        "description": "dummydashboard",
        "style": "CleanRequire",
        "widgetName": "",
        "widget": false,
        "rendererType": "bootstrap",
        "require": true,
        "widgetParameters": []
      };

      dashboard.addDataSource("dummyDataSource", {
        origin: "dummyDashboard"
      });

      var render_button = new ButtonComponent({
        type: "ButtonComponent",
        name: "render_button",
        priority: 5,
        label: "Button",
        htmlObject: "column",
        buttonStyle: "themeroller",
        executeAtStart: true,
        listeners: [],
        actionParameters: [],
        actionDefinition: {

        }
      });
      dashboard.addParameter("dummyParam", "");
      dashboard.setParameterViewMode("dummyParam", "unused");
      dashboard.addParameter("privateParam", "");
      dashboard.setParameterViewMode("privateParam", "unused");
      dashboard.addParameter("multiMapParam", "");
      dashboard.setParameterViewMode("multiMapParam", "unused");

      dashboard.addComponents([render_button]);

    }
  });
  return CustomDashboard;
});
