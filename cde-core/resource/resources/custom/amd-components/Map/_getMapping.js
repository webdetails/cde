define([
  'amd!cdf/lib/underscore'
], function (_) {

  return getMapping;

  function getMapping(json) {
    var map = {};

    if (!json.metadata || json.metadata.length == 0)
      return map;

    //Iterate through the metadata. We are looking for the following columns:
    // * address or one or more of 'Country', 'State', 'Region', 'County', 'City'
    // * latitude and longitude - if found, we no longer care for address
    // * description - Description to show on mouseover
    // * marker - Marker image to use - usually this will be an url
    // * markerWidth - Width of the marker
    // * markerHeight - Height of the marker
    // * popupContents - Contents to show on popup window
    // * popupWidth - Width of the popup window
    // * popupHeight - Height of the popup window

    var colToPropertyMapping = { // colName -> property
      'key': 'id',
      'id': 'id',
      'fill': 'fill',
      'fillColor': 'fill',
      'r': 'r',
      // previously defined mappings
      'latitude': 'latitude',
      'longitude': 'longitude',
      'address': 'address',
      'description': 'description',
      'marker': 'marker', //iconUrl
      'markerwidth': 'markerWidth',
      'markerheight': 'markerHeight',
      'popupcontents': 'popupContents',
      'popupwidth': 'popupWidth',
      'popupheight': 'popupHeight'
    };

    var colNames = _.chain(json.metadata)
      .pluck('colName')
      .map(function (s) {
        return s.toLowerCase();
      })
      .value();

    var map = _.chain(colNames)
      .map(function (colName, idx) {
        var property = colToPropertyMapping[colName];
        if (property) {
          return [property, idx];
        } else {
          return [colName, idx]; //be permissive on the mapping
        }
      })
      .compact()
      .object()
      .value();

    if ('latitude' in map || 'longitude' in map) {
      map.addressType = 'coordinates';
    }
    if ('address' in map && !map.addressType) {
      map.addressType = 'address';
    }

    if (!map.id) {
      map.id = 0; //TODO: evaluate if this sort of hardcoding is really necessary
    }

    return map;
  }
});