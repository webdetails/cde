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
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore'],
  function(AddIn, Dashboard, $, _) {

  var nominatim = {
    name: "openstreetmap",
    label: "OpenStreetMap",
    defaults: {
      url: 'http://nominatim.openstreetmap.org/search',
      serviceParams: {
        format: 'json',
        limit: '1'
      },
      mapping: {
        'street': 'street',
        'postalcode': 'postalcode',
        'city': 'city',
        'county': 'county',
        'state': 'state',
        'country': 'country'
      }
    },
    implementation: function(tgt, st, opt) {
      if(st.latitude || st.longitude) {
        var location = [parseFloat(st.longitude),
                        parseFloat(st.latitude)];
        st.continuationFunction(location);
        return;
      }

      var params = $.extend(true, {}, opt.serviceParams);

      _.each(_.keys(st), function(key) {
        if(_.isFunction(st[key])) {
          return;
        }
        var keyLower = key.toLowerCase();
        if(keyLower in opt.mapping) {
          params[ opt.mapping[ keyLower ] ] = st[key];
        } else {
          // unrecognized fields go here
          //params['q'] = [ (params['q'] || ''), st[key] ].join(', ');
        }

      });

      if(params['q']) {
        // we can't have "q=" and the more specific fields simultaneously.
        // so we use only "q="
        params = {
          q: params['q'] + ', ' + _.compact(_.map(opt.mapping, function(field) {
            return params[field];
          })).join(', ')
        };
      }

      var success = function(result) {
        if(result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
                          parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(opt.url, $.extend({}, opt.serviceParams, params), success);
    }
  };

  var mapquest = {};

  $.extend(
      true,
      mapquest,
      nominatim, {
        name: "mapquest",
        label: "MapQuest",
        defaults: {url: "http://open.mapquestapi.com/nominatim/v1/search"}
      });

  mapquest = new AddIn(mapquest);

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", mapquest);

  return mapquest;

});
