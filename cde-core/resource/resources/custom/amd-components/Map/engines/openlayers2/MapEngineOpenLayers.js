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
 * the license for the specific language governing your rights and limitatio

 /*
 * OpenLayers engine.
 *
 */
define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "../MapEngine",
  "cdf/lib/OpenLayers",
  "../../model/MapModel",
  "../google/MapComponentAsyncLoader",
  "css!./styleOpenLayers2"
], function($, _, MapEngine, OpenLayers, MapModel, loadGoogleMaps) {
  "use strict";
  return MapEngine.extend({
    map: undefined,
    //    featureLayer: undefined,
    API_KEY: 0,
    constructor: function(options) {
      this.base();
      //$.extend(this, options);
      this.options = options;
      this.layers = {}; // map layers
      this.controls = {}; // map controls
    },

    init: function() {
      var hasGoogle = _.some(this.options.tiles.tilesets, function(t) {
        return t.indexOf("google") > -1;
      });
      if (hasGoogle) {
        return $.when(loadGoogleMaps("3", this.options.API_KEY));
      } else {
        return this.base();
      }
    },

    toNativeStyle: function(foreignStyle) {
      var conversionTable = {
        // SVG standard attributes : OpenLayers2 attributes
        "fill": "fillColor",
        "fill-opacity": "fillOpacity",
        "stroke": "strokeColor",
        "stroke-opacity": "strokeOpacity",
        "stroke-width": "strokeWidth",
        "r": "pointRadius",
        "z-index": "graphicZIndex",
        "icon-url": "externalGraphic",
        "iconUrl": "externalGraphic",
        "width": "graphicWidth",
        "height": "graphicHeight",
        //Adapted
        "symbol": "graphicName",
        //Backwards compatibility
        "fillColor": "fillColor",
        "fillOpacity": "fillOpacity",
        "strokeColor": "strokeColor",
        "strokeOpacity": "strokeOpacity",
        "strokeWidth": "strokeWidth",
        "zIndex": "graphicZIndex"
      };
      var validStyle = {};
      _.each(foreignStyle, function(value, key) {
        var nativeKey = conversionTable[key];
        if (nativeKey) {
          validStyle[nativeKey] = value;
        } else {
          switch (key) {
            case "visible":
              validStyle["display"] = value ? true : "none";
              break;
            default:
              // be permissive about the validation
              validStyle[key] = value;
              break;
          }
        }
      });
      //console.log('foreign vs valid:', foreignStyle, validStyle);
      return validStyle;
    },

    wrapEvent: function(event) {
      var feature = event.feature;
      var modelItem = event.feature.attributes.model;

      var lastXy = this.controls.mousePosition.lastXy; // || {x: undefined, y: undefined};
      var coords;
      if (lastXy) {
        coords = this.map.getLonLatFromPixel(lastXy)
          .transform(this.map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
      } else {
        coords = {lat: undefined, lon: undefined};
      }

      var me = this;
      return $.extend(this._wrapEvent(modelItem), {
        mapEngineType: "openlayers2",
        latitude: coords.lat,
        longitude: coords.lon,
        feature: feature, // can refer to either the shape or the marker
        _popup: function(html, options) {
          var opt = $.extend({
            width: 100,
            height: 100
          }, options || {});
          me.showPopup(null, feature, opt.height, opt.width, html, null, null);
        },
        draw: function(style) {
          // currently only makes sense to be called on shape callbacks
          var validStyle = me.toNativeStyle(style);
          event.feature.layer.drawFeature(feature, validStyle);
        },
        // TODO: BEGIN code that must die
        _setSelectedStyle: function(style) {
          event.feature.attributes.clickSelStyle = style;
        },
        _getSelectedStyle: function() {
          return event.feature.attributes.clickSelStyle;
        },
        // END code that might need to die
        raw: event
      });
    },

    renderItem: function(modelItem) {
      if (!modelItem) {
        return;
      }
      var layer = this.layers[modelItem.root().children().first().get("id")];
      var geoJSON = modelItem.get("geoJSON");
      var me = this;
      $.when(geoJSON).then(function(feature) {
        if (!feature) {
          return;
        }
        var f = me._geoJSONParser.parseFeature(feature);
        var style = modelItem.getStyle();
        $.extend(true, f, {
          attributes: {
            id: modelItem.get("id"),
            model: modelItem
          },
          style: me.toNativeStyle(style)
        });
        layer.addFeatures([f]);
      });
    },

    showPopup: function(data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      if (popupContentDiv && popupContentDiv.length > 0) {
        var div = $("<div/>");
        div.append($("#" + popupContentDiv));
        contents = div.html();
      }

      var name = "featurePopup";
      if (borderColor !== undefined) {
        name = name + borderColor.substring(1);
      }

      var p = feature.geometry.getCentroid(); // Hack to get the point
      feature.lonlat = new OpenLayers.LonLat(p.x, p.y);

      var popup = new OpenLayers.Popup.Anchored(name,
        feature.lonlat,
        new OpenLayers.Size(popupWidth, popupHeight),
        contents,
        null, true, null);

      feature.popup = popup;
      popup.feature = feature;

      _.each(this.map.popups, function(elt) {
        elt.hide(); //possible memory leak?
      });

      this.map.addPopup(popup, true);
    },

    renderMap: function(target) {
      var projectionMap = new OpenLayers.Projection("EPSG:900913");
      var projectionWGS84 = new OpenLayers.Projection("EPSG:4326");
      var extent = this.options.viewport.extent;
      var restrictedExtent = new OpenLayers.Bounds(
        extent.southEast.longitude, extent.southEast.latitude,
        extent.northWest.longitude, extent.northWest.latitude
      ).transform(projectionWGS84, projectionMap);

      var mapOptions = {
        zoom: this.options.viewport.zoomLevel["default"],
        zoomDuration: 10, // approximately match Google's zoom animation
        displayProjection: projectionWGS84,
        restrictedExtent: restrictedExtent,
        projection: projectionMap,
        controls: [
          //new OpenLayers.Control.PinchZoom(),
          //new OpenLayers.Control.LayerSwitcher({'ascending': false}),
          new OpenLayers.Control.ScaleLine(),
          new OpenLayers.Control.Attribution()
        ]
      };
      if (OpenLayers.TileManager) {
        mapOptions.tileManager = new OpenLayers.TileManager();
      }
      this.map = new OpenLayers.Map(target, mapOptions);
      this.$map = $(target);

      var me = this;
      this.map.isValidZoomLevel = function(z) {
        var zoomLevelConfig = me.options.viewport.zoomLevel;
        var minZoom = _.isFinite(zoomLevelConfig.min) ? zoomLevelConfig.min : 0;
        var maxZoom = _.isFinite(zoomLevelConfig.max) ? zoomLevelConfig.max : this.getNumZoomLevels();
        return (z != null) && (z >= minZoom) && (z <= maxZoom);
      };

      this.addLayers();
      this.addControls();
      this.registerViewportEvents();

      this._geoJSONParser = new OpenLayers.Format.GeoJSON({
        ignoreExtraDims: true,
        internalProjection: this.map.getProjectionObject(),
        externalProjection: projectionWGS84
      });
    },

    addLayers: function() {
      var me = this;
      _.each(this.options.tiles.tilesets, function(thisTileset) {
        var layer;
        var tilesetId = _.isString(thisTileset) ? thisTileset : thisTileset.id;
        var tileset = tilesetId.slice(0).split("-")[0];
        var variant = tilesetId.slice(0).split("-").slice(1).join("-") || "default";
        switch (tileset) {
          case "google":
            var mapOpts = {
              "default": {
                type: google.maps.MapTypeId.ROADMAP
              },
              "roadmap": {
                type: google.maps.MapTypeId.ROADMAP
              },
              "terrain": {
                type: google.maps.MapTypeId.TERRAIN
              },
              "satellite": {
                type: google.maps.MapTypeId.SATELLITE
              },
              "hybrid": {
                type: google.maps.MapTypeId.HYBRID
              }
            };

            layer = new OpenLayers.Layer.Google(tilesetId, mapOpts[variant]);
            break;

          case "opengeo":
            layer = new OpenLayers.Layer.WMS(tilesetId,
              "http://maps.opengeo.org/geowebcache/service/wms", {
                layers: variant,
                bgcolor: "#A1BDC4"
              }, {
                wrapDateLine: true,
                transitionEffect: "resize"
              });
            break;

          default:
            layer = me.tileLayer(tilesetId);
            break;
        }

        // add the OpenStreetMap layer to the map
        me.map.addLayer(layer);
        me.layers[tilesetId] = layer;
      });

      // add layers for the markers and for the shapes
      this.layers.shapes = new OpenLayers.Layer.Vector("Shapes", {
        //styleMap: olStyleMap,
        rendererOptions: {
          zIndexing: true
        }
      });

      this.layers.markers = new OpenLayers.Layer.Vector("Markers");

      this.map.addLayers([this.layers.shapes, this.layers.markers]);
    },

    setPanningMode: function() {
      this.controls.clickCtrl.activate();
      this.controls.zoomBox.deactivate();
      this.controls.boxSelector.deactivate();
      this._updateMode("pan");
    },

    setZoomBoxMode: function() {
      this.controls.clickCtrl.activate();
      this.controls.zoomBox.activate();
      this.controls.boxSelector.deactivate();
      this._updateMode("zoombox");
    },

    setSelectionMode: function() {
      this.controls.clickCtrl.deactivate();
      this.controls.boxSelector.activate();
      this.controls.zoomBox.deactivate();
      this._updateMode("selection");
    },

    zoomIn: function() {
      this.map.zoomIn();
    },

    zoomOut: function() {
      this.map.zoomOut();
    },

    updateViewport: function(centerLongitude, centerLatitude, zoomLevel) {
      var bounds;
      if (_.isFinite(zoomLevel)) {
        this.map.zoomTo(zoomLevel);
      } else {
        bounds = new OpenLayers.Bounds();
        var markersBounds = this.layers.markers.getDataExtent();
        var shapesBounds = this.layers.shapes.getDataExtent();
        if (markersBounds || shapesBounds) {
          bounds.extend(markersBounds);
          bounds.extend(shapesBounds);
        } else {
          bounds = null;
        }
        if (bounds) {
          this.map.zoomToExtent(bounds);
        } else {
          this.map.zoomTo(this.options.viewport.zoomLevel["default"]);
        }
      }

      var projectionWGS84 = new OpenLayers.Projection("EPSG:4326");
      var centerPoint;
      if (_.isFinite(centerLatitude) && _.isFinite(centerLongitude)) {
        centerPoint = (new OpenLayers.LonLat(centerLongitude, centerLatitude))
          .transform(projectionWGS84, this.map.getProjectionObject());
        this.map.setCenter(centerPoint);
      } else {
        if (!bounds) {
          centerPoint = (new OpenLayers.LonLat(-10, 20))
            .transform(projectionWGS84, this.map.getProjectionObject());
          this.map.setCenter(centerPoint);
        }
      }
    },

    addControls: function() {
      this._addControlKeyboardNavigation();
      this._addControlMouseNavigation();
      this._addControlMousePosition();
      this._addControlHover();
      this._addControlClick();
      this._addControlBoxSelector();
      this._addControlZoomBox();
    },

    _addControlKeyboardNavigation: function() {
      var allowKeyboard = (this.options.controls.enableKeyboardNavigation === true);
      this.controls.keyboardNavigation = new OpenLayers.Control.KeyboardDefaults({
        //observeElement: this.map.div
      });
      this.map.addControl(this.controls.keyboardNavigation);
      if (allowKeyboard) {
        this.controls.keyboardNavigation.activate();
      } else {
        this.controls.keyboardNavigation.deactivate();
      }
    },

    __patchDragHandler: function(handler) {
      // NOTE: the following code is very fragile, might break if we change the library version from 2.13.1
      var me = this;
      handler.down = function() {
        me._updateDrag(true);
      };
      handler.up = function() {
        me._updateDrag(false);
      };
      //handler.stopDown = false;
    },

    _addControlMouseNavigation: function() {
      var allowZoom = (this.options.controls.enableZoomOnMouseWheel === true);
      this.controls.mouseNavigation = new OpenLayers.Control.Navigation({
        zoomWheelEnabled: allowZoom
      });
      this.map.addControl(this.controls.mouseNavigation);
      this.__patchDragHandler(this.controls.mouseNavigation.dragPan.handler);

      this.controls.touchNavigation = new OpenLayers.Control.TouchNavigation();
      this.map.addControl(this.controls.touchNavigation);
      if (allowZoom) {
        this.controls.touchNavigation.activate();
      } else {
        this.controls.touchNavigation.deactivate();
      }
    },

    _addControlMousePosition: function() {
      this.controls.mousePosition = new OpenLayers.Control.MousePosition();
      this.map.addControl(this.controls.mousePosition);
    },

    _addControlClick: function() {
      this.controls.clickCtrl = new OpenLayers.Control.SelectFeature([this.layers.markers, this.layers.shapes], {
        clickout: true,
        callbacks: {
          clickout: this._createClickHandler(null, doZoomIn),
          click: function(feature) {
            this.clickFeature(feature);
            var modelItem = feature.attributes.model;
            var eventName = modelItem.getFeatureType() + ":click";
            me.trigger(eventName, me.wrapEvent({feature: feature}));
          }
        }
      });
      // allowing event to travel down
      this.controls.clickCtrl.handlers["feature"].stopDown = false;
      this.map.addControl(this.controls.clickCtrl);
      var me = this;
      this.controls.clickCtrl.events.on({
        "activate": function(e) {
          me._updateDrag(false);
        }
      });

    },

    _addControlBoxSelector: function() {
      var me = this;
      // add box selector controler
      this.controls.boxSelector = new OpenLayers.Control.SelectFeature([this.layers.shapes, this.layers.markers], {
        clickout: true,
        toggle: true,
        multiple: true,
        hover: false,
        box: true,
        callbacks: {
          clickout: this._createClickHandler(doClearSelection, doZoomIn),
          click: this._createClickHandler(doToggleSelection, doToggleSelection)
        }
      });
      this.map.addControl(this.controls.boxSelector);
      //TODO: apply modelItem.setHover(true) on all items inside the box
      this.__patchDragHandler(this.controls.boxSelector.handlers.box.dragHandler);
      this.controls.boxSelector.events.on({
        "activate": function(e) {
          e.object.unselectAll();
          
          var features = _.union(e.object.layers[0].features, e.object.layers[1].features);
          var selectedItems = me.model.getSelectedItems();
          _.each( features, function(f){
            var id = f.attributes.model.get('id');
            if(_.contains(selectedItems, id)){
              e.object.select(f);
            } 
          })
          
          me._updateDrag(false);
        },
        "boxselectionstart": function(e) {
          e.object.unselectAll();
        },
        "boxselectionend": function(e) {
          _.each(e.layers, function(layer) {
            _.each(layer.selectedFeatures, function(f) {
              addToSelection(f.attributes.model);
            });
          });
          e.object.unselectAll();
          me.trigger("engine:selection:complete");
        }
      });

    },

    _addControlZoomBox: function() {
      // add zoom box controler
      this.controls.zoomBox = new OpenLayers.Control.ZoomBox({
        zoomOnClick: !true
      });
      this.map.addControl(this.controls.zoomBox);
      var me = this;
      this.controls.zoomBox.events.on({
        "activate": function(e) {
          me._updateDrag(false);
        }
      });
      this.__patchDragHandler(this.controls.zoomBox.handler.dragHandler);
    },

    _addControlHover: function() {
      var me = this;

      function event_relay(e) {
        var events = {
          "featurehighlighted": "mouseover",
          "featureunhighlighted": "mouseout"
        };
        //console.log('hoverCtrl', featureType, e.type, styles[e.type]);
        if (events[e.type]) {
          var model = e.feature.attributes.model;
          model.setHover(events[e.type] === "mouseover");
          me.trigger(model.getFeatureType() + ":" + events[e.type], me.wrapEvent(e));
        }
      }

      this.controls.hoverCtrl = new OpenLayers.Control.SelectFeature([this.layers.markers, this.layers.shapes], {
        hover: true,
        highlightOnly: true,
        renderIntent: "temporary",
        eventListeners: {
          featurehighlighted: event_relay,
          featureunhighlighted: event_relay
        },
        //// this version of OpenLayers has issues with the outFeature function
        // this version of the function patches those issues
        // code from -> http://osgeo-org.1560.x6.nabble.com/SelectFeature-outFeature-method-tt3890333.html#a4988237
        outFeature: function(feature) {
          if (this.hover) {
            if (this.highlightOnly) {
              // we do nothing if we're not the last highlighter of the
              // feature
              if (feature._lastHighlighter == this.id) {
                // if another select control had highlighted the feature before
                // we did it ourself then we use that control to highlight the
                // feature as it was before we highlighted it, else we just
                // unhighlight it
                if (feature._prevHighlighter &&
                  feature._prevHighlighter != this.id) {
                  delete feature._lastHighlighter;
                  var control = this.map.getControl(
                    feature._prevHighlighter);
                  if (control) {
                    control.highlight(feature);
                    // THIS IS ADDED BY ME
                    this.events.triggerEvent("featureunhighlighted", {
                      feature: feature
                    });
                  }
                } else {
                  this.unhighlight(feature);
                }
              } else {
                // THIS IS ELSE BLOCK AND TRIGGER CALL ADDED BY ME
                this.events.triggerEvent("featureunhighlighted", {
                  feature: feature
                });
              }
            } else {
              this.unselect(feature);
            }
          }
        }
      });
      // allowing event to travel down
      this.controls.hoverCtrl.handlers["feature"].stopDown = false;
      this.map.addControl(this.controls.hoverCtrl);
      this.controls.hoverCtrl.activate();
    },

    updateItem: function(modelItem) {
      var style = this.toNativeStyle(modelItem.getStyle());
      var featureType = modelItem.getFeatureType();
      var layerName = featureType === "marker" ? "markers" : "shapes";
      var layer = this.layers[layerName];
      var feature = layer.getFeaturesByAttribute("id", modelItem.get("id"))[0];
      if (feature && !_.isEqual(feature.style, style)) {
        feature.style = style;
        feature.layer.drawFeature(feature, style);
      }
    },

    tileLayer: function(name) {
      var urlTemplate = this._getTileServiceURL(name);
      var attribution = this._getTileServiceAttribution(name);
      var options = _.extend({
        attribution: attribution,
        "transitionEffect": "resize"
      }, this._getTileServiceOptions(name));
      return new OpenLayers.Layer.XYZ(name, this._switchUrl(urlTemplate), _.extend({}, options));

    },

    registerViewportEvents: function() {
      var me = this;
      var eventMap = {
        "zoomend": "map:zoom",
        "movestart": "map:center"
      };
      _.each(eventMap, function(mapEvent, engineEvent) {
        me.map.events.register(engineEvent, me.map, function(e) {
          var wrappedEvent = wrapViewportEvent.call(me, e);
          me.trigger(mapEvent, wrappedEvent);
        });
      });

      function wrapViewportEvent(e) {
        var mapProj = this.map.getProjectionObject();
        var wsg84 = new OpenLayers.Projection("EPSG:4326");
        var transformPoint = function(centerPoint) {
          var center;
          if (centerPoint) {
            var p = centerPoint.clone().transform(mapProj, wsg84);
            center = {
              latitude: p.lat,
              longitude: p.lon
            };
          } else {
            center = {
              latitude: undefined,
              longitude: undefined
            };
          }
          return center;
        };

        var extentObj = e.object.getExtent();
        var viewport = {
          northEast: {},
          southWest: {}
        };
        if (extentObj) {
          var extentInLatLon = extentObj.transform(mapProj, wsg84);
          viewport = {
            northEast: {
              latitude: extentInLatLon.top,
              longitude: extentInLatLon.right
            },
            southWest: {
              latitude: extentInLatLon.bottom,
              longitude: extentInLatLon.left
            }
          };
        }
        var wrappedEvent = {
          zoomLevel: e.object.getZoom(),
          center: transformPoint(e.object.center),
          viewport: viewport,
          raw: e
        };
        return wrappedEvent;
      }
    }

  });

  function doClearSelection(me, feature) {
    if (me.model) {
      me.model.flatten().each(function(m) {
        m.setSelection(MapModel.SelectionStates.NONE);
      });
      me.trigger("engine:selection:complete");
    }
  }

  function addToSelection(modelItem) {
    modelItem.setSelection(MapModel.SelectionStates.ALL);
  }

  function doZoomIn(me) {
    me.zoomIn();
  }

  function doToggleSelection(me, feature) {
    this.clickFeature(feature);
    var modelItem = feature.attributes.model;
    toggleSelection(modelItem);
    var eventName = modelItem.getFeatureType() + ":click";
    me.trigger("engine:selection:complete");
    me.trigger(eventName, me.wrapEvent({feature: feature}));
  }

  function toggleSelection(modelItem) {
    var SelectionStates = MapModel.SelectionStates;
    var toggleTable = {};
    toggleTable[SelectionStates.ALL] = SelectionStates.NONE;
    toggleTable[SelectionStates.SOME] = SelectionStates.NONE;
    toggleTable[SelectionStates.NONE] = SelectionStates.ALL;
    var newState = toggleTable[modelItem.getSelection()];
    modelItem.setSelection(newState);
  }

});
