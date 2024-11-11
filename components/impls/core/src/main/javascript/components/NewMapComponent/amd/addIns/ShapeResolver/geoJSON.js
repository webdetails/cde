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


define([
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/Logger",
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, Logger, $, _) {
  "use strict";
  var geoJSON = {
    name: "geoJSON",
    label: "GeoJSON shape resolver",
    defaults: {
      url: "", //url for the resource containing the json map definitions
      idPropertyName: "" //GeoJSON feature property that will be used to index the feature
    },
    implementation: function(tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url) {
        $.ajax(url, {
          async: true,
          type: "GET",
          dataType: "json",
          success: function(json) {
            var map = toMappedGeoJSON(json, opt.idPropertyName);
            deferred.resolve(map);
          },
          error: function() {
            Logger.log("NewMapComponent geoJSON addIn: failed to retrieve data at" + url, "debug");
            deferred.resolve({});
          }
        });
      } else {
        Logger.log("NewMapComponent geoJSON addIn: no url is defined", "debug");
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function toMappedGeoJSON(json, idPropertyName) {
    var map = _.chain(json.features)
      .map(function(feature, idx) {
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

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(geoJSON));

  return geoJSON;
});
