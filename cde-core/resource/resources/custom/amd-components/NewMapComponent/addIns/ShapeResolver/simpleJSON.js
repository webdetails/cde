/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define([
  'cdf/AddIn',
  'cdf/Dashboard.Clean',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore'
], function (AddIn, Dashboard, $, _) {

  var thisAddIn = {
    name: 'simpleJSON',
    label: 'Simple JSON shape resolver',
    defaults: {
      url: '' //url for the resource containing the json map definitions
    },
    implementation: function (tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url){
        $.ajax(url, {
          async: true,
          type: 'GET',
          dataType: 'json',
          success: function(latlonMap) {
            var map = _.chain(latlonMap)
                .map(function(multiPolygonLatLon, key){
                  return [key, multiPolygonReverseCoordinates(multiPolygonLatLon)];
                })
                .object()
                .value();
            deferred.resolve(map);
          },
          error: function(){
            deferred.resolve({});
          }
        });
      } else {
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function multiPolygonReverseCoordinates(latLonMultiPolygon) {
    var lonLatMultiPolygon = _.map(latLonMultiPolygon, function(polygon){
      return _.map(polygon, function(lineString){
        return _.map(lineString, function(point){
          return point.reverse();
        });
      });
    });
    return lonLatMultiPolygon;
  }


  Dashboard.registerGlobalAddIn('NewMapComponent', 'ShapeResolver', new AddIn(thisAddIn));
});
