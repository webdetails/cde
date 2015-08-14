var GoogleMapEngine = (function () {

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
    init: function (mapComponent, tilesets) {
      this.tilesets = tilesets;
      this.mapComponent = mapComponent;

      $.when(loadGoogleMaps('3', this.API_KEY)).then(
        function (status) {
          OurMapOverlay.prototype = new google.maps.OverlayView();
          OurMapOverlay.prototype.onAdd = function () {
            // Note: an overlay's receipt of onAdd() indicates that
            // the map's panes are now available for attaching
            // the overlay to the map via the DOM.

            // Create the DIV and set some basic attributes.
            var div = document.createElement('DIV');
            div.id = 'MapOverlay';
            div.style.position = "absolute";

            if (this.borderColor_) {
              div.style.border = '3px solid ' + this.borderColor_;
            } else {
              div.style.border = "none";
            }


            /*      var myself = this;
             var closeDiv = $("<div id=\"MapOverlay_close\" class=\"olPopupCloseBox\" style=\"position: absolute;\"></div>");
             closeDiv.click(function () {
             myself.setMap(null);
             });

             $(div).append(closeDiv);
             */
            if (this.popupContentDiv_ && this.popupContentDiv_.length > 0) {
              $(div).append($('#' + this.popupContentDiv_));
            } else
              div.innerHTML = this.htmlContent_;


            //Using implementation described on http://web.archive.org/web/20100522001851/http://code.google.com/apis/maps/documentation/javascript/overlays.html
            // Set the overlay's div_ property to this DIV
            this.div_ = div;

            // We add an overlay to a map via one of the map's panes.
            // We'll add this overlay to the overlayImage pane.
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
            // We'll use these coordinates to resize the DIV.
            var sp = overlayProjection.fromLatLngToDivPixel(this.startPoint_);

            // Resize the DIV to fit the indicated dimensions.
            var div = this.div_;
            div.style.left = sp.x + 'px';
            div.style.top = (sp.y + 30) + 'px';
            div.style.width = this.width_ + 'px';
            div.style.height = this.height_ + 'px';
          };


          OurMapOverlay.prototype.onRemove = function () {
            if (this.popupContentDiv_) {
              // $('#' + this.popupContentDiv_).append($(this.div_));
              // $(this.div_).detach();
            }
            this.div_.style.display = 'none';
            // this.div_.parentNode.removeChild(this.div_);
            // this.div_ = null;
          };

          mapComponent.initCallBack();

        });
    },

    toNativeStyle: function (foreignStyle) {
      var validStyle = {};
      _.each(foreignStyle, function (value, key) {
        switch (key) {
          case 'strokeWidth':
            validStyle['strokeWeight'] = value;
            break;
          case 'zIndex':
          case 'visible':
          case 'fillColor':
          case 'fillOpacity':
          case 'strokeColor':
          case 'strokeOpacity':
            validStyle[key] = value;
        }
      });
      return validStyle;
    },

    wrapEvent: function (event, feature, featureType, featureStyle, data) {
      var myself = this;
      return {
        latitude: event.latLng.lat(),
        longitude: event.latLng.lng(),
        data: data,
        feature: feature,
        featureType: featureType,
        style: _.clone(featureStyle),
        marker: feature.marker,
        mapEngineType: 'google3',
        draw: function (style) {
          // this function is currently called by the shape callbacks
          var validStyle = myself.toNativeStyle(style);
          feature.setOptions(validStyle);
          feature.setVisible(false);
          feature.setVisible(_.has(style, 'visible') ? !!style.visible : true);
        },
        setSelectedStyle: function (style) {
          feature.selStyle = style;
        },
        getSelectedStyle: function () {
          return feature.selStyle;
        },
        isSelected: function () {
          return myself.selectedFeature && myself.selectedFeature[0] === data.key;
        },
        raw: event
      };
    },


    setShape1: function (multiPolygon, shapeStyle, data) {
      var shapes = this.map.data.addGeoJson(multiPolygon);
      return;
    },


    setShape: function (feature, shapeStyle, data) {
      if (!feature) {
        return;
      }
      var myself = this;

      var multiPolygon;
      switch (feature.geometry.type) {
        case 'MultiPolygon':
          multiPolygon = feature.geometry.coordinates;
          break;
        case 'Polygon':
          multiPolygon = [feature.geometry.coordinates];
          break;
        case 'LineString':
          multiPolygon = [[feature.geometry.coordinates]];
          break;
        default:
          return;
      }

      // It seems that Google Maps does not support multipolygons, so we have to register each polygon instead.
      var feature = _.map(multiPolygon, function (polygon) {
        var polygonGM = _.map(polygon, function (ring) {
          return _.map(ring, function (lonlat) {
            return new google.maps.LatLng(lonlat[1], lonlat[0]);
          });
        });

        var shape = new google.maps.Polygon(_.extend({
          paths: polygonGM
        }, myself.toNativeStyle(shapeStyle)));
        shape.setMap(myself.map);
        return shape;
      });


      // We'll have to use a trick to emulate the callbacks on multipolygons...
      _.each(feature, function (featurePolygon) {
        // We'll have to use a trick to emulate the multipolygons...
        google.maps.event.addListener(featurePolygon, 'click', function (event) {
          myself.unselectPrevShape(data.key, feature, shapeStyle);
          addEventToFeature('shape:click', event, feature, shapeStyle, data);
        });
        google.maps.event.addListener(featurePolygon, 'mousemove', function (event) {
          addEventToFeature('shape:mouseover', event, feature, shapeStyle, data);
        });
        google.maps.event.addListener(featurePolygon, 'mouseout', function (event) {
          addEventToFeature('shape:mouseout', event, feature, shapeStyle, data);
        });
      });

      function addEventToFeature(eventName, event, feature, shapeStyle, data) {
        _.each(feature, function (f) {
          myself.mapComponent.trigger(eventName, myself.wrapEvent(event, f, 'shape', shapeStyle, data));
        });
      }

    },

    postSetShapes: function () {
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

    setMarker: function (lon, lat, icon, description, data, markerWidth, markerHeight, markerInfo) {
      var myLatLng = new google.maps.LatLng(lat, lon);
      var image = new google.maps.MarkerImage(icon,
        // This marker is 20 pixels wide by 32 pixels tall.
        new google.maps.Size(markerWidth, markerHeight),
        // The origin for this image is 0,0.
        new google.maps.Point(0, 0),
        // The anchor for this image is the base of the flagpole at 0,32.
        new google.maps.Point(0, 0));


      var marker = new google.maps.Marker({
        marker: markerInfo,
        position: myLatLng,
        map: this.map,
        icon: image,
        title: description
      });

      var myself = this;
      google.maps.event.addListener(marker, 'click', function (e) {
        myself.mapComponent.trigger('marker:click', myself.wrapEvent(e, marker, 'marker', markerInfo, data));
      });

    },


    renderMap: function (target) {

      var myOptions = {
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };

      //Prepare tilesets as overlays
      var layers = [],
        layerIds = [],
        layerOptions = [];
      for (var k = 0; k < this.tilesets.length; k++) {
        var thisTileset = this.tilesets[k].slice(0);

        layerIds.push(thisTileset);
        layerOptions.push(_.extend(myOptions, {
          mapTypeId: thisTileset
        }));

        if (this.tileServices[thisTileset]) {
          layers.push(this.tileLayer(thisTileset));
        } else {
          layers.push('');
        }

      } //for tilesets

      // Add base map
      this.map = new google.maps.Map(target, {
        mapTypeControlOptions: {
          mapTypeIds: layerIds.concat(_.values(google.maps.MapTypeId))
        }
      });
      for (k = 0; k < layers.length; k++) {
        if (!_.isEmpty(layers[k])) {
          this.map.mapTypes.set(layerIds[k], layers[k]);
          //this.map.overlayMapTypes.push(layers[k]);
          this.map.setMapTypeId(layerIds[k]);
          this.map.setOptions(layerOptions[k]);
        }
      }

      registerViewportEvents.call(this);
    },


    updateViewport: function (centerLongitude, centerLatitude, zoomLevel) {
      if (!zoomLevel) zoomLevel = 2;
      this.map.setZoom(zoomLevel);

      var centerPoint;
      if (_.isFinite(centerLatitude) && _.isFinite(centerLongitude)) {
        centerPoint = new google.maps.LatLng(centerLatitude, centerLongitude);
        this.centered = true;
        this.map.panTo(centerPoint);
      } else {
        this.map.panTo(new google.maps.LatLng(38, -9));
      }


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
              var s = _.template('${z}/${x}/${y}', {x: coord.x, y: coord.y, z: zoom}, {interpolate: /\$\{(.+?)\}/g});
              url = myself._selectUrl(s, urlList);
            } else {
              url = urlList;
            }
            return _.template(url, {x: coord.x, y: coord.y, z: zoom}, {interpolate: /\$\{(.+?)\}/g});
          }
        }
      }, options));
    },


    showPopup: function (data, mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      var overlay = new OurMapOverlay(mapElement.getPosition(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);

      _.each(this.overlays, function (elt) {
        elt.setMap(null);
      });
      this.overlays.push(overlay);
    }

  });

  return GoogleMapEngine;

  function registerViewportEvents() {
    var me = this;
    var eventMap = {
      'zoom_changed': 'map:zoom',
      'center_changed': 'map:center'
    };
    _.each(eventMap, function (mapEvent, engineEvent) {
      google.maps.event.addListener(me.map, engineEvent, function () {
        var wrappedEvent = wrapViewportEvent.call(me);
        me.mapComponent.trigger.call(me.mapComponent, mapEvent, wrappedEvent);
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
          }
        }
      }
    }
  }


})();