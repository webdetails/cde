/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
  "css!./styleGoogle"
], function ($, _, MapEngine, MapComponentAsyncLoader, MapModel) {
  var SelectionStates = MapModel.SelectionStates;

  function OurMapOverlay(startPoint, width, height, htmlContent, popupContentDiv, map, borderColor) {

    // Now initialize all properties.
    this.startPoint_ = startPoint;
    this.width_ = width;
    this.height_ = height;
    this.map_ = map;
    this.htmlContent_ = htmlContent;
    this.popupContentDiv_ = popupContentDiv;
    this.borderColor_ = borderColor;

    this.div_ = null;

    // Explicitly call setMap() on this overlay
    this.setMap(map);
  }


  var GoogleMapEngine = MapEngine.extend({
    map: undefined,
    centered: false,
    overlays: [],
    API_KEY: false,
    selectedFeature: undefined,

    constructor: function (options) {
      this.base();
      $.extend(this, options);
      this.controls = {}; // map controls
      this.controls.listenersHandle = {};

    },

    init: function () {
      return $.when(MapComponentAsyncLoader("3", this.API_KEY)).then(
        function (status) {
          OurMapOverlay.prototype = new google.maps.OverlayView();
          OurMapOverlay.prototype.onAdd = function () {
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
            closeDiv.click(function () {
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
          OurMapOverlay.prototype.draw = function () {
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


          OurMapOverlay.prototype.onRemove = function () {
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

    wrapEvent: function (event, featureType) {
      var me = this;
      var modelItem = event.feature.getProperty('model');
      return $.extend(this._wrapEvent(modelItem), {
        latitude: event.latLng.lat(),
        longitude: event.latLng.lng(),
        _popup: function (html, options) {
          var opt = $.extend({
            width: 100,
            height: 100
          }, options || {});
          me.showPopup(null, feature, opt.height, opt.width, html, null, null);
        },
        feature: event.feature,
        mapEngineType: "google3",
        draw: function (style) {
          // this function is currently called by the shape callbacks
          var validStyle = me.toNativeStyle(style);
          feature.setOptions(validStyle);
          feature.setVisible(false);
          feature.setVisible(_.has(style, "visible") ? !!style.visible : true);
        },
        setSelectedStyle: function (style) {
          feature.selStyle = style;
        },
        getSelectedStyle: function () {
          return feature.selStyle;
        },
        isSelected: function () {
          return me.selectedFeature && me.selectedFeature[0] === data.key;
        },
        raw: event
      });
    },


    toNativeStyle: function (foreignStyle, modelItem) {
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
      _.each(foreignStyle, function (value, key) {
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
              break
          }
        }
      });

      if (modelItem && modelItem.getFeatureType() === 'marker') {
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

    updateItem: function (modelItem) {
      var id = modelItem.get("id");
      var feature = this.map.data.getFeatureById(id);
      var style = this.toNativeStyle(modelItem.getStyle(), modelItem);
      this.map.data.overrideStyle(feature, style);
    },

    renderMap: function (target) {
      var mapOptions = {
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: true
      };

      // Add base map
      this.map = new google.maps.Map(target, mapOptions);

      this.addLayers();
      this.addControls();
      this.registerViewportEvents();

    },

    zoomExtends: function () {
      var latlngbounds = new google.maps.LatLngBounds();
      this.map.data.forEach(function (feature) {
        if (feature.getGeometry().getType() == 'Point') {
          latlngbounds.extend(feature.getGeometry().get());
        }
      });

      if (!latlngbounds.isEmpty()) {
        this.map.setCenter(latlngbounds.getCenter());
        this.map.fitBounds(latlngbounds);
        return true;
      } else {
        return false;
      }
    },

    renderItem: function (modelItem) {
      if (!modelItem) {
        return;
      }
      var geoJSON = modelItem.get('geoJSON');
      var me = this;
      $.when(geoJSON).then(function (feature) {
        if (!feature) {
          return;
        }

        //set id for the feature
        $.extend(true, feature, {
          properties: {
            id: modelItem.get('id'),
            model: modelItem
          }
        });

        var importedFeatures = me.map.data.addGeoJson(feature, {
          idPropertyName: 'id'
        });
        _.each(importedFeatures, function (f) {
          var style = me.toNativeStyle(modelItem.getStyle(), modelItem);
          me.map.data.overrideStyle(f, style);
        });
      });

    },

    addControls: function () {

      this._addControlHover();
      //this._addControlClick();
      this._addControlZoomBox();
      this._addControlBoxSelector();
      this._addLimitZoomLimits();

    },

    _removelistenersHandle: function () {
      _.each(this.controls.listenersHandle, function (h) {
        h.remove();
      });
    },

    _addControlHover: function () {
      this.map.data.addListener('mouseover', function (event) {
        setStyle(event, 'hover');
      });

      this.map.data.addListener('mouseout', function (event) {
        setStyle(event, 'normal');
      });

      function setStyle(event, action) {
        var modelItem = event.feature.getProperty('model');
        modelItem.setHover(action === 'hover');
      }

    },

    _addControlZoomBox: function () {
      this.controls.zoomBox = {
        bounds: null,
        gribBoundingBox: null,
        mouseIsDown: false
      };
    },

    _addControlBoxSelector: function () {
      this.controls.boxSelector = {
        bounds: null,
        gribBoundingBox: null,
        mouseIsDown: false
      };
    },

    _addControlClick: function () {
      var me = this;
      this.map.data.addListener('click', function (e) {
        var featureType = event.feature.getProperty('model').getFeatureType();
        me.trigger(featureType + ':click', me.wrapEvent(e));
        me.trigger('engine:selection:complete');
      });
    },

    _addLimitZoomLimits: function () {

      var me = this;
      var minZoom = _.isFinite(me.options.viewport.zoomLevel.min) ? me.options.viewport.zoomLevel.min : 0;
      var maxZoom = _.isFinite(me.options.viewport.zoomLevel.max) ? me.options.viewport.zoomLevel.max : null;

      // Limit the zoom level
      google.maps.event.addListener(me.map, 'zoom_changed', function () {
        if (me.map.getZoom() < minZoom) {
          me.map.setZoom(minZoom);
        } else if ((!_.isNull(maxZoom)) && (me.map.getZoom() > maxZoom)) {
          me.map.setZoom(maxZoom); // if is NULL, max is the limit of the map
        }
      });
    },

    zoomIn: function () {
      //console.log('zoomIn');
      this.map.setZoom(this.map.getZoom() + 1);
    },

    zoomOut: function () {
      //console.log('zoomOut');
      this.map.setZoom(this.map.getZoom() - 1);
    },

    setPanningMode: function () {
      this._removelistenersHandle();
      var me = this;
      me.controls.listenersHandle.click = this._toggleOnClick();
    },

    setZoomBoxMode: function () {
      this._removelistenersHandle();
      var me = this;
      me.controls.listenersHandle.click = this._toggleOnClick();
      me.controls.listenersHandle.mousedown = google.maps.event.addListener(this.map, 'mousedown', function (e) {
        if (me.model.isZoomBoxMode()) {
          me._beginBox(me.controls.zoomBox, e);
        }
      });

      me.controls.listenersHandle.mousemove = google.maps.event.addListener(this.map, 'mousemove', function (e) {
        var control = me.controls.zoomBox;
        if (me.model.isZoomBoxMode() && control.mouseIsDown) {
          me._onBoxResize(control, e);
        }
      });

      me.controls.listenersHandle.mouseup = google.maps.event.addListener(this.map, 'mouseup', function (e) {
        if (me.model.isZoomBoxMode() && me.controls.zoomBox.mouseIsDown) {
          me.controls.zoomBox.mouseIsDown = false;
          me.controls.zoomBox.mouseUpPos = e.latLng;

          var bounds = me.controls.zoomBox.gribBoundingBox.getBounds();
          var boundsSelectionArea = new google.maps.LatLngBounds(
            bounds.getSouthWest(),
            bounds.getNorthEast()
          );

          me.map.fitBounds(boundsSelectionArea);
          me.controls.zoomBox.gribBoundingBox.setMap(null);
          me.controls.zoomBox.gribBoundingBox = null;
          me.map.setOptions({
            draggable: true
          });
        }
      });


    },


    setSelectionMode: function () {
      this._removelistenersHandle();
      var me = this;
      var control = me.controls.boxSelector;

      this.controls.listenersHandle.click = this._toggleOnClick();

      this.controls.listenersHandle.mousedown = google.maps.event.addListener(this.map, 'mousedown', function (e) {
        if (me.model.isSelectionMode()) {
          me._beginBox(me.controls.boxSelector, e);
        }
      });

      me.controls.listenersHandle.mousemove = google.maps.event.addListener(this.map, 'mousemove', function (e) {
        var control = me.controls.boxSelector;
        if (me.model.isSelectionMode() && control.mouseIsDown) {
          me._onBoxResize(control, e);
        }
      });

      me.controls.listenersHandle.mouseup = google.maps.event.addListener(this.map, 'mouseup', function (e) {
        if (me.model.isSelectionMode() && control.mouseIsDown) {
          control.mouseIsDown = false;
          control.mouseUpPos = e.latLng;

          me.model.leafs()
            .each(function (m) {
              var id = m.get('id');
              if (me.map.data.getFeatureById(id) != undefined) {
                // Replace with $.when(m.geoJSON).then()
                me.map.data.getFeatureById(id).toGeoJson(function (obj) {
                  var geometry = obj.geometry;
                  var isWithinArea = false;
                  var bounds = control.gribBoundingBox.getBounds();
                  // For shape
                  if (geometry.type == 'MultiPolygon') {
                    isWithinArea = _.some(geometry.coordinates, function (value) {
                      return _.some(value, function (value) {
                        return _.some(value, function (value) {
                          var latLng = new google.maps.LatLng(value[1], value[0]);
                          return bounds.contains(latLng);
                        });
                      });
                    });
                  } else if (geometry.type == 'Point') {
                    var latLng = new google.maps.LatLng(geometry.coordinates[1], geometry.coordinates[0]);
                    isWithinArea = bounds.contains(latLng);
                  }

                  // Area contains shape
                  if (isWithinArea) {
                    toggleSelection(me, m);
                  }

                });

              }

            });

          //me.map.fitBounds( boundsSelectionArea );
          me.trigger('engine:selection:complete');
          control.gribBoundingBox.setMap(null);
          control.gribBoundingBox = null;

          me.map.setOptions({
            draggable: true
          });

        }
      });


      //console.log('Selection mode enable');
    },


    /*-----------------------------*/
    _toggleOnClick: function () {
      var me = this;
      return this.map.data.addListener('click', function (event) {
        var modelItem = event.feature.getProperty('model');
        toggleSelection(me, modelItem);
        me.trigger('engine:selection:complete');
        var featureType = modelItem.getFeatureType();
        me.trigger(featureType + ':click', me.wrapEvent(event));
      });
    },

    _beginBox: function (control, e) {
      control.mouseIsDown = true;
      control.mouseDownPos = e.latLng;
      this.map.setOptions({
        draggable: false
      });
    },

    _onBoxResize: function (control, e) {
      if (control.gribBoundingBox !== null) { // box exists
        var newbounds = new google.maps.LatLngBounds(control.mouseDownPos, null);
        newbounds.extend(e.latLng);
        control.gribBoundingBox.setBounds(newbounds); // If this statement is enabled, I lose mouseUp events
      } else { // create bounding box
        control.gribBoundingBox = new google.maps.Rectangle({
          map: this.map,
          fillOpacity: 0.15,
          strokeWeight: 0.9,
          clickable: false
        });
      }
    },


    unselectPrevShape: function (key, shapes, shapeStyle) {
      var myself = this;
      var prevSelected = this.selectedFeature;
      if (prevSelected && prevSelected[0] !== key) {
        var prevShapes = prevSelected[1];
        var prevStyle = prevSelected[2];
        _.each(prevShapes, function (s) {
          var validStyle = myself.toNativeStyle(prevStyle);
          s.setOptions(validStyle);
          s.setVisible(false);
          s.setVisible(_.has(prevStyle, 'visible') ? !!prevStyle.visible : true);
        });
      }
      this.selectedFeature = [key, shapes, shapeStyle];
    },

    addLayers: function () {
      //Prepare tilesets as overlays
      var layers = [],
        layerIds = [],
        layerOptions = [];
      for (var k = 0; k < this.tilesets.length; k++) {
        var thisTileset = this.tilesets[k].slice(0);
        layerIds.push(thisTileset);
        layerOptions.push({
          mapTypeId: thisTileset
        });

        if (this.tileServices[thisTileset]) {
          layers.push(this.tileLayer(thisTileset));
        } else {
          layers.push('');
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

    updateViewport: function (centerLongitude, centerLatitude, zoomLevel) {
      if (!zoomLevel) zoomLevel = 2;
      this.map.setZoom(zoomLevel);

      var centerPoint;

      if (!this.zoomExtends())
        this.map.panTo(new google.maps.LatLng(38, -9));
    },

    tileLayer: function (name) {
      var options = _.extend({
        tileSize: new google.maps.Size(256, 256),
        minZoom: 1,
        maxZoom: 19
      }, this.tileServicesOptions[name] || {});
      var urlList = this._switchUrl(this._getTileServiceURL(name));
      var myself = this;

      return new google.maps.ImageMapType(_.defaults({
        name: name.indexOf('/') >= 0 ? 'custom' : name,
        getTileUrl: function (coord, zoom) {
          var limit = Math.pow(2, zoom);
          if (coord.y < 0 || coord.y >= limit) {
            return '404.png';
          } else {
            // use myself._selectUrl
            coord.x = ((coord.x % limit) + limit) % limit;
            var url;
            if (_.isArray(urlList)) {
              var s = _.template('${z}/${x}/${y}', {
                x: coord.x,
                y: coord.y,
                z: zoom
              }, {
                interpolate: /\$\{(.+?)\}/g
              });
              url = myself._selectUrl(s, urlList);
            } else {
              url = urlList;
            }
            return _.template(url, {
              x: coord.x,
              y: coord.y,
              z: zoom
            }, {
              interpolate: /\$\{(.+?)\}/g
            });
          }
        }
      }, options));
    },

    showPopup0: function (data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      if (popupContentDiv && popupContentDiv.length > 0) {
        contents = $('#' + popupContentDiv).html();
      }

      var popup = new OurMapOverlay(feature.getGeometry().get(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);
      this._popups = this._popups || [];
      _.each(this._popups, function (p) {
        p.setMap(null);
      });
      this._popups.push(popup);
    },

    showPopup: function (data, feature, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      var popup = new google.maps.InfoWindow({
        content: contents,
        position: feature.getGeometry().get(),
        maxWidth: popupWidth
      });
      this._popups = this._popups || [];
      _.each(this._popups, function (p) {
        p.close();
      });
      popup.open(this.map);
      this._popups.push(popup);
    },

    registerViewportEvents: function () {
      var me = this;
      var eventMap = {
        'zoom_changed': 'map:zoom',
        'center_changed': 'map:center'
      };
      _.each(eventMap, function (mapEvent, engineEvent) {
        google.maps.event.addListener(me.map, engineEvent, function () {
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

  return GoogleMapEngine;

  function toggleSelection(me, modelItem) {
    modelItem.setSelection((modelItem.getSelection() === SelectionStates.ALL) ? SelectionStates.NONE : SelectionStates.ALL);
    //me.updateItem(modelItem);
  }

});
