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
