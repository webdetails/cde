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


define('cde/components/DashboardComponent/DashboardComponent.ext', ['pentaho/environment'], function(env) {

  return {
    getDashboardUrl: function(path) {
      return path;
    },

    getDashboardParametersEndpoint: function() {
      return env.server.root + "plugin/pentaho-cdf-dd/api/renderer/getDashboardParameters?path=";
    }
  };

});
