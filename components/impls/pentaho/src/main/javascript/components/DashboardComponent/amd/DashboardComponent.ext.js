/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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

define([
  'pentaho/environment'
], function(environment) {

  return {

    getDashboardUrl: function(path) {
      // full endpoint URL already provided
      if(path.indexOf("getDashboard?path=") > 0) {
        return path;
      }

      // use the dash! requirejs loader plugin
      return "dash!" + encodeURIComponent(path).replace(/[!'()*]/g, function(c) {
        return '%' + c.charCodeAt(0).toString(16);
      });
    },

    getDashboardParametersEndpoint: function() {
      return environment.server.root + "plugin/pentaho-cdf-dd/api/renderer/getDashboardParameters?path=";
    }
  };
});
