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
  'cdf/lib/Base',
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  './GMapComponentAsyncLoader',],
  function(Base, Logger, $, _, loadGoogleMapsOverlay) {

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

  function submitGeocode(input) {
    return function(e) {
      var keyCode;

      if(window.event) {
          keyCode = window.event.keyCode;
      } /*else if (variable) {
         keyCode = e.which;
         }*/

      if(keyCode == 13) {
        geocoder.geocode({address: input.value}, function(results, status) {
          if(status == google.maps.GeocoderStatus.OK) {
            map.fitBounds(results[0].geometry.viewport);
          } else {
            Logger.warn("The location entered could not be found");
          }
        });
      }
    }
  }

  var GMapEngine = Base.extend({
    map: undefined,
    opts: {
      mapOptions: {
        styles: [{
          featureType: "administrative",
          stylers: [{"visibility": "off"}]
        }],
        disableDefaultUI: false,
        mapTypeControl: false,
        streetViewControl: false
      }
    },
    opened_info: undefined,
    centered: false,
    overlays: [],

    init: function(mapComponent) {

      $.when(loadGoogleMapsOverlay()).then(function(status) {
        mapComponent.draw();
      });
    },

    createMap: function(target, centerLongitude, centerLatitude, defaultZoomLevel, mapHeight, mapWidth) {
      // see possible features on https://developers.google.com/maps/documentation/javascript/reference#MapTypeStyleFeatureType
      var mapOptions = $.extend(true, {
        zoom: parseInt(defaultZoomLevel),
        center: new google.maps.LatLng(centerLatitude, centerLongitude), 
        mapTypeId: google.maps.MapTypeId.TERRAIN
      }, this.opts.mapOptions);

      this.map = new google.maps.Map(target, mapOptions);
      this.opened_info = new google.maps.InfoWindow();
    
      $(target).css("height", mapHeight + "px");
      $(target).css("width", mapWidth + "px");

    },

    renderMap: function(mapDefinition, queryResult, defaultColor, legend) {
  
      if(!mapDefinition) { return; }
      var myself = this;

      for(var c in mapDefinition) {
        var coods = mapDefinition[c], polyPath = [];
          
        for(var k = 0; k < coods.length; k++) {
          polyPath.push(new google.maps.LatLng(coods[k][0], coods[k][1]));
        }

        var shapeinfo = {
          fillColor: !!queryResult[c] ? queryResult[c].fillColor: defaultColor,
          fillOpacity: !!queryResult[c] ? queryResult[c].fillOpacity : 0,
          strokeWeight: !!queryResult[c] ? queryResult[c].strokeWeight : 0,
          strokeColor: '#8c8c8c'
        };

        var shape = new google.maps.Polygon(_.extend({
          paths: polyPath
        }, shapeinfo));


        var shapeValue = queryResult[c] ? queryResult[c].value : null;

        shape.infowindow = new google.maps.InfoWindow({
          content: myself.tooltipMessage(c, shapeValue),
          pixelOffset: {width: 0, height: -3}
        });
        
        shape.infowindow.dataPayload = _.extend({
          name: c,
          value: shapeValue,
          level: queryResult[c] ? queryResult[c].level : 0
        }, shapeinfo);

        if(!!queryResult[c]) {
          queryResult[c].shape = shape;
        }

        shape.setMap(myself.map);
        google.maps.event.addListener(shape, 'click', function(event) {
          myself.clickCallback(this.infowindow, event);
          myself.displayCoordinates(event.latLng);
        });
      
        google.maps.event.addListener(shape, 'click', function(event) {
          this.fillOpacity = 1;
          this.strokeColor = "#000000";
          this.setVisible(false);
          this.setVisible(true);
          this.infowindow.setOptions({maxWidth: 500});
          this.infowindow.setPosition(event.latLng);
          if(!this.infowindow.getMap()) {
            this.infowindow.open(myself.map);
          }
          myself.opened_info = this.infowindow;

        });

        /*
        google.maps.event.addListener(shape, 'mousemove',function (event) {
          this.strokeColor= "#000000";
          this.setVisible(false);
          this.setVisible(true);
        });
        */
        
        google.maps.event.addListener(shape, 'mouseout', function(event) {
          //this.strokeWeight=0.5;
          myself.opened_info.close();
          this.fillOpacity = 0.6;
          this.strokeColor = "#8c8c8c";
          this.setVisible(false);
          this.setVisible(true);
        });
        
      }
    },
  
    tooltipMessage: function(shapeName, shapeValue) {
    var message = shapeName + "</br>" + (shapeValue ? shapeValue : '-');
      return '<div class="gmapsoverlay-tooltip">' + message + '</div>';
    },
  
    // Apenas para debug de codigo
    clickCallback: function(shape, event) {
      //Override this
      Logger.log(shape.dataPayload.name + ':' + shape.dataPayload.value + ':' + shape.dataPayload.level * 100 + '%');
    },
  
    // Apenas para debug, mostra Lat/Lng no console
    displayCoordinates: function(pnt) {
      var lat = pnt.lat();
      lat = lat.toFixed(4);
      var lng = pnt.lng();
      lng = lng.toFixed(4);
      Logger.log("Lat: " + lat + "  Lng: " + lng);
    },
  
    showInfo: function(event, mapEngine, infowindow) {
      mapEngine.opened_info.close();
      //if (mapEngine.opened_info.name != infowindow.name) {
      infowindow.setPosition(event.latLng);
      infowindow.open(mapEngine.map);
      mapEngine.opened_info = infowindow;
      //}
    },

    resetButton: function(id, zoom, centerLongitude, centerLatitude) {

      var myself = this;

      var controlReset = document.createElement('div');
      var linkReset = document.createElement('a');
      controlReset.appendChild(linkReset);
      controlReset.setAttribute('id', 'controlReset_' + id);
      linkReset.setAttribute('id', 'linkReset_' + id);
      linkReset.href = "javascript:void(0)";
      linkReset.className = 'gmapsoverlay-button';
      linkReset.onclick = (function() {  myself.map.setZoom(zoom);
        myself.map.setCenter(new google.maps.LatLng(centerLatitude, centerLongitude));
      });
      linkReset.innerHTML = 'Reset';

      myself.map.controls[google.maps.ControlPosition.TOP_LEFT].push(controlReset);
    },

    searchBox: function(id) {

      var myself = this;

      var control = document.createElement('div');
      var input = document.createElement('input');
      control.appendChild(input);
      control.setAttribute('id', 'locationField_' + id);
      input.style.width = '250px';
      input.style.height = '100%';
      input.style.margin = '0px';
      input.style.border = '1px solid #A9BBDF';
      input.style.borderRadius = '2px';
      input.setAttribute('id', 'locationInput_' + id);
      myself.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(control);

      var ac = new google.maps.places.Autocomplete(input, {types: ['geocode']});
      google.maps.event.addListener(ac, 'place_changed', function() {
        var place = ac.getPlace();
        if(place.geometry.viewport) {
          myself.map.fitBounds(place.geometry.viewport);
        } else {
          myself.map.setCenter(place.geometry.location);
          myself.map.setZoom(17);
        }
      });

      google.maps.event.addListener(myself.map, 'bounds_changed', function() {
        input.blur();
        input.value = '';
      });

      input.onkeyup = submitGeocode(input);
    },
  
    renderLegend: function(id, mapDefinition, queryResult, colormap, ticks, legendRanges, isContinuousMapColor, isColorDefinedInDS) {
      /*
        var engNotation = function(d) {
          var Log1000 = Math.log(1000);
          var engLabels = ['', ' k' , ' M', ' G' , ' T', ' P'];
          var exponent3 = (d == 0
            ? 0
            : Math.floor(Math.round(100 * Math.log(d)/Log1000 )/100));
          var mantissa = Math.round(100* d / Math.pow(1000, exponent3))/100;
          return mantissa + engLabels[exponent3];
        };
      */

      if(isContinuousMapColor) {
        var sigFigs = function(num, sig) {
          if(num == 0) { return 0; }
          if(Math.round(num) == num) { return num; }
          var digits = Math.round((-Math.log(Math.abs(num)) / Math.LN10) + (sig || 2));
          //round to significant digits (sig)
          if(digits < 0) { digits = 0; }
          return num.toFixed(digits);
        };


        if(queryResult && mapDefinition) {
          var values = _.map(queryResult, function(q) { return q.value; });
          var minValue = _.min(values), maxValue = _.max(values);
          var n = colormap.length;
          var rounding = 1;
          if(maxValue < -5) {
            rounding = ((maxValue - minValue) / 5).toString().split('.');
            rounding = rounding.length > 1 ? Math.pow(10, Math.max(rounding[1].length, 3)): 1;
          }
          var legend = _.map(ticks, function(level) {
            var value = (minValue + level * (maxValue - minValue) * rounding) / rounding;
            return {
              value: sigFigs(value, 1),
              level: level,
              fillColor: colormap[Math.floor(level * n -1)]
            };
          });
        }

        this.legend = legend;
      }

      // Set CSS styles for the DIV containing the control
      // Setting padding to 5 px will offset the control
      // from the edge of the map
      var controlDiv = document.createElement('DIV');
      controlDiv.style.padding = '5px';
      controlDiv.setAttribute('id', 'legendDiv_' + id);

      // Set CSS for the control border
      var controlUI = document.createElement('DIV');
      controlUI.setAttribute('id', 'legendUI_' + id);
      //controlUI.style.backgroundColor = 'white';
      //controlUI.style.borderStyle = 'solid';
      //controlUI.style.borderWidth = '1px';
      controlUI.title = 'Legend';
      controlDiv.appendChild(controlUI);

      // Set CSS for the control text
      var controlText = document.createElement('DIV');
      controlText.setAttribute('id', 'legendText_' + id);
      controlText.style.fontFamily = 'Arial,sans-serif';
      controlText.style.fontSize = '12px';
      controlText.style.paddingLeft = '4px';
      controlText.style.paddingRight = '4px';

      if(isContinuousMapColor) {
        var legendTable = '';
        _.each(legend, function(el) {
          var left = (el.level != 0) ? el.level * 100 + '%' : '-1px';
          legendTable += "<div class='gmapsoverlay-legend-label' style='left:" + left
            + ";position:absolute;'><div>" + el.value + "</div></div>";
        });
        
        // Add the text
        controlText.innerHTML =
          "<div class='gmapsoverlay-legend'>" +
          "  <div class='gmapsoverlay-legend-title'>" + legendRanges.text + "</div>" +
          "  <div class='gmapsoverlay-legend-scale'>" +
          "    <div class='gmapsoverlay-legend-labels'>" + legendTable + "</div>" +
          "  </div>" +
          "  <div class='gmapsoverlay-legend-source'>" + legendRanges.source + "</div>" +
          "</div>";

      } else {
        var legendTable = "";
        var qtd = Object.keys(legendRanges.ranges).length;
        for(var j = 0; j < qtd; j++) {
        
          if(isColorDefinedInDS) {

            legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'></span>"
              + legendRanges.ranges[j].desc + "</li>";

          } else if (isNaN(legendRanges.ranges[j].min)) {

            legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'><= "
              + legendRanges.ranges[j].max + "</span>" + legendRanges.ranges[j].desc + "</li>";

          } else if (isNaN(legendRanges.ranges[j].max)) {

            legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'>>= "
              + legendRanges.ranges[j].min + "</span>" + legendRanges.ranges[j].desc + "</li>";

          } else {

            legendTable += "<li><span style='background:" + legendRanges.ranges[j].color + ";'>"
              + legendRanges.ranges[j].max + "</span>" + legendRanges.ranges[j].desc + "</li>";

          }
        }
      
        // Add the text
        controlText.innerHTML =
          "<div class='gmapsoverlay-legend' style='width: auto'>" +
          "  <div class='gmapsoverlay-legend-title'>" + legendRanges.text + "</div>" +
          "  <div class='gmapsoverlay-legend-scale-range'>" +
          "    <ul class='gmapsoverlay-legend-labels-range'>" + legendTable + "</ul>" +
          "  </div>" +
          "  <div class='gmapsoverlay-legend-source'>" + legendRanges.source + "</div>" +
          "</div>";
      }

      controlUI.appendChild(controlText);
      this.map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(controlDiv);
      
    },

    showPopup: function(data, mapElement, popupHeight, popupWidth, contents, popupContentDiv, borderColor) {
      var overlay = new OurMapOverlay(mapElement.getPosition(), popupWidth, popupHeight, contents, popupContentDiv, this.map, borderColor);

      $(this.overlays).each(function(i, elt) {elt.setMap(null);});
      this.overlays.push(overlay);
    }

  });

  return GMapEngine;

});
