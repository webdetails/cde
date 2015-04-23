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
;(function() {

  var geonames = {
    name: "geonames",
    label: "GeoNames",
    defaults: {
      username: ''
    },
    implementation: function (tgt, st, opt) {
      var location;
      var name = st.address;
      var featureClass;
      if (!name) {
        //Check city
        if (st.city) {
          name = st.city;
          featureClass = 'P';
        } else if (st.county) {
          name = st.county;
          featureClass = 'A';
        } else if (st.region) {
          name = st.region;
          featureClass = 'A';
        } else if (st.state) {
          name=st.state;
          featureClass = 'A';
        } else if (st.country) {
          name = st.country;
          featureClass = 'A';
        }
      }

      var url = 'http://ws.geonames.org/searchJSON';
      var data = {
        q: name.replace(/&/g,","),
        maxRows: 1,
        dataType: "json",
        username: opt.username,
        featureClass: featureClass
      };
      if (featureClass) {
        data.featureClass = featureClass;
      }
      var success = function (result) {
        if (result.geonames && result.geonames.length > 0) {
          location = [parseFloat(result.geonames[0].lng),
                      parseFloat(result.geonames[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(url, data, success);
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));

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
    implementation: function (tgt, st, opt) {
      if ( st.latitude || st.longitude){
          var location = [parseFloat(st.longitude),
                          parseFloat(st.latitude)];
          st.continuationFunction(location);
          return;
      }
        
      var params = $.extend(true, {}, opt.serviceParams);

      _.each(_.keys(st), function(key){
        if (_.isFunction(st[key])){
          return;
        }
        var keyLower = key.toLowerCase();
        if (keyLower in opt.mapping){
          params[ opt.mapping[ keyLower ] ] = st[key];
        } else {
          // unrecognized fields go here
          //params['q'] = [ (params['q'] || ''), st[key] ].join(', ');
        }

      });

      if (params['q']){
        // we can't have "q=" and the more specific fields simultaneously.
        // so we use only "q="
        params = {
          q:  params['q'] + ', ' + _.compact(_.map(opt.mapping, function(field){
            return params[field];
          })).join(', ')
        };
      }

      var success = function (result) {
        if (result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
                          parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      $.getJSON(opt.url, $.extend({}, opt.serviceParams, params), success);

    }
  };
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(nominatim));
  var mapquest = {};
  $.extend(true, mapquest, nominatim, {
    name: "mapquest",
    label: "MapQuest",
    defaults:{
      url: "http://open.mapquestapi.com/nominatim/v1/search"
    }
  });
  Dashboards.registerAddIn("NewMapComponent", "LocationResolver", new AddIn(mapquest));


  var componentPath = Dashboards.getWebAppPath() + '/content/pentaho-cdf-dd/resources/custom/components/NewMapComponent';
  var urlMarker = {
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl:  componentPath + '/images/marker_grey.png',
      imagePath: componentPath + '/images/',
      images: [
        'marker_grey.png',
        'marker_blue.png',
        'marker_grey02.png',
        'marker_orange.png',
        'marker_purple.png',
      ]
    },
    implementation: function (tgt, st, opt) {
      if (st.url)
        return st.url;
      if (st.position) {
        return opt.imagePath + opt.images[st.position % opt.images.length] || opt.defaultUrl;
      }
      return opt.defaultUrl;
    }
  };
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(urlMarker));

  var cggMarker = {
    name: "cggMarker",
    label: "CGG Marker",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var url = wd.helpers.cggHelper.getCggDrawUrl() + '?script=' + st.cggGraphName;

      var width = st.width;
      var height = st.height;
      var cggParameters = {};
      if (st.width) cggParameters.width = st.width;
      if (st.height) cggParameters.height = st.height;

      cggParameters.noChartBg = true;

      for (parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      // Check debug level and pass as parameter
      var level = Dashboards.debug;
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
  Dashboards.registerAddIn("NewMapComponent", "MarkerImage", new AddIn(cggMarker));

})();
