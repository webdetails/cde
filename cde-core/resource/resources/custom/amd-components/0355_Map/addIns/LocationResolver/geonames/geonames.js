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
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/lib/jquery"
], function(AddIn, Dashboard, $) {
  "use strict";
  var geonames = {
    name: "geonames",
    label: "GeoNames",
    defaults: {
      username: "",
      url: "http://ws.geonames.org/searchJSON"
    },
    implementation: function(tgt, st, opt) {
      var location;
      var name = st.address;
      var featureClass;
      if (!name) {
        //Check city
        if (st.city) {
          name = st.city;
          featureClass = "P";
        } else if (st.county) {
          name = st.county;
          featureClass = "A";
        } else if (st.region) {
          name = st.region;
          featureClass = "A";
        } else if (st.state) {
          name = st.state;
          featureClass = "A";
        } else if (st.country) {
          name = st.country;
          featureClass = "A";
        }
      }

      var params = {
        q: name.replace(/&/g, ","),
        maxRows: 1,
        dataType: "json",
        username: opt.username,
        featureClass: featureClass
      };
      if (featureClass) {
        params.featureClass = featureClass;
      }
      var onSuccess = function(result) {
        if (result.geonames && result.geonames.length > 0) {
          location = [parseFloat(result.geonames[0].lng),
            parseFloat(result.geonames[0].lat)];
          st.continuationFunction(location);
        }
      };
      var onError = function() {
        st.continuationFunction(undefined);
      };
      return $.ajax({
        dataType: "json",
        url: opt.url,
        method: "GET",
        data: params,
        success: onSuccess,
        error: onError
      });
    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));

  return geonames;

});
