/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


define({
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
    return "/cxf/cde/renderer/getDashboardParameters?path=";
  }
});
