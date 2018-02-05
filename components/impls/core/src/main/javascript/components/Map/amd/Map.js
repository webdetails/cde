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

/**
 *
 * NewMapComponent
 *
 * Places markers on a map or represents values as a color code
 *
 *
 * Changelog 2014-02-27 -------------
 * Support for representing data as a colored shape in a map,
 * e.g. population of a country using a color map.
 *
 * New CDE properties:
 * - mapMode = markers, shapes, just the map
 * - colormap = [[0,0,0,255],[255,255,255,0]] (RGBA array for defining the colormap of the shapes)
 * - shapeSource: file/url with the shape definitions
 * - shapeMouseOver, shapeMouseOut, shapeMouseClick: callbacks for handling shapes
 * - tilesets: which of the tilesets to use for rendering the map (added support for about 30 tilesets)
 *
 * Internal changes:
 * - renamed OpenStreetMapEngine to OpenLayersEngine, modified mapEngineType enums to "openlayers" (default), "google"
 * - added a bunch of functions to the map engines
 *
 * Features:
 *
 * 1) SHAPES:
 * Loading of shapes in the following file formats
 * - GeoJSON (used as the internal representation of shapes and markers)
 * - JSON (not quite the same format as Kleyson's gmapsoverlay component)
 * - KML (google earth)
 *
 * Goodies:
 * - possibility to reduce the number of points (useful for importing complex KML/GeoJSON files)
 *
 * TODO:
 * - ability to have both markers and shapes in the same datasource
 *
 *
 * 2) TILES (png images representing the map)
 * OpenStreetMaps default tiles are ugly, I found many nicer tilesets that work in both map engines (google/openlayers)
 * To compare the various tilesets, visit http://mc.bbbike.org/mc/?num=2
 *
 * Example of valid values for the CDE property "tilesets"
 * 'mapquest'
 * ['mapquest']
 * ['mapquest', 'apple']
 * 'custom/static/localMapService/${z}/${x}/${y}.png'
 * "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
 * "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
 *
 * 3) TODOs/Ideas
 *
 * - Write map engines for jvectormap, openlayers3
 * - improve handling of markers and popups
 * - implement firing of "marker:mouseout" and "marker:mouseover" events and add corresponding callbacks
 * - generalize handling of colours (shapes currently support only RGBA )
 *
 *
 */

