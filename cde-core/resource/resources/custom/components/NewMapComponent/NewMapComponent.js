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
 - consider supporting GeoJSON format

 2) TILES (png images representing the map)
 OpenStreetMaps default tiles are ugly, I found many nicer tilesets that work in both map engines (google/openlayers)
 To compare the various tilesets, visit http://mc.bbbike.org/mc/?num=2

 Example of valid values for the CDE property "tilesets"
 'mapquest'
 ['mapquest']
 ['mapquest', 'apple']
 'custom/static/localMapService/${z}/${x}/${y}.png'
 "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
 "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"

 3) TODOs/Ideas
 - Use GeoJSON for internal representation of shapes
 - Write map engines for jvectormap, openlayers3
 - improve handling of markers and popups
 - implement firing of "marker:mouseout" and "marker:mouseover" events and add corresponding callbacks
 - generalize handling of colours (shapes currently support only RGBA )

 *
 */
var NewMapComponent = (function (){


  var ShapeConversionMixin = {
    // Mixin for loading map shape data from a KML
    // Assume always that latitude and longitude are angular coordinates in decimal format (Google's default)

    parseShapeKey : undefined, // parseShapeKey(placemark): user-defined function that returns the key associated with a given shape
    getShapeFromKML: function (rawData) {
      /*
       Parse a KML file, return a JSON dictionary where each key is associated with an array of shapes of the form
       mymap = {'Cascais:'[ [[lat0, long0],[lat1, long1]] ]}; // 1 array with a list of points
       */
      var myself = this,
          mymap = {};

      var result = $(rawData).find('Placemark').each( function(idx, y){
        var key;
        if ( _.isFunction(myself.parseShapeKey) ){
          try {
            key = myself.parseShapeKey(y);
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
                  return _.map(el.split(',').slice(0,2), parseFloat).reverse();
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
        // var pp = _.map(polygonArray, function (polygon) {
        //     _.each(polygon, function (ppolygon) {
        //     var p = _.map(ppolygon.split(' '), function(el){
        //         return _.map(el.split(',').slice(0,2), parseFloat).reverse();
        //     });
        //        return p;
        //         });

        //var p =  this.reducePoints(pp.slice(0, pp.length -1), precision_m); // this would reduce the number of points in the shape
        //var p =pp;
        if (!mymap[key]) {
          mymap[key] = polygonArray;
        }// else {
        //    mymap[key].push([p.slice(0)]);
        //}
        //});
      });

      return mymap;
    },

    reducePoints: function (points, precision_m){
      if (precision_m < 0){
        return points;
      }
      function properRDP(points,epsilon){
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
        var firstPoint=points[0];
        var lastPoint=points[points.length-1];
        if (points.length<3){
          return points;
        }
        var index=-1;
        var dist=0;
        for (var i=1;i<points.length-1;i++){
          var cDist=findPerpendicularDistance(points[i],firstPoint,lastPoint);
          if (cDist>dist){
            dist=cDist;
            index=i;
          }
        }
        if (dist>epsilon){
          // iterate
          var l1=points.slice(0, index+1);
          var l2=points.slice(index);
          var r1=properRDP(l1,epsilon);
          var r2=properRDP(l2,epsilon);
          // concat r2 to r1 minus the end/startpoint that will be the same
          var rs=r1.slice(0,r1.length-1).concat(r2);
          return rs;
        }else{
          return [firstPoint,lastPoint];
        }
      }
      function findPerpendicularDistance(p, p1,p2) {
        // if start and end point are on the same x the distance is the difference in X.
        var result;
        var slope;
        var intercept;
        if (p1[0]==p2[0]){
          result=Math.abs(p[0]-p1[0]);
        }else{
          slope = (p2[1] - p1[1]) / (p2[0] - p1[0]);
          intercept = p1[1] - (slope * p1[0]);
          result = Math.abs(slope * p[0] - p[1] + intercept) / Math.sqrt(Math.pow(slope, 2) + 1);
        }

        return result;
      }
      return properRDP(points, precision_m/6.3e6);

    }, // reducePoints

    exportShapeDefinition: function () {
      if (this.shapeDefinition){
        window.open( "data:text/json;charset=utf-8," + escape(JSON.stringify(this.shapeDefinition)));
      }
    }

  };


  var ColorMapMixin = {
    /** Mixin for handling color maps
     This should probably be elevated to a proper class with a nice database of colormaps
     */
    colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
    colormaps: {
      'jet': [],
      'gray': [[0,0,0,255],[255,255,255,255]],
      'french-flag': [[255,0,0,255],[255,254,255,255], [0,0,255,255]]
    },
    getColorMap: function() {

      var interpolate = function(a, b, n){
        var c = [], d=[];
        var k, kk, step;
        for (k=0; k<a.length; k++){
          c[k] = [];
          for (kk=0, step = (b[k]-a[k])/n; kk<n; kk++){
            c[k][kk] = a[k] + kk*step;
          }
        }
        for (k=0; k<c[0].length; k++){
          d[k] = [];
          for (kk=0; kk<c.length; kk++){
            d[k][kk] =  Math.round(c[kk][k]);
          }
        }
        return d;
      };
      var cmap = [];
      for (k=1; k<this.colormap.length; k++)
      {
        cmap = cmap.concat(interpolate(this.colormap[k-1], this.colormap[k], 32));
      }
      return _.map( cmap, function (v) {
        return 'rgba('+ v.join(',') +')';
      });
    },
    mapColor: function (value, minValue, maxValue, colormap){
      var n = colormap.length;
      var level =  (value-minValue) / (maxValue - minValue);
      return colormap[Math.floor( level * (n-1)) ];
    }
  };

  var MyClass = UnmanagedComponent.extend(ShapeConversionMixin).extend(ColorMapMixin).extend({
    ph: undefined, //perhaps this is not needed
    mapEngine: undefined, // points to one instance of a MapEngine object
    values: undefined,
    locationResolver: undefined, // addIn used to process location
    API_KEY: false, // API KEY for map services such as Google Maps
    // Properies defined in CDE
    //shapeDefinition: undefined,
    //mapMode: ['', 'markers', 'shapes'][1],
    //shapeSource: '',
    //tilesets: ['mapquest'],
    //colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
    tileServices: {
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
      'mapbox-example':'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.c7d2024a/${z}/${x}/${y}.png',
      'mapbox-example2':'https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.bc17bb2a/${z}/${x}/${y}.png',
      'openstreetmaps':  "http://{switch:a,b,c}.tile.openstreetmap.org/${z}/${x}/${y}.png", //OSM tile server
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

    },
    otherTileServices: [
      // These are tilesets using special code
      //'google'
    ],
    tileServicesOptions: {
      // WIP: interface for overriding defaults
      'apple': {minZoom: 3, maxZoom:14}
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
    update: function(){
      this.registerEvents();
      if (_.isString(this.tilesets)){
        this.tilesets = [this.tilesets];
      }

      if (this.testData) {
        this.render(this.testData);
        return;
      }
      Dashboards.log('Starting clock of ' + this.htmlObject, 'debug');
      this.clock = (new Date());

      if ( this.queryDefinition && !$.isEmptyObject(this.queryDefinition) ){
        if ( (this.mapMode == "shapes") && (!_.isEmpty(this.shapeSource)) )  {
          // this.load_sequential();
          // this.load_deferred_hack();
          this.load_deferred();
        } else {
          this.triggerQuery(this.queryDefinition, _.bind(this.render, this));
        }
      } else {
        // No datasource, we'll just display the map
        this.synchronous(_.bind(this.render, this), {});
      }
    },

    dataRequest: function(url, callback){
      /* Small wrapper around $.ajax requests with a success callback
       * A $.Deferred() object is returned
       */
      return $.ajax(url, {
        async: true,
        type: 'GET',
        processData: url.endsWith('json')
      }).then(callback);
    },

    load_deferred_hack: function() {
      var myself = this;
      /* There are two possible approaches, concerning
       *
       */
      // In this variant, postExec is called ASAP, possibly before this.render is called
      var deferredQuery = function() {
        var _deferredQuery = $.Deferred(),
            myself = this;
        this.triggerQuery(this.queryDefinition, function(data){
          //myself.rawData = data;
          _deferredQuery.resolve(data);
        });
        return _deferredQuery.promise();
      };

      return $.when( this.dataRequest(this.shapeSource),
                     deferredQuery()
                   ).done( function(shapes, resultset){
                     myself.processShapeDefinition(shapes, function(){});
                     myself.render(resultset);
                   });
    },

    load_deferred: function() {
      var myself = this;

      // PreExec  must be called before any deferred is launched and before the call to deferredTriggerQuery
      if ( !this.preExec() ) {
        return null;
      }

      var deferreds = [
        this.dataRequest(myself.shapeSource, _.bind(this.processShapeDefinition, this))
      ];

      if (this.mapEngine == 'google'){
        deferreds.push( loadGoogleMaps('3',  this.API_KEY) );
      }

      // by returning a deferred object we are allowing the caller to do add more callbacks
      return this.deferredTriggerQuery( this.queryDefinition, deferreds, function (resultset){
        myself.render(resultset);
      });


    },
    deferredTriggerQuery: function (queryDef, deferreds, callback, userQueryOptions) {
      /*
       * Variation of the triggerQuery, supporting deferreds
       * The triggerQuery lifecycle handler builds a lifecycle around Query objects.
       *
       * It takes a query definition object that is passed directly into the Query
       * constructor, and the component rendering callback, and implements the full
       * preExecution->block->postFetch->render->postExecution->unblock lifecycle. This method
       * detects concurrent updates to the component and ensures that only one
       * redraw is performed.
       */

      var silent = this.isSilent();
      if (!silent){
        this.block();
      };
      userQueryOptions = userQueryOptions || {};
      /*
       * The query response handler should trigger the component-provided callback
       * and the postExec stage if the call wasn't skipped, and should always
       * unblock the UI
       */
      var success = _.bind(function(data) {
        // callback(data);
        //this.postExec();
        return data;
      }, this);
      var always = _.bind(function (){
        if (!silent){
          this.unblock();
        }
      }, this);

      this.getDeferredSuccessHandler = function(success){
        /* Code adapted from core.js/UnmanagedComponent.getSuccessHandler */
        var counter = this.callCounter();
        return _.bind(function(data) {
          var newData, result;
          if(counter >= this.runCounter) {
            try {
              if(typeof this.postFetch == "function") {
                newData = this.postFetch(data);
                this.trigger('cdf cdf:postFetch',this,data);
                data = typeof newData == "undefined" ? data : newData;
              }
              result = success(data);
            } catch(e) {
              this.error(Dashboards.getErrorObj('COMPONENT_ERROR').msg, e);
              this.dashboard.log(e,"error");
            }
          }
          // if(typeof always == "function") {
          // always();
          // }
          return result;
        }, this);
      };

      var querySuccess = this.getDeferredSuccessHandler( success ); //prepare handler that calls postFetch
      var queryError = this.getErrorHandler();

      var query = this.queryState = this.query = Dashboards.getQuery( queryDef );
      var ajaxOptions = {
        async: true
      };
      if(userQueryOptions.ajax) {
        _.extend(ajaxOptions,userQueryOptions.ajax);
      }
      query.setAjaxOptions(ajaxOptions);
      if(userQueryOptions.pageSize){
        query.setPageSize(userQueryOptions.pageSize);
      }


      // Setup a function that launches a query and returns a deferred object
      // Each deferred object is effectively a task that runs in parallel
      var myself = this;
      var launchQuery = function(){
        var _deferredQuery = $.Deferred();
        query.fetchData(myself.parameters, function(){
          /* handle postFetch */
          var data = querySuccess.apply(myself, arguments);
          var processedArgs = _.extend([], arguments); //A shallow extend is ok
          processedArgs[0] = data;
          _deferredQuery.resolve.apply(myself, processedArgs);
        }, function(){
          queryError.apply(myself, arguments);
          _deferredQuery.reject();
        });
        return _deferredQuery.promise().done(always); // danger: is this done or always?
      };

      var deferredSuccess = function(datasets){
        // typically render is called here
        if ( _.isFunction(callback) ){
          callback(datasets);
        }
        myself.postExec();
      };

      // Launch all tasks, run postExec once all are successful
      deferreds = deferreds || [];
      deferreds.unshift( launchQuery() ); //prepend deferred that gets the data
      return $.when.apply(null, deferreds).then( deferredSuccess );
    },

    triggerQuery: function (queryDef, callback, userQueryOptions) {
      var myself = this;

      // PreExec must be called before the call to deferredTriggerQuery
      if ( !this.preExec() ) {
        return;
      }
      this.deferredTriggerQuery( this.queryDefinition, null, function (resultset){
        if ( _.isFunction(callback) ){
          callback(resultset);
        }
      });
    },


    load_sequential : function() {
      /*
       Ensure the shapes are loaded before rendering, i.e., before attempting to draw the shapes.
       In this approach, the shapes are loaded after postFetch
       */
      var myself=this;
      this.triggerQuery( this.queryDefinition, function (resultset) {
        myself.getShapeDefinition( function(){
          myself.render(resultset);
        });
      });
    },


    getShapeDefinition : function(callback){
      Dashboards.log('NewMapComponent.getShapeDefinition: entered with arguments ' + JSON.stringify(arguments), 'debug');
      var myself = this;
      if (this.shapeSource && !_.isEmpty(this.shapeSource) && !this.shapeDefinition){
        var filetype = this.shapeSource.split('.').reverse()[0].toLowerCase(); // get extension
        $.ajax(this.shapeSource, {
          async: true,
          type: 'GET',
          processData: this.shapeSource.endsWith('json'),
          success: function(data) {
            myself.processShapeDefinition(data, callback);
          }
        });
      }
      // else {
      //     callback.apply(myself);
      //}
    },

    processShapeDefinition : function(data, callback) {
      var filetype = this.shapeSource.split('.').reverse()[0].toLowerCase(); // get extension
      if (data)  {
        switch (filetype){
        case 'kml':
          Dashboards.log('parsing shapes', 'debug');
          this.shapeDefinition = this.getShapeFromKML(data, this.parseShapeKey);
          break;
        case 'json':
          this.shapeDefinition = data;
          break;
        }
        if (_.isFunction( this.postProcessShape )) {
          this.shapeDefinition = this.postProcessShape(this.shapeDefinition);
        }
      }
      if (_.isFunction(callback)){
        Dashboards.log('NewMapComponent.processShapeDefinition: callback is a function', 'debug');
        callback(data);
      }
    },

    postProcessShape : function(shapeDefinition){
      /*
       Override this function e.g. to reduce the number of points in the shapes:

       // var myself = this,
       //      precision_m = 100;
       // _.each(shapeDefinition, function (points, key) {
       //     shapeDefinition[key] =  _.map(points, myself.reducePoints, precision_m);
       // });
       */
      return shapeDefinition;
    },

    render: function(values) {
      //Dashboards.log('NewMapComponent.render: entered with arguments ' + JSON.stringify(arguments), 'debug')
      Dashboards.log('Stopping clock at render in ' + this.htmlObject + ': took ' + (new Date() - this.clock) + ' ms', 'debug');

      if (this.shapeDefinition){
        Dashboards.log('Loaded ' + _.keys(this.shapeDefinition).length + ' shapes', 'debug');
      }

      if (this.mapEngineType == 'google') {
        this.mapEngine = new GoogleMapEngine();
      } else {
        this.mapEngine = new OpenLayersEngine();
      }

      this.values = values;
      this.mapEngine.API_KEY = this.API_KEY || window.API_KEY; //either local or global API_KEY
      this.mapEngine.tileServices = this.tileServices;
      this.mapEngine.tileServicesOptions = this.tileServicesOptions;
      this.mapEngine.init(this, this.tilesets);
    },

    initCallBack: function() {

      this.ph = $('#' + this.htmlObject);// this.placeholder();
      var $popupDivHolder = $("#"+this.popupContentsDiv).clone();
      this.ph.empty(); //clear();
      //after first render, the popupContentsDiv gets moved inside ph, it will be discarded above, make sure we re-add him
      if (this.popupContentsDiv && $("#"+this.popupContentsDiv).length != 1) {
        this.ph.append($popupDivHolder.html("None"));
      }
      this.mapEngine.renderMap(this.ph[0], this.centerLongitude, this.centerLatitude, this.defaultZoomLevel);

      switch (this.mapMode){
      case 'shapes':
        this.setupShapes(this.values);
        break;
      case 'markers':
        this.setupMarkers(this.values);
        break;
      }
      Dashboards.log('Stopping clock: update cycle of ' + this.htmlObject + ' took ' + (new Date() - this.clock) + ' ms', 'debug');
    },

    registerEvents: function(){
      /** Registers handlers for mouse events
       *
       */
      var myself = this;
      this.on('marker:click', function(event){
        // Dashboards.log('Marker click: ' + JSON.stringify(event.data));
        // $('<div/>').text('Hello').attr('title', 'My title').appendTo($('body')).dialog();
        var result;
        if (_.isFunction(this.markerClickFunction)){
            result = this.markerClickFunction(event);
        }
        if (result !== false){
          // built-in click handler for markers
          this.markerClickCallback(event);
        }
      });

      // Marker mouseover/mouseout events are not yet completely supported
      // this.on('marker:mouseover', function(event){
      //   // Dashboards.log('Marker mouseover');
      // });
      // this.on('marker:mouseout', function(event){
      //   Dashboards.log('Marker mouseout');
      // });

      this.on('shape:mouseover', function(event){
        // Dashboards.log('Shape mouseover');
        //this.mapEngine.showPopup(event.data,  event.feature, 50, 20, "Hello", undefined, 'red'); //Poor man's popup, only seems to work with OpenLayers
        if ( _.isFunction(myself.shapeMouseOver) ){
          var result = myself.shapeMouseOver(event);
          if (result){
            event.draw( _.defaults(result, {'zIndex': 1}, event.style) );
          }
        }
      });

      this.on('shape:mouseout', function(event) {
        //Dashboards.log('Shape mouseout');
        var result = {};
        if (_.isFunction(myself.shapeMouseOut)) {
          result = myself.shapeMouseOut(event);
        }
        if (event.feature == event.feature.layer.selectedFeatures[0]) {
          event.draw(_.defaults(result, event.raw.feature.attributes.clickSelStyle));
        } else if (_.size(result) > 0) {
          event.draw(_.defaults(result, event.style));
        } else if (myself.shapeMouseOver) {
          event.draw(event.style);
        }

      });

      this.on('shape:click', function(event){
        if ( _.isFunction(myself.shapeMouseClick) ){
          var result = myself.shapeMouseClick(event);
          if (result){
            var selStyle = _.defaults(result, event.style);
            event.raw.feature.attributes.clickSelStyle = selStyle;
            event.draw( selStyle );
          }
        }
      });
    },

    setupShapes: function (values) {
      if (!this.shapeDefinition) {
        return;
      }
      if (!values || !values.resultset){
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
      var qvalues = _.map(values.resultset, function (row) { return row[idxValue]; });
      var minValue = _.min(qvalues), maxValue = _.max(qvalues);

      $(values.resultset).each( function (i, elt) {
        var fillColor = myself.mapColor(elt[idxValue], minValue, maxValue, colormap);
        myself.renderShape( myself.shapeDefinition[elt[idxKey]], _.defaults({
          fillColor: fillColor
        }, shapeSettings),{
          rawData: elt,
          key : elt[idxKey],
          value: elt[idxValue],
          minValue: minValue,
          maxValue: maxValue
        });
      });
      this.mapEngine.postSetShapes(this);
    },

    renderShape:  function(shapeDefinition, shapeSettings, data) {
      this.mapEngine.setShape(shapeDefinition, shapeSettings, data);
    },

    setupMarkers: function (values)  {
      if (!values || !values.resultset)
        return;

      //Build an hashmap from metadata
      var mapping = this.getMapping(values);
      var myself = this;
      $(values.resultset).each(function (i, elt) {
        var location;
        if (mapping.addressType != 'coordinates') {
          location = myself.getAddressLocation((mapping.address != undefined ? elt[mapping.address]:undefined), mapping.addressType, elt, mapping, i);
        } else {
          location = [elt[mapping.longitude], elt[mapping.latitude]];
          myself.renderMarker(location, elt, mapping, i);
        }
      });

    },

    renderMarker: function(location, elt, mapping, position) {
      var myself = this;
      if (location === undefined) {
        Dashboards.log('Unable to get location for address ' + elt[mapping.address] + '. Ignoring element.', 'debug');
        return true;
      }

      var markerIcon;
      var description;

      var markerWidth = myself.markerWidth;
      if (mapping.markerWidth){
        markerWidth = elt[mapping.markerWidth];
      }
      var markerHeight = myself.markerHeight;
      if (mapping.markerHeight){
        markerHeight = elt[mapping.markerHeight];
      }

      var defaultMarkers = false;

      if (mapping.marker) {
        markerIcon = elt[mapping.marker];
      }
      if (markerIcon == undefined) {
        var st = {data: elt, position: position};
        var addinName = this.markerImageGetter;

        //Check for cgg graph marker
        if (this.markerCggGraph) {
          st.cggGraphName = this.markerCggGraph;
          st.width = markerWidth;
          st.height = markerHeight;
          st.parameters = {};
          $(this.cggGraphParameters).each (function (i, parameter) {
            st.parameters[parameter[0]] =  elt[mapping[parameter[1]]];
          });
          addinName = 'cggMarker';
        } else {
          //Else resolve to urlMarker addin
          st.url = myself.marker;
          defaultMarkers = (myself.marker == undefined);
          addinName = 'urlMarker';
        }

        if (!addinName){
          addinName = 'urlMarker';
        }
        var addIn = this.getAddIn("MarkerImage",addinName);
        markerIcon = addIn.call(this.ph, st, this.getAddInOptions("MarkerImage", addIn.name));
      }
      if (mapping.description) description = elt[mapping.description];

      Dashboards.log('About to render ' + location[0] + ' / ' + location[1] + ' with marker ' + marker + ' sized ' + markerHeight + ' / ' + markerWidth + 'and description ' + description, 'debug');

      var markerInfo = { // hack to pass marker information to the mapEngine. This information will be included in the events
        longitude: location[0],
        latitude: location[1],
        defaultMarkers: defaultMarkers,
        position: position,
        mapping: mapping
      };

      myself.mapEngine.setMarker(location[0], location[1], markerIcon, description, elt, markerWidth, markerHeight, markerInfo);
    },

    markerClickCallback: function(event){
      var myself = this;
      var elt = event.data;
      var defaultMarkers = event.marker.defaultMarkers;
      var mapping = event.marker.mapping;
      var position = event.marker.position;
      $(this.popupParameters).each(function (i, eltA) {
        Dashboards.fireChange(eltA[1], event.data[ mapping[ eltA[0].toLowerCase() ] ]);
      });

        if (this.popupContentsDiv || mapping.popupContents) {
          var contents;
          if (mapping.popupContents) contents = elt[mapping.popupContents];
          var height = mapping.popupContentsHeight ? elt[mapping.popupContentsHeight] : undefined;
          if (!height) height = this.popupHeight;
          var width = mapping.popupContentsWidth? elt[mapping.popupContentsWidth] : undefined;
          if (!width) width = this.popupWidth;
          //                    if (!contents) contents = $("#" + myself.popupContentsDiv).html();

          var borderColor = undefined;
          if (defaultMarkers) {
            var borderColors = ["#394246", "#11b4eb", "#7a879a", "#e35c15", "#674f73"];
            borderColor = borderColors[position % 5];
          }
          this.mapEngine.showPopup(event.data,  event.feature, height, width, contents, this.popupContentsDiv, borderColor);
        }
    },




    getAddressLocation: function (address, addressType, data, mapping, position) {

      var addinName = this.locationResolver;
      if (!addinName) addinName = 'openstreetmap';
      var addIn = this.getAddIn("LocationResolver",addinName);

      var target = this.ph;
      var state = {address: address, addressType: addressType, position: position};
      if (mapping.country != undefined) state.country = data[mapping.country];
      if (mapping.city != undefined) state.city = data[mapping.city];
      if (mapping.county != undefined) state.county = data[mapping.county];
      if (mapping.region != undefined) state.region = data[mapping.region];
      if (mapping.state != undefined) state.state = data[mapping.state];
      var myself = this;
      state.continuationFunction = function (location) {
        myself.renderMarker(location, data, mapping, position);
      };
      addIn.call(target, state, this.getAddInOptions("LocationResolver", addIn.getName()));
    },

    getMapping: function(values) {
      var map = {};

      if(!values.metadata || values.metadata.length == 0)
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

      $(values.metadata).each(function (i, elt) {

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