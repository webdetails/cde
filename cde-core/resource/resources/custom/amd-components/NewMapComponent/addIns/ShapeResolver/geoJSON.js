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
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore'
], function (AddIn, Dashboard, Logger, $, _) {

  var thisAddIn = {
    name: "geoJSON",
    label: "GeoJSON shape resolver",
    defaults: {
      url: '', //url for the resource containing the json map definitions
      idPropertyName: '' //GeoJSON feature property that will be used to index the feature
    },
    implementation: function (tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url) {
        $.ajax(url, {
          async: true,
          type: 'GET',
          dataType: 'json',
          success: function (json) {
            var map = toMappedGeoJSON(json, opt.idPropertyName);
            deferred.resolve(map);
          },
          error: function () {
            Logger.log('NewMapComponent geoJSON addIn: failed to retrieve data at' + url, 'debug');
            deferred.resolve({});
          }
        });
      } else {
        Logger.log('NewMapComponent geoJSON addIn: no url is defined', 'debug');
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function toMappedGeoJSON(json, idPropertyName) {
    var map = _.chain(json.features)
      .map(function (feature, idx) {
        var id = getFeatureId(feature, idPropertyName) || idx;
        return [id, feature];
      })
      .object()
      .value();
    return map;
  }

  function getFeatureId(feature, idPropertyName) {
    var id = feature.id;
    if (idPropertyName) {
      id = feature.properties[idPropertyName] || id;
    }
    return id;
  }

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(thisAddIn));
});