define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "cdf/components/UnmanagedComponent",
  "./Map.lifecycle",
  "./Map.selector",
  "./Map.model",
  "./Map.configuration",
  "./Map.featureStyles",
  "./Map.colorMap",
  "./ControlPanel/ControlPanel",
  "./Map.tileServices",
  "./engines/openlayers2/MapEngineOpenLayers",
  "./engines/google/MapEngineGoogle",
  "./addIns/mapAddIns",
  "css!./Map"
], function($, _, UnmanagedComponent,
            ILifecycle,
            ISelector, IMapModel, IConfiguration, IFeatureStyle,
            IColorMap,
            ControlPanel,
            tileServices,
            OpenLayersEngine, GoogleMapEngine) {
  "use strict";
  return UnmanagedComponent.extend(ILifecycle)
    .extend(ISelector)
    .extend(IMapModel)
    .extend(IConfiguration)
    .extend(IFeatureStyle)
    .extend(IColorMap)
    .extend(tileServices)
    .extend({
      mapEngine: undefined, // points to one instance of a MapEngine object
      locationResolver: undefined, // addIn used to process location
      //shapeResolver: undefined, // addIn used to process location
      API_KEY: false, // API KEY for map services such as Google Maps
      // Properies defined in CDE
      //mapMode: ['', 'markers', 'shapes'][1],
      //shapeDefinition: undefined,
      //shapeSource: '',
      //tilesets: ['mapquest'],
      //colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
      // shapeMouseOver : function(event){
      //     Logger.log('Currently at lat=' + event.latitude + ', lng=' + event.longitude + ': Beat '+ event.data.key + ':' + event.data.value + ' crimes');
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
      update: function() {
        if (!this.preExec()) {
          return false;
        }
        this.maybeToggleBlock(true);

        this.configuration = this.getConfiguration();
        this._initMapEngine()
          .then(_.bind(this.init, this))
          .then(_.bind(function() {
            if (this.queryDefinition && !_.isEmpty(this.queryDefinition)) {
              this.getQueryData();
            } else {
              // No datasource, we'll just display the map
              this.onDataReady(this.testData || {});
            }
          }, this));
      },

      onDataReady: function(json) {
        return $.when(this.resolveFeatures(json))
          .then(_.bind(function(json) {
            this.initModel(json);
            this._initControlPanel();
            this.updateSelection();
            this._processMarkerImages();
          }, this))
          .then(_.bind(this.render, this))
          .then(_.bind(this._concludeUpdate, this));
      },

      _initMapEngine: function() {
        if (this.configuration.addIns.MapEngine.name === "google") {
          this.mapEngine = new GoogleMapEngine(this.configuration);
        } else {
          this.mapEngine = new OpenLayersEngine(this.configuration);
        }
        return this.mapEngine.init();
      },

      init: function() {
        var $map = $('<div class="map-container"/>');
        $map.css({
          position: "relative",
          overflow: "hidden",
          width: "100%",
          height: "100%"
        });
        $map.appendTo(this.placeholder().empty());
        this._relayMapEngineEvents();
        this._registerEvents();

        this.mapEngine.renderMap($map.get(0));
        this._initPopup();
      },

      _initControlPanel: function() {
        var $controlPanel = $('<div class="map-controls" />').prependTo(this.placeholder());
        this.controlPanel = new ControlPanel($controlPanel, this.model, this.configuration);
        this.controlPanel.render();
        var me = this;
        var eventMapping = {
          "zoom:in": _.bind(this.mapEngine.zoomIn, this.mapEngine),
          "zoom:out": _.bind(this.mapEngine.zoomOut, this.mapEngine)
        };

        _.each(eventMapping, function(callback, event) {
          if (_.isFunction(callback)) {
            me.listenTo(me.controlPanel, event, callback);
          }
        });
      },

      render: function() {
        this.mapEngine.render(this.model);
        var centerLatitude = this.configuration.viewport.center.latitude;
        var centerLongitude = this.configuration.viewport.center.longitude;
        var defaultZoomLevel = this.configuration.viewport.zoomLevel["default"];
        this.mapEngine.updateViewport(centerLongitude, centerLatitude, defaultZoomLevel);
      },

      _relayMapEngineEvents: function() {
        var engine = this.mapEngine;
        var component = this;
        var events = [
          "marker:click", "marker:mouseover", "marker:mouseout",
          "shape:click", "shape:mouseover", "shape:mouseout",
          "map:zoom", "map:center" //TODO: consider renaming these to viewport:zoom and viewport:center
        ];
        _.each(events, function(event) {
          component.listenTo(engine, event, function() {
            var args = _.union([event], arguments);
            component.trigger.apply(component, args);
          });
        });

        this.listenTo(this.mapEngine, "engine:selection:complete", function() {
          component.processChange();
        });

      },

      _registerEvents: function() {
        /** Registers handlers for mouse events
         *
         */
        var me = this;
        this.on("marker:click", function(event) {
          var result;
          if (_.isFunction(me.markerClickFunction)) {
            result = me.markerClickFunction(event);
          }
          if (result !== false && me.model.isPanningMode() && _.isEmpty(this.parameter)) {
            // built-in click handler for markers
            me.showPopup(event);
          }
        });

        // Marker mouseover/mouseout events are not yet completely supported
        //this.on("marker:mouseover", function (event) {
        //this.showPopup(event);
        //});
        // this.on("marker:mouseout", function(event){
        //this.hidePopup(event);
        // });
        function redrawUponCallback(event, callback, extraDefaults){
          var result = {};
          if (_.isFunction(callback)) {
            result = callback.call(me, event);
          }
          result = _.isObject(result) ? result : {};
          if (_.size(result) > 0) {
            event.draw(_.defaults(result, extraDefaults, event.style));
          }
        }

        this.on("shape:mouseover", function(event) {
          //this.showPopup(event);
          redrawUponCallback(event, me.shapeMouseOver, {"z-index": 1});
        });

        this.on("shape:mouseout", function(event) {
          redrawUponCallback(event, me.shapeMouseOut, {"z-index": 0});
        });

        this.on("shape:click", function(event) {
          redrawUponCallback(event, me.shapeMouseClick);
        });
      },

      _processMarkerImages: function() {
        var markersRoot = this.model.findWhere({id: "markers"});
        if (!markersRoot) {
          return;
        }

        var state = {
          height: this.configuration.addIns.MarkerImage.options.height,
          width: this.configuration.addIns.MarkerImage.options.width,
          url: this.configuration.addIns.MarkerImage.options.iconUrl
        };

        markersRoot.leafs()
          .each(_.bind(processRow, this))
          .value();

        function processRow(m) {
          var mapping = this.mapping || {};
          var row = m.get("rawData") || [];

          var st = $.extend(true, {}, state, {
            data: row,
            position: m.get("rowIdx"),
            height: row[mapping.markerHeight],
            width: row[mapping.markerWidth]
          });

          // Select addIn, consider all legacy special cases
          var addinName = this.configuration.addIns.MarkerImage.name,
            extraSt = {},
            extraOpts = {};
          if (addinName === "cggMarker") {
            extraSt = {
              cggGraphName: this.configuration.addIns.MarkerImage.options.cggScript,
              parameters: _.object(_.map(this.configuration.addIns.MarkerImage.options.parameters, function(parameter) {
                return [parameter[0], row[mapping[parameter[1]]]];
              }))
            };
          }

          // Invoke addIn
          var addIn = this.getAddIn("MarkerImage", addinName);
          if (!addIn) {
            return;
          }
          $.extend(true, st, extraSt);
          var opts = $.extend(true, {}, this.getAddInOptions("MarkerImage", addIn.getName()), extraOpts);
          var markerIcon = addIn.call(this.placeholder(), st, opts);

          // Update model's style
          if (_.isObject(markerIcon)) {
            $.extend(true, m.attributes.styleMap, markerIcon);
          } else {
            $.extend(true, m.attributes.styleMap, {
              width: st.width,
              height: st.height,
              "icon-url": markerIcon
            });
          }
        }
      },

      /**
       * Legacy stuff associated with popups that should be revised sometime
       */

      _initPopup: function() {
        if (this.popupContentsDiv) {
          var $popupContentsDiv = $("#" + this.popupContentsDiv);
          var $popupDivHolder = $popupContentsDiv.clone();
          //after first render, the popupContentsDiv gets moved inside ph, it will be discarded above, make sure we re-add him
          if (this.popupContentsDiv && $popupContentsDiv.length != 1) {
            this.placeholder().append($popupDivHolder.html("None"));
          }
        }
      },

      showPopup: function(event) {
        var data = event.data || [];
        var me = this;
        if (this.popupContentsDiv || data[me.mapping.popupContents]) {
          _.each(this.popupParameters, function(paramDef) {
            me.dashboard.fireChange(paramDef[1], data[me.mapping[paramDef[0].toLowerCase()]]);
          });

          var height = data[me.mapping.popupContentsHeight] || this.popupHeight;
          var width = data[me.mapping.popupContentsWidth] || this.popupWidth;
          var contents = data[me.mapping.popupContents] || $("#" + this.popupContentsDiv).html(); //TODO: revisit this

          // TODO: The following block should be revised, as it depends on too many parameters.
          // Why should we hardcode the border colors after all?
          var borderColor = "#394246";
          var isDefaultMarker = _.isUndefined(data.marker) && !this.markerCggGraph && _.isUndefined(me.marker) && me.configuration.addIns.MarkerImage.name === "urlMarker";
          if (isDefaultMarker) { // access defaultMarkers
            var borderColors = ["#394246", "#11b4eb", "#7a879a", "#e35c15", "#674f73"];
            borderColor = borderColors[event.model.get("rowIdx") % borderColors.length];
          }

          this.mapEngine.showPopup(event.data, event.feature, height, width, contents, this.popupContentsDiv, borderColor);
        }
      }

    });

});
