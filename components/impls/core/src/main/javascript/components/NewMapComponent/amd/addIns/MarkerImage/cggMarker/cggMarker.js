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


define([
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/components/CggComponent.ext"
], function(AddIn, Dashboard, CggComponentExt) {
  "use strict";
  var cggMarker = {
    name: "cggMarker",
    label: "CGG Marker",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var url = CggComponentExt.getCggDrawUrl() + "?script=" + st.cggGraphName;

      var cggParameters = {};
      if (st.width) {
        cggParameters.width = st.width;
      }
      if (st.height) {
        cggParameters.height = st.height;
      }

      cggParameters.noChartBg = true;
      var parameter;

      for (parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      // Check debug level and pass as parameter
      var level = Dashboard.debug; //TODO: review
      if (level > 1) {
        cggParameters.debug = true;
        cggParameters.debugLevel = level;
      }

      for (parameter in cggParameters) {
        if (cggParameters[parameter] !== undefined) {
          url += "&param" + parameter + "=" + encodeURIComponent(cggParameters[parameter]);
        }
      }

      return url;

    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", new AddIn(cggMarker));

  return cggMarker;

});
