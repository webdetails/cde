define([
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore'
], function ($, _) {

  return resolveMarkers;

  function resolveMarkers(json, mapping, configuration) {
    var addIn = this.getAddIn('LocationResolver', configuration.addIns.LocationResolver.name);

    var deferred = $.Deferred();
    if (!addIn) {
      deferred.resolve({});
      return deferred.promise();
    }

    var tgt = this;
    var opts = this.getAddInOptions('LocationResolver', addIn.getName());
    var markerDefinitions;
    if (mapping.addressType === 'coordinates') {
      markerDefinitions = _.chain(json.resultset)
        .map(function (row) {
          var id = row[mapping.id];
          var location = [row[mapping.longitude], row[mapping.latitude]];
          return [id, createFeatureFromLocation(location)];
        })
        .object()
        .value();

    } else {
      markerDefinitions = _.chain(json.resultset)
        .map(function (row, rowIdx) {
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
            continuationFunction: function (location) {
              promisedLocation.resolve(createFeatureFromLocation(location));
            }
          };
          var props = ['country', 'city', 'county', 'region', 'state'];
          _.each(_.pick(mapping, props), function (propIdx, prop) {
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