/**
 *
 * NewMapComponent
 *
 * Places markers on a map or represents values as a color code
 *

 Changelog 2014-02-27 -------------
 Support for representing data as a colored shape in a map,
 e.g. population of a country using a color map.

 New CDE properties:
 - mapMode = markers, shapes, just the map
 - colormap = [[0,0,0,255],[255,255,255,0]] (RGBA array for defining the colormap of the shapes)
 - shapeSource: file/url with the shape definitions
 - shapeMouseOver, shapeMouseOut, shapeMouseClick: callbacks for handling shapes
 - tilesets: which of the tilesets to use for rendering the map (added support for about 30 tilesets)

 Internal changes:
 - renamed OpenStreetMapEngine to OpenLayersEngine, modified mapEngineType enums to "openlayers" (default), "google"
 - added a bunch of functions to the map engines

 Features:

 1) SHAPES:
 Loading of shapes in the following file formats
 - JSON (not quite the same format as Kleyson's gmapsoverlay component)
 - KML (google earth)

 Goodies:
 - data can be exported to JSON format
 - possibility to reduce the number of points (useful for importing KML and exporting to JSON)
 - abstracted the interface to the map engines for simple tasks like changing the color of the shape on mouseover/click

 TODO:
 - smart detection of columns in the datasource, as it is currently done with the markers. Currently key=column[0], value = column[1]
 - ability to have both markers and shapes in the same datasource


 3) TODOs/Ideas
 - Use GeoJSON for internal representation of shapes
 - Write map engines for jvectormap, openlayers3
 - improve handling of markers and popups
 - implement firing of "marker:mouseout" and "marker:mouseover" events and add corresponding callbacks
 - generalize handling of colours (shapes currently support only RGBA )

 *
 */
