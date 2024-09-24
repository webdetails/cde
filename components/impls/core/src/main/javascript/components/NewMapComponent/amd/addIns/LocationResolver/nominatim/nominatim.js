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
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, $, _) {
  "use strict";
  var nominatim = {
    name: "openstreetmap",
    label: "OpenStreetMap",
    defaults: {
      url: "//nominatim.openstreetmap.org/search",
      serviceParams: {
        format: "json",
        limit: "1"
      },
      mapping: {
        "street": "street",
        "postalcode": "postalcode",
        "city": "city",
        "county": "county",
        "state": "state",
        "country": "country"
      }
    },
    implementation: function(tgt, st, opt) {
      if (st.latitude || st.longitude) {
        var location = [parseFloat(st.longitude),
          parseFloat(st.latitude)];
        st.continuationFunction(location);
        return;
      }

      var params = $.extend(true, {}, opt.serviceParams);

      _.each(_.keys(st), function(key) {
        if (_.isFunction(st[key])) {
          return;
        }
        var keyLower = key.toLowerCase();
        if (keyLower in opt.mapping) {
          params[opt.mapping[keyLower]] = st[key];
        } else {
          // unrecognized fields go here
          //params['q'] = [ (params['q'] || ''), st[key] ].join(', ');
        }

      });

      if (params["q"]) {
        // we can't have "q=" and the more specific fields simultaneously.
        // so we use only "q="
        params = {
          q: params["q"] + ", " + _.compact(_.map(opt.mapping, function(field) {
            return params[field];
          })).join(", ")
        };
      }

      var onSuccess = function(result) {
        if (result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
            parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      var onError = function() {
        st.continuationFunction([]);
      };
      return $.ajax({
        dataType: "json",
        method: "GET",
        url: opt.url,
        data: $.extend({}, opt.serviceParams, params),
        success: onSuccess,
        error: onError
      });

    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(nominatim));

  return nominatim;

});
