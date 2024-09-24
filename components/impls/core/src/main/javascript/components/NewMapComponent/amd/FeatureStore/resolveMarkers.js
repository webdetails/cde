/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function($, _) {
  "use strict";
  return resolveMarkers;

  function resolveMarkers(json, mapping, configuration) {
    var addIn = this.getAddIn("LocationResolver", configuration.addIns.LocationResolver.name);

    var deferred = $.Deferred();
    if (!addIn) {
      deferred.resolve({});
      return deferred.promise();
    }

    var tgt = this;
    var opts = this.getAddInOptions("LocationResolver", addIn.getName());
    var markerDefinitions;
    if (mapping.addressType === "coordinates") {
      markerDefinitions = _.chain(json.resultset)
        .map(function(row) {
          var id = row[mapping.id];
          var location = [row[mapping.longitude], row[mapping.latitude]];
          return [id, createFeatureFromLocation(location)];
        })
        .object()
        .value();

    } else {
      markerDefinitions = _.chain(json.resultset)
        .map(function(row, rowIdx) {
          var promisedLocation = $.Deferred();
          var id = row[mapping.id];
          var address = mapping.address != undefined ? row[mapping.address] : undefined;
          var st = {
            data: row,
            position: rowIdx,
            address: address,
            addressType: mapping.addressType,

            key: id, //TODO: deprecate 'key' in favour of 'id'
            id: id,
            mapping: mapping,
            tableData: json,
            continuationFunction: function(location) {
              promisedLocation.resolve(createFeatureFromLocation(location));
            }
          };
          var props = ["country", "city", "county", "region", "state"];
          _.each(_.pick(mapping, props), function(propIdx, prop) {
            if (propIdx != undefined) {
              st[prop] = row[propIdx];
            }
          });
          try {
            addIn.call(tgt, st, opts);
          } catch (e) {
            promisedLocation.resolve(null);
          }
          return [id, promisedLocation.promise()];
        })
        .object()
        .value();
    }

    deferred.resolve(markerDefinitions);
    return deferred.promise();
  }

  function createFeatureFromLocation(location) {
    var longitude = location[0];
    var latitude = location[1];
    var feature = {
      geometry: {
        coordinates: [longitude, latitude],
        type: "Point",
        properties: {
          latitude: latitude,
          longitude: longitude
        }
      },
      type: "Feature"
    };
    return feature;
  }

});