var NewMapComponent = (function () {


  var ShapeConversion = {

    simplifyPoints: function (points, precision_m) {
      if (precision_m < 0) {
        return points;
      }
      function properRDP(points, epsilon) {
        /*
         *** Ramer Douglas Peucker, from http://karthaus.nl/rdp/js/rdp.js

         The Ramer-Douglas–Peucker algorithm is an algorithm for reducing the number of points in a curve that is approximated by a series of points.
         It does so by "thinking" of a line between the first and last point in a set of points that form the curve.
         It checks which point in between is farthest away from this line.
         If the point (and as follows, all other in-between points) is closer than a given distance 'epsilon', it removes all these in-between points.
         If on the other hand this 'outlier point' is farther away from our imaginary line than epsilon, the curve is split in two parts.
         The function is recursively called on both resulting curves, and the two reduced forms of the curve are put back together.

         1) From the first point up to and including the outlier
         2) The outlier and the remaining points.


         *** Bad implementations on the web
         On the web I found many Ramer Douglas Peucker implementations, but most of the top results on google contained bugs.
         Even the original example on Wikipedia was BAD!
         The bugs were ranging from bad calculation of the perpendicular distance of a point to a line (often they contained a devide by zero error for vertical lines),
         to discarding points that should not be removed at all.
         To see this in action, just try running the algorithm on it's own result with the same epsilon,
         many implementations will keep on reducing more and more points until there is no spline left.
         A correct implementation of RDP will remove *all* points that it can remove given a certain epsilon in the first run.

         I hope that by looking at this source code for my Ramer Douglas Peucker implementation you will be able to get a correct reduction of your dataset.

         @licence Feel free to use it as you please, a mention of my name is always nice.

         Marius Karthaus
         http://www.LowVoice.nl

         *
         */
        var firstPoint = points[0];
        var lastPoint = points[points.length - 1];
        if (points.length < 3) {
          return points;
        }
        var index = -1;
        var dist = 0;
        for (var i = 1; i < points.length - 1; i++) {
          var cDist = findPerpendicularDistance(points[i], firstPoint, lastPoint);
          if (cDist > dist) {
            dist = cDist;
            index = i;
          }
        }
        if (dist > epsilon) {
          // iterate
          var l1 = points.slice(0, index + 1);
          var l2 = points.slice(index);
          var r1 = properRDP(l1, epsilon);
          var r2 = properRDP(l2, epsilon);
          // concat r2 to r1 minus the end/startpoint that will be the same
          var rs = r1.slice(0, r1.length - 1).concat(r2);
          return rs;
        } else {
          return [firstPoint, lastPoint];
        }
      }

      function findPerpendicularDistance(p, p1, p2) {
        // if start and end point are on the same x the distance is the difference in X.
        var result;
        var slope;
        var intercept;
        if (p1[0] == p2[0]) {
          result = Math.abs(p[0] - p1[0]);
        } else {
          slope = (p2[1] - p1[1]) / (p2[0] - p1[0]);
          intercept = p1[1] - (slope * p1[0]);
          result = Math.abs(slope * p[0] - p[1] + intercept) / Math.sqrt(Math.pow(slope, 2) + 1);
        }

        return result;
      }

      return properRDP(points, precision_m / 6.3e6);

    }, // reducePoints

    exportShapeDefinition: function () {
      if (this.shapeDefinition) {
        window.open("data:text/json;charset=utf-8," + escape(JSON.stringify(this.shapeDefinition)));
      }
    }

  };


  var ColorMapMixin = {
    /** Mixin for handling color maps
     This should probably be elevated to a proper class with a nice database of colormaps
     */
    colormaps: {
      'jet': [],
      'gray': [[0, 0, 0, 255], [255, 255, 255, 255]],
      'french-flag': [[255, 0, 0, 255], [255, 254, 255, 255], [0, 0, 255, 255]]
    },
    getColorMap: function () {

      var colorMap = [];
      if (this.colormap == null || (_.isArray(this.colormap) && !this.colormap.length)) {
        colorMap = [[0, 102, 0, 255], [255, 255, 0, 255], [255, 0, 0, 255]]; //RGBA
      } else {
        for (var k = 0, L = this.colormap.length; k < L; k++) {
          colorMap.push(JSON.parse(this.colormap[k]));
        }
      }

      var interpolate = function (a, b, n) {
        var c = [], d = [];
        var k, kk, step;
        for (k = 0; k < a.length; k++) {
          c[k] = [];
          for (kk = 0, step = (b[k] - a[k]) / n; kk < n; kk++) {
            c[k][kk] = a[k] + kk * step;
          }
        }
        for (k = 0; k < c[0].length; k++) {
          d[k] = [];
          for (kk = 0; kk < c.length; kk++) {
            d[k][kk] = Math.round(c[kk][k]);
          }
        }
        return d;
      };
      var cmap = [];
      for (k = 1, L = colorMap.length; k < L; k++) {
        cmap = cmap.concat(interpolate(colorMap[k - 1], colorMap[k], 32));
      }
      return _.map(cmap, function (v) {
        return 'rgba(' + v.join(',') + ')';
      });
    },
    mapColor: function (value, minValue, maxValue, colormap) {
      var n = colormap.length;
      var level = (value - minValue) / (maxValue - minValue);
      return colormap[Math.floor(level * (n - 1))];
    }
  };

  /**
   * TileServices (servers providing png images representing the map)
   OpenStreetMaps default tiles are ugly, I found many nicer tilesets that work in both map engines (google/openlayers)
   To compare the various tilesets, visit http://mc.bbbike.org/mc/?num=2

   Example of valid values for the CDE property "tilesets"
   'mapquest'
   ['mapquest']
   ['mapquest', 'apple']
   'custom/static/localMapService/${z}/${x}/${y}.png'
   "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   */

  var _tileServices = {
    // list of tileset services that were tested and are working at 2013-11-04, see http://mc.bbbike.org/mc/?num=2 for comparison
    'default': "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
    'apple': "http://gsp2.apple.com/tile?api=1&style=slideshow&layers=default&lang=en_US&z=${z}&x=${x}&y=${y}&v=9",
    'google': "http://mt{switch:0,1,2,3}.googleapis.com/vt?x=${x}&y=${y}&z=${z}",
    'mapquest': "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
    'mapquest-normal': "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
    'mapquest-hybrid': "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/hyb/${z}/${x}/${y}.png", //MapQuest tile server
    'mapquest-sat': "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg", //MapQuest tile server
    'mapbox-world-light': "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-light/${z}/${x}/${y}.jpg",
    'mapbox-world-dark': "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-dark/${z}/${x}/${y}.jpg",
    'mapbox-terrain': 'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-9ijuk24y/${z}/${x}/${y}.jpg',
    'mapbox-satellite': 'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-qfyrx5r8/${z}/${x}/${y}.png',
    'mapbox-example': 'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.c7d2024a/${z}/${x}/${y}.png',
    'mapbox-example2': 'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.bc17bb2a/${z}/${x}/${y}.png',
    'openstreetmaps': "http://{switch:a,b,c}.tile.openstreetmap.org/${z}/${x}/${y}.png", //OSM tile server
    'openmapsurfer': 'http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-roads': 'http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-semitransparent': 'http://129.206.74.245:8003/tms_h.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-hillshade': 'http://129.206.74.245:8004/tms_hs.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-contour': 'http://129.206.74.245:8006/tms_b.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-administrative': 'http://129.206.74.245:8007/tms_b.ashx?x=${x}&y=${y}&z=${z}',
    'openmapsurfer-roads-grayscale': 'http://129.206.74.245:8008/tms_rg.ashx?x=${x}&y=${y}&z=${z}',
    // 'map.eu': "http://alpha.map1.eu/tiles/${z}/${y}/${x}.jpg",
    //'naturalearth': "http://www.staremapy.cz/naturalearth/${z}/${x}/${y}.png",
    'stamen': "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
    'stamen-terrain': "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
    'stamen-terrain-background': "http://{switch:a,b,c,d}.tile.stamen.com/terrain-background/${z}/${x}/${y}.jpg",
    'stamen-terrain-labels': "http://{switch:a,b,c,d}.tile.stamen.com/terrain-labels/${z}/${x}/${y}.jpg",
    'stamen-toner': "http://{switch:a,b,c,d}.tile.stamen.com/toner/${z}/${x}/${y}.png",
    'stamen-toner-lite': "http://{switch:a,b,c,d}.tile.stamen.com/toner-lite/${z}/${x}/${y}.png",
    'stamen-toner-background': "http://{switch:a,b,c,d}.tile.stamen.com/toner-background/${z}/${x}/${y}.png",
    'stamen-toner-hybrid': "http://{switch:a,b,c,d}.tile.stamen.com/toner-hybrid/${z}/${x}/${y}.png",
    'stamen-toner-labels': "http://{switch:a,b,c,d}.tile.stamen.com/toner-labels/${z}/${x}/${y}.png",
    'stamen-toner-lines': "http://{switch:a,b,c,d}.tile.stamen.com/toner-lines/${z}/${x}/${y}.png",
    'stamen-toner-2010': "http://{switch:a,b,c,d}.tile.stamen.com/toner-2010/${z}/${x}/${y}.png",
    'stamen-toner-2011': "http://{switch:a,b,c,d}.tile.stamen.com/toner-2011/${z}/${x}/${y}.png",
    'stamen-watercolor': "http://{switch:a,b,c,d}.tile.stamen.com/watercolor/${z}/${x}/${y}.jpg",
    'nokia-normal': 'http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/${z}/${x}/${y}/256/png8',
    'nokia-normal-grey': 'http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.grey/${z}/${x}/${y}/256/png8',
    'nokia-normal-transit': 'http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.transit/${z}/${x}/${y}/256/png8',
    'nokia-satellite': 'http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/satellite.day/${z}/${x}/${y}/256/png8',
    'nokia-terrain': 'http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/terrain.day/${z}/${x}/${y}/256/png8',
    'arcgis-street': 'http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Map/MapServer/tile/${z}/${y}/${x}',
    'arcgis-topographic': 'http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Topo/MapServer/tile/${z}/${y}/${x}',
    'arcgis-natgeo': 'http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/${z}/${y}/${x}',
    'arcgis-world': 'http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}',
    'arcgis-lightgray': 'http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer/tile/${z}/${y}/${x}',
    'arcgis-delorme': 'http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/${z}/${y}/${x}'
  };


  var MyClass = UnmanagedComponent.extend(ColorMapMixin).extend({
    ph: undefined, //perhaps this is not needed
    mapEngine: undefined, // points to one instance of a MapEngine object
    values: undefined,
    locationResolver: undefined, // addIn used to process location
    //shapeResolver: undefined, // addIn used to process location
    API_KEY: false, // API KEY for map services such as Google Maps
    // Properies defined in CDE
    //mapMode: ['', 'markers', 'shapes'][1],
    //shapeDefinition: undefined,
    //shapeSource: '',
    //tilesets: ['mapquest'],
    //colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
    tileServices: _tileServices,
    otherTileServices: [
      // These are tilesets using special code
      //'google'
    ],
    tileServicesOptions: {
      // WIP: interface for overriding defaults
      'apple': {minZoom: 3, maxZoom: 14}
    },
    // shapeMouseOver : function(event){
    //     Dashboards.log('Currently at lat=' + event.latitude + ', lng=' + event.longitude + ': Beat '+ event.data.key + ':' + event.data.value + ' crimes');
    //     return {
    //         fillColor: 'blue',
    //         fillOpacity: 1,
    //         strokeColor: 'white',
    //         strokeWidth: 2
    //     };

    // },
    // shapeMouseOut: undefined,
    // // function(event){
    // //         return event.style;
    // // },
    // shapeMouseClick: function(event){
    //     return {
    //         fillColor: 'red',
    //         fillOpacity: 1
    //     };
    // },
    // // End
    update: function () {
      this.registerEvents();
      if (_.isString(this.tilesets)) {
        this.tilesets = [this.tilesets];
      }

      if (this.testData) {
        this.render(this.testData);
        return;
      }
      Dashboards.log('Starting clock of ' + this.htmlObject, 'debug');
      this.clock = (new Date());

      if (this.queryDefinition && !_.isEmpty(this.queryDefinition)) {
        this.triggerQuery(this.queryDefinition, _.bind(this.onDataReady, this));
      } else {
        // No datasource, we'll just display the map
        this.synchronous(_.bind(this.render, this), {});
      }
    },

    onDataReady: function (json) {
      var idx = {
        id: 0,
        value: 1
      };

      var me = this;
      if ((this.mapMode == "shapes") && (!_.isEmpty(this.shapeSource))) {
        var keys = _.pluck(json.resultset, idx.id);
        this.dataRequest(this.shapeSource, keys)
            .then(function () {
              me.render.call(me, json);
            });
      } else {
        this.render(json);
      }
    },

    dataRequest: function (url, keys) {
      var addIn = this.getAddIn('ShapeResolver', this.shapeResolver);
      if (!addIn) {
        if (this.shapeSource.endsWith('json')) {
          addIn = this.getAddIn('ShapeResolver', 'simpleJSON');
        } else {
          addIn = this.getAddIn('ShapeResolver', 'kml');
        }
      }
      var deferred = $.Deferred();
      if (!addIn) {
        deferred.resolve({});
        return deferred.promise();
      }

      var tgt = this,
          st = {
            keys: keys,
            _simplifyPoints: ShapeConversion.simplifyPoints,
            _parseShapeKey: this.parseShapeKey,
            _shapeSource: url
          };
      var promise = addIn.call(tgt, st, this.getAddInOptions('ShapeResolver', addIn.getName()));
      var me = this;
      promise.then(function (result) {
        me.shapeDefinition = _.chain(result)
            .map(function (geoJSONFeature, key) {
              return [key, geoJSONFeature]; //decode geojson to native format
            })
            .object()
            .value();
        deferred.resolve(result);
      });
      return deferred.promise();

    },

    render: function (values) {
      //Dashboards.log('NewMapComponent.render: entered with arguments ' + JSON.stringify(arguments), 'debug')
      Dashboards.log('Stopping clock at render in ' + this.htmlObject + ': took ' + (new Date() - this.clock) + ' ms', 'debug');

      if (this.shapeDefinition) {
        Dashboards.log('Loaded ' + _.keys(this.shapeDefinition).length + ' shapes', 'debug');
      }

      if (this.mapEngineType == 'google') {
        this.mapEngine = new GoogleMapEngine();
      } else {
        this.mapEngine = new OpenLayersEngine();
      }

      this.values = values;
      $.extend(true, this.mapEngine, {
        API_KEY: this.API_KEY || window.API_KEY, //either local or global API_KEY
        tileServices: this.tileServices,
        tileServicesOptions: this.tileServicesOptions
      });
      this.mapEngine.init(this, this.tilesets);
    },

    initCallBack: function () {

      this.ph = this.placeholder();
      var $popupContentsDiv = $("#" + this.popupContentsDiv);
      var $popupDivHolder = $popupContentsDiv.clone();
      this.ph.empty(); //clear();
      //after first render, the popupContentsDiv gets moved inside ph, it will be discarded above, make sure we re-add him
      if (this.popupContentsDiv && $popupContentsDiv.length != 1) {
        this.ph.append($popupDivHolder.html("None"));
      }

      var centerLatitude = parseFloat(this.centerLatitude);
      var centerLongitude = parseFloat(this.centerLongitude);

       //centerLatitude = _.isFinite(centerLatitude) ? centerLatitude : 38.471;
       //centerLongitude = _.isFinite(centerLongitude) ? centerLongitude : -9.15;

      this.mapEngine.renderMap(this.ph[0]);

      switch (this.mapMode) {
        case 'shapes':
          this.setupShapes(this.values);
          break;
        case 'markers':
          this.setupMarkers(this.values);
          break;
      }

      this.mapEngine.updateViewport(centerLongitude, centerLatitude, this.defaultZoomLevel);

      Dashboards.log('Stopping clock: update cycle of ' + this.htmlObject + ' took ' + (new Date() - this.clock) + ' ms', 'debug');
    },

    registerEvents: function () {
      /** Registers handlers for mouse events
       *
       */
      var me = this;
      this.on('marker:click', function (event) {
        var result;
        if (_.isFunction(me.markerClickFunction)) {
          result = me.markerClickFunction(event);
        }
        if (result !== false) {
          // built-in click handler for markers
          me.markerClickCallback(event);
        }
      });

      // Marker mouseover/mouseout events are not yet completely supported
      // this.on('marker:mouseover', function(event){
      //   // Dashboards.log('Marker mouseover');
      // });
      // this.on('marker:mouseout', function(event){
      //   Dashboards.log('Marker mouseout');
      // });

      this.on('shape:mouseover', function (event) {
        // Dashboards.log('Shape mouseover');
        //this.mapEngine.showPopup(event.data,  event.feature, 50, 20, "Hello", undefined, 'red'); //Poor man's popup, only seems to work with OpenLayers
        if (_.isFunction(me.shapeMouseOver)) {
          var result = me.shapeMouseOver(event);
          if (result) {
            result = _.isObject(result) ? result : {};
            event.draw(_.defaults(result, {'zIndex': 1}, event.style));
          }
        }
      });

      this.on('shape:mouseout', function (event) {
        //Dashboards.log('Shape mouseout');
        var result = {};
        if (_.isFunction(me.shapeMouseOut)) {
          result = me.shapeMouseOut(event);
        }
        result = _.isObject(result) ? result : {};
        if (event.isSelected()) {
          event.draw(_.defaults(result, event.getSelectedStyle()));
        } else if (_.size(result) > 0) {
          event.draw(_.defaults(result, event.style));
        } else if (me.shapeMouseOver) {
          event.draw(event.style);
        }

      });

      this.on('shape:click', function (event) {
        if (_.isFunction(me.shapeMouseClick)) {
          var result = me.shapeMouseClick(event);
          if (result) {
            result = _.isObject(result) ? result : {};
            var selStyle = _.defaults(result, event.style);
            event.setSelectedStyle(selStyle);
            event.draw(selStyle);
          }
        }
      });
    },

    setupShapes: function (json) {
      if (!this.shapeDefinition) {
        return;
      }
      if (!json || !json.resultset) {
        return;
      }
      var myself = this;

      //Build an hashmap from metadata
      //var mapping = this.getMapping(values);
      //TODO: Discover automatically which columns correspond to the key and to the value
      var idxKey = 0,
          idxValue = 1;

      // Define default shape appearance
      var shapeSettings = _.defaults(this.shapeSettings || {}, {
        //fillColor:  'blue',
        //fillOpacity: 0.5,
        strokeWidth: 2,
        strokeColor: 'white',
        zIndex: 0
      });

      // Attribute a color each shape
      var colormap = this.getColorMap();
      var qvalues = _.pluck(json.resultset, idxValue);
      var minValue = _.min(qvalues),
          maxValue = _.max(qvalues);

      _.each(json.resultset, function (elt) {
        var fillColor = myself.mapColor(elt[idxValue], minValue, maxValue, colormap);
        var data = {
          rawData: elt,
          key: elt[idxKey],
          value: elt[idxValue],
          minValue: minValue,
          maxValue: maxValue
        };

        myself.renderShape(myself.shapeDefinition[elt[idxKey]], _.defaults({
          fillColor: fillColor
        }, shapeSettings), data);
      });
      this.mapEngine.postSetShapes(this);
    },

    renderShape: function (shapeDefinition, shapeSettings, data) {
      this.mapEngine.setShape(shapeDefinition, shapeSettings, data);
    },

    setupMarkers: function (json) {
      if (!json || !json.resultset)
        return;

      //Build an hashmap from metadata
      var mapping = this.getMapping(json);
      var myself = this;
      if (mapping.addressType != 'coordinates') {
        _.each(json.resultset, function (row, rowIdx) {
          var address = mapping.address != undefined ? row[mapping.address] : undefined;
          myself.getAddressLocation(address, mapping.addressType, row, mapping, rowIdx);
        });
      } else {
        _.each(json.resultset, function (row, rowIdx) {
          var location = [row[mapping.longitude], row[mapping.latitude]];
          myself.renderMarker(location, row, mapping, rowIdx);
        });
      }

    },

    renderMarker: function (location, row, mapping, position) {
      var myself = this;
      if (location === undefined) {
        Dashboards.log('Unable to get location for address ' + row[mapping.address] + '. Ignoring element.', 'debug');
        return true;
      }

      var markerIcon;
      var description;

      var markerWidth = myself.markerWidth;
      if (mapping.markerWidth) {
        markerWidth = row[mapping.markerWidth];
      }
      var markerHeight = myself.markerHeight;
      if (mapping.markerHeight) {
        markerHeight = row[mapping.markerHeight];
      }

      var defaultMarkers = false;

      if (mapping.marker) {
        markerIcon = row[mapping.marker];
      }
      if (markerIcon == undefined) {
        var st = {
          data: row,
          position: position
        };
        var addinName = this.markerImageGetter;

        //Check for cgg graph marker
        if (this.markerCggGraph) {
          st.cggGraphName = this.markerCggGraph;
          st.width = markerWidth;
          st.height = markerHeight;
          st.parameters = {};
          _.each(this.cggGraphParameters, function (parameter) {
            st.parameters[parameter[0]] = row[mapping[parameter[1]]];
          });
          addinName = 'cggMarker';
        } else {
          //Else resolve to urlMarker addin
          st.url = myself.marker;
          defaultMarkers = (myself.marker == undefined);
          addinName = 'urlMarker';
        }

        if (!addinName) {
          addinName = 'urlMarker';
        }
        var addIn = this.getAddIn("MarkerImage", addinName);
        markerIcon = addIn.call(this.ph, st, this.getAddInOptions("MarkerImage", addIn.getName()));
      }
      if (mapping.description) {
        description = row[mapping.description];
      }

      Dashboards.log('About to render ' + location[0] + ' / ' + location[1] + ' with marker sized ' + markerHeight + ' / ' + markerWidth + 'and description ' + description, 'debug');

      var markerInfo = { // hack to pass marker information to the mapEngine. This information will be included in the events
        longitude: location[0],
        latitude: location[1],
        defaultMarkers: defaultMarkers,
        position: position,
        mapping: mapping
      };

      myself.mapEngine.setMarker(location[0], location[1], markerIcon, description, row, markerWidth, markerHeight, markerInfo);
    },

    markerClickCallback: function (event) {
      var elt = event.data;
      var defaultMarkers = event.marker.defaultMarkers;
      var mapping = event.marker.mapping;
      var position = event.marker.position;
      _.each(this.popupParameters, function (eltA) {
        Dashboards.fireChange(eltA[1], event.data[mapping[eltA[0].toLowerCase()]]);
      });

      if (this.popupContentsDiv || mapping.popupContents) {
        var contents;
        if (mapping.popupContents) contents = elt[mapping.popupContents];
        var height = mapping.popupContentsHeight ? elt[mapping.popupContentsHeight] : undefined;
        var width = mapping.popupContentsWidth ? elt[mapping.popupContentsWidth] : undefined;
        height = height || this.popupHeight;
        width = width || this.popupWidth;
        //  if (!contents) contents = $("#" + myself.popupContentsDiv).html();

        var borderColor = undefined;
        if (defaultMarkers) {
          var borderColors = ["#394246", "#11b4eb", "#7a879a", "#e35c15", "#674f73"];
          borderColor = borderColors[position % 5];
        }
        this.mapEngine.showPopup(event.data, event.feature, height, width, contents, this.popupContentsDiv, borderColor);
      }
    },


    getAddressLocation: function (address, addressType, data, mapping, position) {

      var addinName = this.locationResolver || 'openstreetmap';
      var addIn = this.getAddIn("LocationResolver", addinName);

      var target = this.placeholder();
      var state = {
        address: address,
        addressType: addressType,
        position: position
      };

      var props = ['country', 'city', 'county', 'region', 'state'];
      _.each(_.pick(mapping, props), function (mappingProp, prop) {
        if (mappingProp != undefined) {
          state[prop] = data[mappingProp];
        }
      });
      /*
       if (mapping.country != undefined) state.country = data[mapping.country];
       if (mapping.city != undefined) state.city = data[mapping.city];
       if (mapping.county != undefined) state.county = data[mapping.county];
       if (mapping.region != undefined) state.region = data[mapping.region];
       if (mapping.state != undefined) state.state = data[mapping.state];
       */
      var myself = this;
      state.continuationFunction = function (location) {
        myself.renderMarker(location, data, mapping, position);
      };
      addIn.call(target, state, this.getAddInOptions("LocationResolver", addIn.getName()));
    },

    getMapping: function (json) {
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

      _.each(json.metadata, function (elt, i) {

        switch (elt.colName.toLowerCase()) {
          case 'latitude':
            map.addressType = 'coordinates';
            map.latitude = i;
            break;
          case 'longitude':
            map.addressType = 'coordinates';
            map.longitude = i;
            break;
          case 'description':
            map.description = i;
            break;
          case 'marker':
            map.marker = i;
            break;
          case 'markerwidth':
            map.markerWidth = i;
            break;
          case 'markerheight':
            map.markerHeight = i;
            break;
          case 'popupcontents':
            map.popupContents = i;
            break;
          case 'popupwidth':
            map.popupWidth = i;
            break;
          case 'popupheight':
            map.popupHeight = i;
            break;
          case 'address':
            if (!map.addressType) {
              map.address = i;
              map.addressType = 'address';
            }
            break;
          default:
            map[elt.colName.toLowerCase()] = i;
            break;
          // if ($.inArray(values.metadata[0].colName, ['Country', 'State', 'Region', 'County', 'City'])) {
        }

      });

      return map;
    }


  });

  return MyClass;
})();
