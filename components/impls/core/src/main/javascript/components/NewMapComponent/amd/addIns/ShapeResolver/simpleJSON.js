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
  var simpleJSON = {
    name: "simpleJSON",
    label: "Simple JSON shape resolver",
    defaults: {
      url: "" //url for the resource containing the json map definitions
    },
    implementation: function(tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url) {
        $.ajax(url, {
          async: true,
          type: "GET",
          dataType: "json",
          success: function(latlonMap) {
            deferred.resolve(_.chain(latlonMap)
              .map(function(multiPolygonLatLon, key) {
                return [key, multiPolygonToGeoJSON(multiPolygonLatLon)];
              })
              .object()
              .value());
          },
          error: function() {
            deferred.resolve({});
          }
        });
      } else {
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function multiPolygonToGeoJSON(latLonMultiPolygon) {
    var lonLatMultiPolygon = _.map(latLonMultiPolygon, function(polygon) {
      return _.map(polygon, function(lineString) {
        return _.map(lineString, function(point) {
          return point.reverse();
        });
      });
    });

    return {
      type: "Feature",
      geometry: {
        type: "MultiPolygon",
        coordinates: lonLatMultiPolygon
      },
      properties: {}
    };
  }

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(simpleJSON));

  return simpleJSON;
});
