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
  "amd!cdf/lib/underscore",
  "../MapEngine",
  "./MapComponentAsyncLoader",
  "../../model/MapModel",
  "./MapOverlay",
  "css!./styleGoogle"
], function($, _, MapEngine, MapComponentAsyncLoader, MapModel, OurMapOverlay) {
  "use strict";


  return MapEngine.extend({
    map: undefined,
    centered: false,
    boxStyle: {
      fillOpacity: 0.15,
      strokeWeight: 0.9
    },
    overlays: [],
    selectedFeature: undefined,

    constructor: function(options) {
      this.base();
      this.options = options;
      this.controls = {}; // map controls
      this.controls.listenersHandle = {};
    },

    init: function() {
      return $.when(MapComponentAsyncLoader("3", this.options.API_KEY)).then(
        function(status) {
          OurMapOverlay.prototype = new google.maps.OverlayView();
          OurMapOverlay.prototype.onAdd = function() {
            // Note: an overlay"s receipt of onAdd() indicates that
            // the map"s panes are now available for attaching
            // the overlay to the map via the DOM.

            // Create the DIV and set some basic attributes.
            var div = document.createElement("DIV");
            div.id = "MapOverlay";
            div.style.position = "absolute";

            if (this.borderColor_) {
              div.style.border = "3px solid " + this.borderColor_;
            } else {
              div.style.border = "none";
            }

            var me = this;
            var closeDiv = $('<div id="MapOverlay_close" class="olPopupCloseBox" style="position: absolute;"></div>');
            closeDiv.click(function() {
              me.setMap(null);
            });
            $(div).append(closeDiv);

            if (this.popupContentDiv_ && this.popupContentDiv_.length > 0) {
              $(div).append($("#" + this.popupContentDiv_));
            } else {
              div.innerHTML = this.htmlContent_;
            }

            //Using implementation described on http://web.archive.org/web/20100522001851/http://code.google.com/apis/maps/documentation/javascript/overlays.html
            // Set the overlay"s div_ property to this DIV
            this.div_ = div;

            // We add an overlay to a map via one of the map"s panes.
            // We"ll add this overlay to the overlayImage pane.
            var panes = this.getPanes();
            panes.overlayLayer.appendChild(div);
          };

          //Using implementation described on http://web.archive.org/web/20100522001851/http://code.google.com/apis/maps/documentation/javascript/overlays.html
          OurMapOverlay.prototype.draw = function() {
            // Size and position the overlay. We use a southwest and northeast
            // position of the overlay to peg it to the correct position and size.
            // We need to retrieve the projection from this overlay to do this.
            var overlayProjection = this.getProjection();

            // Retrieve the southwest and northeast coordinates of this overlay
            // in latlngs and convert them to pixels coordinates.
            // We"ll use these coordinates to resize the DIV.
            var sp = overlayProjection.fromLatLngToDivPixel(this.startPoint_);

            // Resize the DIV to fit the indicated dimensions.
            var div = this.div_;
            div.style.left = sp.x + "px";
            div.style.top = (sp.y + 30) + "px";
            div.style.width = this.width_ + "px";
            div.style.height = this.height_ + "px";
          };

          OurMapOverlay.prototype.onRemove = function() {
            if (this.popupContentDiv_) {
              $("#" + this.popupContentDiv_).append($(this.div_));
              $(this.div_).detach();
            }
            this.div_.style.display = "none";
            this.div_.parentNode.removeChild(this.div_);
            this.div_ = null;
          };

        });
    },

    wrapEvent: function(event, featureType) {
      var me = this;
      var feature = event.feature;
      var modelItem = feature.getProperty("model");
      return $.extend(this._wrapEvent(modelItem), {
        latitude: event.latLng.lat(),
        longitude: event.latLng.lng(),
        _popup: function(html, options) {
          var opt = $.extend({
            width: 100,
            height: 100
          }, options || {});
          me.showPopup(null, feature, opt.height, opt.width, html, null, null);
        },
        feature: feature,
        mapEngineType: "google3",
        draw: function(style) {
          // this function is currently called by the shape callbacks
          var validStyle = me.toNativeStyle(style);
          feature.setOptions(validStyle);
          feature.setVisible(false);
          feature.setVisible(_.has(style, "visible") ? !!style.visible : true);
        },
        _setSelectedStyle: function(style) {
          feature._selStyle = style;
        },
        _getSelectedStyle: function() {
          return feature._selStyle;
        },
        raw: event
      });
    },

    toNativeStyle: function(foreignStyle, modelItem) {
      var conversionTable = {
        // SVG standard attributes : OpenLayers2 attributes
        "fill": "fillColor",
        "fill-opacity": "fillOpacity",
        "stroke": "strokeColor",
        "stroke-opacity": "strokeOpacity",
        "stroke-width": "strokeWeight",
        "r": "scale",
        "z-index": "zIndex",
        //Backwards compatibility
        "fillColor": "fillColor",
        "fillOpacity": "fillOpacity",
        "strokeColor": "strokeColor",
        "strokeOpacity": "strokeOpacity",
        "strokeWidth": "strokeWeight",
        "zIndex": "zIndex"
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
            case "icon-url":
              validStyle["icon"] = value;
              validStyle["size"] = new google.maps.Size(foreignStyle["width"], foreignStyle["height"]);
              break;
            case "symbol":
              var symbols = {
                circle: google.maps.SymbolPath.CIRCLE
              };
              var symbol = symbols[value];
              validStyle["path"] = _.isUndefined(symbol) ? value : symbol;

              break;
            default:
              // be permissive about the validation
              validStyle[key] = value;
              break;
          }
        }
      });

      if (modelItem && modelItem.getFeatureType() === "marker") {
        if (!validStyle.icon) {
          validStyle = {
            icon: validStyle
          };
        }
      }
      //console.log("foreign vs valid:", foreignStyle, validStyle);
      return validStyle;
    },

    /*----------------------------*/

    updateItem: function(modelItem) {
      var id = modelItem.get("id");
      var feature = this.map.data.getFeatureById(id);
      var style = this.toNativeStyle(modelItem.getStyle(), modelItem);
      this.map.data.overrideStyle(feature, style);
    },

    renderMap: function(target) {
      var mapOptions = {
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        draggingCursor: "inherit",
        draggableCursor: "inherit",
        scrollwheel: this.options.controls.enableZoomOnMouseWheel === true,
        keyboardShortcuts: this.options.controls.enableKeyboardNavigation === true,
        disableDefaultUI: true
      };

      // Add base map
      this.map = new google.maps.Map(target, mapOptions);
      this.$map = $(this.map.getDiv());

      // Add div for attribution
      this.$attribution = $('<div class="map-attribution" />');
      $(target).after(this.$attribution);

      this.addLayers();
      this.addControls();
      this.registerViewportEvents();
      this._registerDragCallbacks();
    },

    _registerDragCallbacks: function() {
      var me = this;
      google.maps.event.addListener(this.map, "dragstart", function() {
        me._updateDrag(true);
      });

      var extent = this.options.viewport.extent;
      var restrictedExtent = new google.maps.LatLngBounds(
        new google.maps.LatLng(extent.southEast.latitude, extent.southEast.longitude),
        new google.maps.LatLng(extent.northWest.latitude, extent.northWest.longitude)
      );
      google.maps.event.addListener(this.map, "dragend", function() {
        me._restrictPanning(restrictedExtent);
        me._updateDrag(false);
      });
    },

    _restrictPanning: function(restrictedExtent) {
      var c = this.map.getCenter(),
        x = c.lng(),
        y = c.lat();
      var b = this.map.getBounds();
      var h = 0.5 * (b.getNorthEast().lat() - b.getSouthWest().lat());
      var w = 0.5 * (b.getNorthEast().lng() - b.getSouthWest().lng());

      var maxX = restrictedExtent.getNorthEast().lng();
      var minX = restrictedExtent.getSouthWest().lng();
      var maxY = restrictedExtent.getNorthEast().lat();
      var minY = restrictedExtent.getSouthWest().lat();

      if (x - w < minX) {
        x = minX + w;
      }
      if (x + w > maxX) {
        x = maxX - w;
      }
      if (y - h < minY) {
        y = minY + h;
      }
      if (y + h > maxY) {
        y = maxY - h;
      }
      if (c.lng() !== x || c.lat() !== y) {
        this.map.setCenter(new google.maps.LatLng(y, x));
      }
    },

    zoomExtends: function() {
      var bounds = new google.maps.LatLngBounds();
      this.map.data.forEach(function(feature) {
        if (feature.getGeometry().getType() == "Point") {
          bounds.extend(feature.getGeometry().get());
        }
      });

      if (!bounds.isEmpty()) {
        this.map.setCenter(bounds.getCenter());
        this.map.fitBounds(bounds);
        return true;
      } else {
        return false;
      }
    },

    renderItem: function(modelItem) {
      if (!modelItem) {
        return;
      }
      var geoJSON = modelItem.get("geoJSON");
      var me = this;
      $.when(geoJSON).then(function(feature) {
        if (!feature) {
          return;
        }

        //set id for the feature
        $.extend(true, feature, {
          properties: {
            id: modelItem.get("id"),
            model: modelItem
          }
        });

        var importedFeatures = me.map.data.addGeoJson(feature, {
          idPropertyName: "id"
        });
        _.each(importedFeatures, function(f) {
          var style = me.toNativeStyle(modelItem.getStyle(), modelItem);
          me.map.data.overrideStyle(f, style);
        });
      });

    },

    addControls: function() {

      this._addControlHover();
      //this._addControlClick();
      this._addControlZoomBox();
      this._addControlBoxSelector();
      this._addLimitZoomLimits();
    },

    _removeListeners: function() {
      _.each(this.controls.listenersHandle, function(h) {
        h.remove();
      });
    },

    _addControlHover: function() {
      var me = this;
      this.map.data.addListener("mouseover", function(e) {
        setStyle(e, "hover");
        var featureType = e.feature.getProperty("model").getFeatureType();
        me.trigger(featureType + ":mouseover", me.wrapEvent(e));
      });

      this.map.data.addListener("mouseout", function(e) {
        setStyle(e, "normal");
        var featureType = e.feature.getProperty("model").getFeatureType();
        me.trigger(featureType + ":mouseout", me.wrapEvent(e));
      });

      function setStyle(event, action) {
        var modelItem = event.feature.getProperty("model");
        modelItem.setHover(action === "hover");
      }

    },

    _addControlZoomBox: function() {
      this.controls.zoomBox = {
        bounds: null,
        gribBoundingBox: null,
        mouseIsDown: false
      };
    },

    _addControlBoxSelector: function() {
      this.controls.boxSelector = {
        bounds: null,
        gribBoundingBox: null,
        mouseIsDown: false
      };
    },

    _addControlClick: function() {
      var me = this;
      this.map.data.addListener("click", function(e) {
        var featureType = e.feature.getProperty("model").getFeatureType();
        me.trigger(featureType + ":click", me.wrapEvent(e));
        me.trigger("engine:selection:complete");
      });
    },

    _addLimitZoomLimits: function() {
      var minZoom = _.isFinite(this.options.viewport.zoomLevel.min) ? this.options.viewport.zoomLevel.min : 0;
      var maxZoom = _.isFinite(this.options.viewport.zoomLevel.max) ? this.options.viewport.zoomLevel.max : null;
      var me = this;

      // Limit the zoom level
      google.maps.event.addListener(this.map, "zoom_changed", function() {
        if (me.map.getZoom() < minZoom) {
          me.map.setZoom(minZoom);
        } else {
          if ((!_.isNull(maxZoom)) && (me.map.getZoom() > maxZoom)) {
            me.map.setZoom(maxZoom); // if is NULL, max is the limit of the map
          }
        }
      });
    },

    zoomIn: function() {
      this.map.setZoom(this.map.getZoom() + 1);
    },

    zoomOut: function() {
      this.map.setZoom(this.map.getZoom() - 1);
    },

    setPanningMode: function() {
      this._removeListeners();
      this._updateMode("pan");
      this._updateDrag(false);
      this.map.setOptions({
        draggingCursor: "inherit",
        draggableCursor: "inherit",
        draggable: true
      });

      return;
      var listeners = this.controls.listenersHandle;
      listeners.clickOnData = this.map.data.addListener("click",
        this._createClickHandler(function(me, event) {
          //console.log('Click on feature!');
          var modelItem = event.feature.getProperty("model");
          toggleSelection(modelItem);
          me.trigger("engine:selection:complete");
          var featureType = modelItem.getFeatureType();
          me.trigger(featureType + ":click", me.wrapEvent(event));
        })
      );

      listeners.clickOnMap = google.maps.event.addListener(this.map, "click",
        this._createClickHandler(function(me, event) {
          clearSelection(me.model);
          me.trigger("engine:selection:complete");
        })
      );

    },

    setZoomBoxMode: function() {
      this._removeListeners();
      this._updateMode("zoombox");
      this._updateDrag(false);
      var me = this;
      var control = this.controls.zoomBox;
      var listeners = this.controls.listenersHandle;
      this.map.setOptions({
        draggingCursor: "inherit",
        draggableCursor: "inherit",
        draggable: false
      });

      listeners.drag = google.maps.event.addDomListener(this.map.getDiv(), "mousemove", function(e) {
        control.isDragging = (e.buttons === 1);
      });

      var onMouseMove = function(e) {
        if (me.model.isZoomBoxMode() && control.isDragging) {
          if (control.mouseIsDown) {
            me._onBoxResize(control, e);
          } else {
            me._beginBox(control, e);
          }
        }
      };
      listeners.mousemove = google.maps.event.addListener(this.map, "mousemove", onMouseMove);
      listeners.mousemoveFeature = this.map.data.addListener("mousemove", onMouseMove);

      var onMouseUp = this._endBox(control,
        function() {
          return me.model.isZoomBoxMode();
        },
        function(bounds) {
          me.map.fitBounds(bounds);
        }
      );
      listeners.mouseup = google.maps.event.addListener(this.map, "mouseup", onMouseUp);
      listeners.mouseupFeature = this.map.data.addListener("mouseup", onMouseUp);

    },

    setSelectionMode: function() {
      this._removeListeners();
      this._updateMode("selection");
      this._updateDrag(false);
      var me = this;
      var control = me.controls.boxSelector;
      var listeners = this.controls.listenersHandle;
      this.map.setOptions({
        draggingCursor: "inherit",
        draggableCursor: "inherit",
        draggable: false
      });

      listeners.drag = google.maps.event.addDomListener(this.map.getDiv(), "mousemove", function(e) {
        control.isDragging = (e.buttons === 1);
      });

      var onMouseMove = function(e) {
        if (me.model.isSelectionMode() && control.isDragging) {
          if (control.mouseIsDown) {
            me._onBoxResize(control, e);
          } else {
            me._beginBox(control, e);
          }
        }
      };

      listeners.mousemove = google.maps.event.addListener(this.map, "mousemove", onMouseMove);
      listeners.mousemoveFeature = this.map.data.addListener("mousemove", onMouseMove);

      var isSelectionMode = function() {
        return me.model.isSelectionMode();
      };

      var addFeaturesInBoundsToSelection = function(bounds) {
        //console.log('Mouse up!');
        me.model.leafs()
          .each(function(m) {
            var id = m.get("id");
            if (me.map.data.getFeatureById(id) != undefined) {
              $.when(m.get("geoJSON")).then(function(obj) {
                // $.when is executed immediately, as the feature is already resolved
                // Area contains shape
                if (isInBounds(obj.geometry, bounds)) {
                  addToSelection(m);
                }
              });
            }
          });
        me.trigger("engine:selection:complete");
      };

      var onMouseUpOverMap = this._endBox(control,
        isSelectionMode,
        addFeaturesInBoundsToSelection,
        this._createClickHandler(function(me, event) {
          clearSelection(me.model);
          //console.log('Click on map!');
          me.trigger("engine:selection:complete");
        })
      );

      var onMouseUpOverFeature = this._endBox(control,
        isSelectionMode,
        addFeaturesInBoundsToSelection,
        this._createClickHandler(function(me, event) {
          var modelItem = event.feature.getProperty("model");
          toggleSelection(modelItem);
          me.trigger("engine:selection:complete");
          var featureType = modelItem.getFeatureType();
          me.trigger(featureType + ":click", me.wrapEvent(event));
        }, null)
      );

      listeners.mouseup = google.maps.event.addListener(this.map, "mouseup", onMouseUpOverMap);
      listeners.mouseupFeature = this.map.data.addListener("mouseup", onMouseUpOverFeature);
    },


    _beginBox: function(control, e) {
      control.mouseIsDown = true;
      control.mouseDownPos = e.latLng;
      this._updateDrag(true);
    },

    _endBox: function(control, condition, dragEndCallback, clickCallback) {
      var me = this;
      return function(e) {
        if (condition()) {
          if (control.mouseIsDown && control.gribBoundingBox) {
            control.mouseIsDown = false;
            control.mouseUpPos = e.latLng;
            var bounds = control.gribBoundingBox.getBounds();

            dragEndCallback(bounds);

            control.gribBoundingBox.setMap(null);
            control.gribBoundingBox = null;
            me._updateDrag(false);
          } else {
            if (_.isFunction(clickCallback)) {
              clickCallback(e);
            }
          }
        }
      };
    },

    _onBoxResize: function(control, e) {
      if (control.gribBoundingBox !== null) { // box exists
        var bounds = new google.maps.LatLngBounds(control.mouseDownPos, null);
        bounds.extend(e.latLng);
        control.gribBoundingBox.setBounds(bounds); // If this statement is enabled, I lose mouseUp events
      } else { // create bounding box
        control.gribBoundingBox = new google.maps.Rectangle($.extend({
          map: this.map,
          clickable: false
        }, this.boxStyle));
      }
    },

    addLayers: function() {
      //Prepare tilesets as overlays
      var layers = [],
        layerIds = [],
        layerOptions = [];
      for (var k = 0; k < this.options.tiles.tilesets.length; k++) {
        var tilesetId = this.options.tiles.tilesets[k].slice(0);
        layerIds.push(tilesetId);
        var tileset = tilesetId.slice(0).split("-")[0];
        var variant = tilesetId.slice(0).split("-").slice(1).join("-") || "default";
        switch (tileset) {
          case "google":
            var mapOpts = {
              "default": {
                mapTypeId: google.maps.MapTypeId.ROADMAP
              },
              "roadmap": {
                mapTypeId: google.maps.MapTypeId.ROADMAP
              },
              "terrain": {
                mapTypeId: google.maps.MapTypeId.TERRAIN
              },
              "satellite": {
                mapTypeId: google.maps.MapTypeId.SATELLITE
              },
              "hybrid": {
                mapTypeId: google.maps.MapTypeId.HYBRID
              }
            };

            layerOptions.push(mapOpts[variant] || mapOpts.default);
            layers.push("");
            break;

          default:
            layerOptions.push({
              mapTypeId: tilesetId
            });

            if (this.options.tiles.services[tilesetId]) {
              layers.push(this.tileLayer(tilesetId));
              var attribution = this._getTileServiceAttribution(tilesetId);
              if (!_.isEmpty(attribution)) {
                this.$attribution.append($("<div>" + attribution + "</div>"));
              }
            } else {
              layers.push("");
            }
        }

      } //for tilesets

      for (k = 0; k < layers.length; k++) {
        if (!_.isEmpty(layers[k])) {
          this.map.mapTypes.set(layerIds[k], layers[k]);
          //this.map.overlayMapTypes.push(layers[k]);
          this.map.setMapTypeId(layerIds[k]);
          this.map.setOptions(layerOptions[k]);
        }
      }

    },

    updateViewport: function(centerLongitude, centerLatitude, zoomLevel) {
      if (!zoomLevel) {
        zoomLevel = this.options.viewport.zoomLevel["default"];
      }
      this.map.setZoom(zoomLevel);
      if (!this.zoomExtends()) {
        this.map.panTo(new google.maps.LatLng(38, -9));
      }
    },

    tileLayer: function(name) {
      var options = _.extend({
        tileSize: new google.maps.Size(256, 256),
        minZoom: 1,
        maxZoom: 19
      }, this.options.tiles.services[name].options || {});
      var urlList = this._switchUrl(this._getTileServiceURL(name));
      var myself = this;

      return new google.maps.ImageMapType(_.defaults({
        name: name.indexOf("/") >= 0 ? "custom" : name,
        getTileUrl: function(coord, zoom) {
          var limit = Math.pow(2, zoom);
          if (coord.y < 0 || coord.y >= limit) {
            return "404.png";
          } else {
            // use myself._selectUrl
            coord.x = ((coord.x % limit) + limit) % limit;
            var url;
            if (_.isArray(urlList)) {
              url = myself._selectUrl(
                _.template(
                  "${z}/${x}/${y}",
                  {interpolate: /\$\{(.+?)\}/g}
                )({x: coord.x, y: coord.y, z: zoom}),
                urlList
              );
            } else {
              url = urlList;
            }
            return _.template(
              url,
              {interpolate: /\$\{(.+?)\}/g}
            )({x: coord.x,y: coord.y, z: zoom});
          }
        }
      }, options));
    },

    showPopup0: function(data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      if (popupContentDiv && popupContentDiv.length > 0) {
        contents = $("#" + popupContentDiv).html();
      }

      var popup = new OurMapOverlay(feature.getGeometry().get(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);
      this._popups = this._popups || [];
      _.each(this._popups, function(p) {
        p.setMap(null);
      });
      this._popups.push(popup);
    },

    showPopup: function(data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      var popup = new google.maps.InfoWindow({
        content: contents,
        position: feature.getGeometry().get(),
        maxWidth: popupWidth
      });
      this._popups = this._popups || [];
      _.each(this._popups, function(p) {
        p.close();
      });
      popup.open(this.map);
      this._popups.push(popup);
    },

    registerViewportEvents: function() {
      var me = this;
      var eventMap = {
        "zoom_changed": "map:zoom",
        "center_changed": "map:center"
      };
      _.each(eventMap, function(mapEvent, engineEvent) {
        google.maps.event.addListener(me.map, engineEvent, function() {
          var wrappedEvent = wrapViewportEvent.call(me);
          me.trigger(mapEvent, wrappedEvent);
        });
      });

      function wrapViewportEvent() {
        var viewport = getViewport(this.map.getBounds());
        var wrappedEvent = {
          zoomLevel: this.map.getZoom(),
          center: transformPoint(this.map.getCenter() || new google.maps.LatLng()),
          viewport: viewport,
          raw: this.map
        };
        return wrappedEvent;

        function transformPoint(centerPoint) {
          var center = {
            latitude: centerPoint.lat(),
            longitude: centerPoint.lng()
          };
          return center;
        }

        function getViewport(bounds) {
          if (bounds) {
            viewport = {
              northEast: transformPoint(bounds.getNorthEast()),
              southWest: transformPoint(bounds.getSouthWest())
            };
          } else {
            viewport = {
              northEast: {},
              southWest: {}
            };
          }
        }
      }
    }

  });

  function clearSelection(modelItem) {
    modelItem.root().setSelection(MapModel.SelectionStates.NONE);
  }

  function addToSelection(modelItem) {
    modelItem.setSelection(MapModel.SelectionStates.ALL);
  }

  function toggleSelection(modelItem) {
    modelItem.setSelection(
      (modelItem.getSelection() === MapModel.SelectionStates.ALL) ?
        MapModel.SelectionStates.NONE
        : MapModel.SelectionStates.ALL
    );
  }

  function isInBounds(geometry, bounds) {
    if (!bounds) {
      return false;
    }
    switch (geometry.type) {
      case "MultiPolygon":
        return containsMultiPolygon(bounds, geometry.coordinates);
      case "Polygon":
        return containsPolygon(bounds, geometry.coordinates);
      case "Point":
        return containsPoint(bounds, geometry.coordinates);
      default:
        return false;
    }

    function containsMultiPolygon(bounds, multiPolygon) {
      var hasPolygon = function(polygon) {
        return containsPolygon(bounds, polygon);
      };
      return _.some(multiPolygon, hasPolygon);
    }

    function containsPolygon(bounds, polygon) {
      var hasPoint = function(point) {
        return containsPoint(bounds, point);
      };
      return _.some(polygon, function(line) {
        return _.some(line, hasPoint);
      });
    }

    function containsPoint(bounds, point) {
      var latLng = new google.maps.LatLng(point[1], point[0]);
      return bounds.contains(latLng);
    }
  }

});
