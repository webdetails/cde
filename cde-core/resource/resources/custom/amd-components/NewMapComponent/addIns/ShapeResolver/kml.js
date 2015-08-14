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
    name: 'kml',
    label: 'KML shape resolver',
    defaults: {
      url: '', //url for the resource containing the kml data
      parseShapeKey: null
    },
    implementation: function (tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource,
          parseShapeKey = opt.parseShapeKey || st._parseShapeKey;

      if (url){
        $.ajax(url, {
          async: true,
          type: 'GET',
          processData: false,
          success: function(data) {
            var map = getShapeFromKML(data, parseShapeKey);
            //st.callback(data);
            deferred.resolve(map);
          },
          error: function(){
            //st.callback({});
            deferred.resolve({});
          }
        });
      } else {
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function getShapeFromKML(rawData, parseShapeKey){
    /*
      Parse a KML file, return a JSON dictionary where each key is associated with an array of shapes of the form
      mymap = {'Cascais:'[ [[lat0, long0],[lat1, long1]] ]}; // 1 array with a list of points
    */
    var mymap = {};

    $(rawData).find('Placemark').each( function(idx, y){
      var key;
      if ( _.isFunction(parseShapeKey) ){
        try {
          key = parseShapeKey(y);
        } catch (e) {
          key = $(y).find('name').text();
        }
      } else {
        key = $(y).find('name').text();
      }

      // Create an array for the strings that define the (closed) curves in a Placemark
      var polygonArray = _.map($(y).find('Polygon'), function (yy){
        var polygon = [];
        _.each(['outerBoundaryIs', 'innerBoundaryIs'], function (b) {
          var polygonObj = $(yy).find(b + ' LinearRing coordinates');
          //if (polygonObj.length >0){
          _.each(polygonObj, function (v) {
            var s = $(v).text().trim();
            if (s.length > 0){
              var p = _.map(s.split(' '), function(el){
                return _.map(el.split(',').slice(0,2), parseFloat);//.reverse();
              });
              //p =  this.reducePoints(p.slice(0, pp.length -1), precision_m); // this would reduce the number of points in the shape
              polygon.push( p );
            }
          });
          //}
        });
        return polygon;
      });
      if (_.isEmpty(polygonArray)){
        return;
      }
      if (!mymap[key]) {
        mymap[key] = multiPolygonToGeoJSON(polygonArray);
      }
    });

    return mymap;
  }

  function multiPolygonToGeoJSON(polygonArray){
    var feature = {
      type: 'Feature',
      geometry: {
        type: 'MultiPolygon',
        coordinates: polygonArray
      },
      properties: {}
    };
    return feature;
  }

  Dashboard.registerGlobalAddIn('NewMapComponent', 'ShapeResolver', new AddIn(thisAddIn));

});
