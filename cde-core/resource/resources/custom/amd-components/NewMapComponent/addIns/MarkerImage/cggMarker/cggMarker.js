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

define([
  'cdf/AddIn',
  'cdf/Dashboard.Clean',
  'cdf/components/CggComponent.ext'],
  function(AddIn, Dashboard, CggComponentExt) {
  
  var cggMarker = new AddIn({
    name: "cggMarker",
    label: "CGG Marker",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var url = CggComponentExt.getCggDrawUrl() + '?script=' + st.cggGraphName;

      var width = st.width;
      var height = st.height;
      var cggParameters = {};
      if(st.width) {cggParameters.width = st.width;}
      if(st.height) {cggParameters.height = st.height;}

      cggParameters.noChartBg = true;
      var parameter;

      for(parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      // Check debug level and pass as parameter
      var level = Dashboard.debug; //TODO: review
      if(level > 1) {
        cggParameters.debug = true;
        cggParameters.debugLevel = level;
      }

      for(parameter in cggParameters) {
        if(cggParameters[parameter] !== undefined) {
          url += "&param" + parameter + "=" + encodeURIComponent(cggParameters[parameter]);
        }
      }

      return url;

    }
  });
  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", cggMarker);

  return cggMarker;

});
