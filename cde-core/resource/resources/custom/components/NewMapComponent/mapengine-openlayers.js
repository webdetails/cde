/*
 * OpenLayers engine.
 *
 */


var OpenLayersEngine = MapEngine.extend({
  map: undefined,
  markers: undefined,
  centered: false,
  //    featureLayer: undefined,
  useMercator: true,
  API_KEY: 0,
  init: function(mapComponent, tilesets) {
    this.tilesets = tilesets;
    this.mapComponent = mapComponent; // the callbacks will use this
    Dashboards.log ('Requested tilesets:' + JSON.stringify(tilesets), 'debug');

    var contains = function(v){
      return _.some(tilesets, function(tileset){
        //Dashboards.log(tileset, 'debug');
        return tileset.search(v) >= 0;
      });
    };

    if (contains('googleXXX')) {
      // This is (probably) only needed if we use the OpenLayers.Layer.Google API,
      $.when( loadGoogleMaps('3', this.API_KEY) ).then ( mapComponent.initCallBack );
    } else {
      mapComponent.initCallBack();
    }
  },


  setShape: function(multiPolygon, shapeStyle, data) {
    var myself =this;
    var proj = new OpenLayers.Projection("EPSG:4326"),  // transform from WGS 1984 //4326
        mapProj = this.map.getProjectionObject();

    var multiPolygonOL = _.map(multiPolygon, function(polygon){
      var polygonOL = _.map(polygon, function (ring) {
        var linearRingOL = _.map(ring, function (latlong){
          var point = new OpenLayers.LonLat(latlong[1], latlong[0] ).transform(
            proj, // transform from WGS 1984
            mapProj // to the map system
          );
          return new OpenLayers.Geometry.Point(point.lon, point.lat);
        });
        return new OpenLayers.Geometry.LinearRing(linearRingOL);
      });
      return new OpenLayers.Geometry.Polygon(polygonOL);
    });

    var shape = new OpenLayers.Geometry.MultiPolygon(multiPolygonOL);
    var feature = new OpenLayers.Feature.Vector(shape, {data: data, style: shapeStyle}, myself.toNativeStyle(shapeStyle) );
    myself.shapes.addFeatures([feature]);
    //$('#' + feature.id ).tipsy({gravity: 'n', title: function(){return JSON.stringify(data);}});

  },

  postSetShapes: function(){},

  toNativeStyle: function (foreignStyle){
    var validStyle = {};
    _.each(foreignStyle, function (value, key){
      switch(key){
      case 'visible':
        validStyle['display'] = value ? true : 'none';
        break;
      case 'zIndex':
        validStyle['graphicZIndex'] = value;
        break;
      case 'fillColor':
      case 'fillOpacity':
      case 'strokeColor':
      case 'strokeOpacity':
      case 'strokeWidth':
        validStyle[key] = value;
      }
    });
    return validStyle;
  },

  wrapEvent: function (event, featureType){
    var lastXy = this.map.getControlsByClass("OpenLayers.Control.MousePosition")[0].lastXy || {x: undefined, y: undefined};
    var coords = this.map.getLonLatFromPixel(lastXy)
          .transform(this.map.getProjectionObject(), new OpenLayers.Projection('EPSG:4326')
                    );
    var feature = event.feature.layer.getFeatureById(event.feature.id);
    var myself = this;
    return {
      latitude: coords.lat,
      longitude: coords.lon,
      data: event.feature.attributes.data,
      feature: feature, // can refer to either the shape or the marker
      featureType: featureType,
      style: event.feature.attributes.style, // currently only shape styles
      marker: event.feature.attributes.marker, //marker-specific attributes
      mapEngineType: 'openlayers2',
      draw: function( style ){
        // currently only makes sense to be called on shape callbacks
        var validStyle = myself.toNativeStyle(style);
        event.feature.layer.drawFeature(feature, validStyle);
      },
      // isSelected: false &&  _.some(_.map(event.feature.layer.selectedFeatures, function(el){
        // return (el.id == event.feature.id);
      // })),
      raw: event
    };
  },


  setMarker: function(lon, lat, icon, description, data, markerWidth, markerHeight, markerInfo) {
    var size = new OpenLayers.Size(markerWidth,markerHeight);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var iconObj = new OpenLayers.Icon(icon,size,offset);

    var proj = new OpenLayers.Projection("EPSG:4326"),  // transform from WGS 1984 //4326
        mapProj = this.map.getProjectionObject();
    var point = new OpenLayers.LonLat(lon, lat).transform(
      proj, // transform from WGS 1984
      mapProj // to the map system
    );
    var marker = new OpenLayers.Geometry.Point(point.lon, point.lat);
    var style;
    var feature = new OpenLayers.Feature.Vector(marker, {
      data: data,
      style: style,
      marker: markerInfo
    }, {
      externalGraphic: icon,
      graphicWidth: markerWidth,
      graphicHeight: markerHeight,
      fillOpacity: 1
    });

    this.markers.addFeatures([feature]);

    if (!this.centered)
      this.map.setCenter(point);
  },

  showPopup: function(data,  mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {

    var feature = mapElement;

    if (popupContentDiv && popupContentDiv.length > 0) {
      var div = $('<div>');
      div.append($('#' + popupContentDiv));
      contents = div.html();
    }

    var name = "featurePopup";
    if (borderColor != undefined) {
      name = name + borderColor.substring(1);
    }

    var p = mapElement.geometry.getCentroid(); // Hack to get the point
    feature.lonlat = new OpenLayers.LonLat(p.x, p.y);

    var popup = new OpenLayers.Popup.Anchored(name,
                                          feature.lonlat,
                                          new OpenLayers.Size(popupWidth, popupHeight),
                                          contents,
                                          null, true, null);

    feature.popup = popup;
    popup.feature = feature;

    $(this.map.popups).each(function (i, elt) {elt.hide();});

    this.map.addPopup(popup, true);
  },

  renderMap: function(target, centerLongitude, centerLatitude, zoomLevel) {
    Dashboards.log('Entered renderMap', 'debug');
    var myself = this;
    var useLayerControl = false;
    var customMap = false;
    var centerPoint;

    if (centerLongitude && centerLongitude != '' && centerLatitude && centerLatitude != '') {
      if(this.useMercator) {
        centerPoint = lonLatToMercator(new OpenLayers.LonLat(centerLongitude,centerLatitude));
      }else{
        centerPoint = new OpenLayers.LonLat(centerLongitude,centerLatitude);
      }
    }

    this.map = new OpenLayers.Map(target, {
      maxExtent: new OpenLayers.Bounds(-20037508,-20037508,20037508,20037508),
      numZoomLevels: 18,
      maxResolution: 156543,
      units: 'm',
      zoomDuration: 10, // approximately match Google's zoom animation
      displayProjection: new OpenLayers.Projection("EPSG:4326"),
      projection: "EPSG:900913",
      controls: [
        new OpenLayers.Control.Navigation(),
        // new OpenLayers.Control.NavToolbar(),
        // new OpenLayers.Control.PanZoom(),
        new OpenLayers.Control.ZoomPanel(),
        new OpenLayers.Control.DragPan(),
        new OpenLayers.Control.PinchZoom(),
        new OpenLayers.Control.LayerSwitcher({'ascending':false}),
        new OpenLayers.Control.ScaleLine(),
        new OpenLayers.Control.KeyboardDefaults(),
        new OpenLayers.Control.MousePosition(),
        new OpenLayers.Control.Attribution(),
        new OpenLayers.Control.TouchNavigation()
      ]
    });
    //OpenLayers.ImgPath = 'resources/components/NewMapComponent/openlayers_themes/dark/'; //theme the buttons
    var myOptions = {
      type: 'png',
      transparent: 'true',
      transitionEffect: 'resize',
      displayOutsideMaxExtent: true
    };
    var layer;
    for (var k=0, m=this.tilesets.length; k<m; k++){
      var thisTileset = this.tilesets[k],
          tileset = this.tilesets[k].slice(0).split('-')[0],
          variant = this.tilesets[k].slice(0).split('-').slice(1).join('-') || 'default';
      Dashboards.log('Tilesets: ' + JSON.stringify(this.tilesets)  + ', handling now :' + thisTileset + ', ie tileset ' + tileset + ', variant ' + variant);
      switch(tileset){
      case 'googleXXX':
        layer =  new OpenLayers.Layer.Google("Google Streets", {visibility: true, version: '3'});
        break;

      case 'opengeo':
        layer = new OpenLayers.Layer.WMS( thisTileset,
                                          "http://maps.opengeo.org/geowebcache/service/wms",
                                          {
                                            layers: variant,
                                            //format: format,
                                            bgcolor: '#A1BDC4'
                                          },
                                          {
                                            wrapDateLine: true,
                                            transitionEffect: 'resize'
                                          });
        break;

      default:
        layer = this.tileLayer(thisTileset);
        break;
      }

      // add the OpenStreetMap layer to the map
      this.map.addLayer(layer);
    }

    // add layers for the markers and for the shapes
    this.shapes = new OpenLayers.Layer.Vector( 'Shapes',  {
      rendererOptions: {
        zIndexing: true
      }
    });

    this.shapes.styleMap = new OpenLayers.StyleMap({
      'default': {
        graphicZIndex: 0
      },
      'select': {
        graphicZIndex: 1
      }
    });
    this.markers = new OpenLayers.Layer.Vector( "Markers" );

    this.map.addLayers([this.shapes, this.markers]);
    this.setCallbacks();

    //set center and zoomlevel of the map
    if (centerPoint) {
      this.map.setCenter(centerPoint);
      this.centered = true;
    } else {
      this.map.setCenter(lonLatToMercator(new OpenLayers.LonLat(-9.15,38.46)));
    }

    if (zoomLevel != '')
      this.map.zoomTo(zoomLevel);

    Dashboards.log('NewMapComponent: exited renderMap', 'debug');
    this.postRenderMap();

  },

  postRenderMap: function(){
    // Hook for adding extra rendering code
    //
    // var layer = new OpenLayers.Layer.GML("KML", "static/custom/maps/PoliceBeats.kml", {
    //     projection: "EPSG:4326",
    //     format: OpenLayers.Format.KML,
    //     formatOptions: {
    //         'extractStyles': true
    //     }
    // });
    // this.map.addLayer(layer);
  },

  setCallbacks: function(){
    var myself = this;

    function event_relay(e){
      var prefix;
      if (e.feature.layer.name == "Shapes"){
        prefix = 'shape';
      } else {
        prefix = 'marker';
      }
      var events = {
        'featurehighlighted': 'mouseover',
        'featureunhighlighted': 'mouseout',
        'featureselected': 'click'
      };
      if (events[e.type]){
        myself.mapComponent.trigger(prefix + ':' + events[e.type], myself.wrapEvent(e));
      }
    }

    var hoverCtrl = new OpenLayers.Control.SelectFeature( [this.markers, this.shapes], {
      hover: true,
      highlightOnly: true,
      renderIntent: "temporary",
      eventListeners: {
        featurehighlighted: event_relay,
        featureunhighlighted: event_relay,
        featureselected: event_relay
      },
      // this version of OpenLayers has issues with the outFeature function
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
              });;
            }
          } else {
            this.unselect(feature);
          }
        }
      }
    });
    this.map.addControl(hoverCtrl);
    hoverCtrl.activate();
    var clickCtrl = new OpenLayers.Control.SelectFeature( [this.markers, this.shapes], {
      clickout: false
    });
    this.map.addControl(clickCtrl);
    clickCtrl.activate();

    this.markers.events.on({
      featurehighlighted: function(e) { myself.mapComponent.trigger('marker:mouseover', myself.wrapEvent(e)); },
      featureunhighlighted:  function(e) { myself.mapComponent.trigger('marker:mouseout', myself.wrapEvent(e)); },
      featureselected: function(e) {
        myself.mapComponent.trigger('marker:click', myself.wrapEvent(e));
        // The feature remains selected after we close the popup box, which disables clicking on the same box.
        // Thus we enforce that no marker is selected.
        clickCtrl.unselectAll();
      }
    });

    this.shapes.events.on({
      featureselected: function(e) {myself.mapComponent.trigger('shape:click', myself.wrapEvent(e));}
    })

  },

  tileLayer: function(name){
    var urlTemplate = this.tileServices[name];
    var options = _.extend({
      "transitionEffect": "resize"
    }, this.tileServicesOptions[name] || {});
    if (!urlTemplate){
      // Allow the specification of an url from CDE
      if (name.length>0 && name.contains('{')){
        urlTemplate = name;
        name = 'custom';
      }
    }
    return new OpenLayers.Layer.XYZ( name, this._switchUrl(urlTemplate), _.defaults({}, options));

  }
});
