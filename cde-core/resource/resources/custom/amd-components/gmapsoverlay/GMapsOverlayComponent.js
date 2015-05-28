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

/*

Contributions by Carlos Russo from Webdetails.pt

* TODO Consider using .kml files directly, see https://code.google.com/p/geoxml3/
* TODO Attempt merging with NewMapComponent
* TODO Attempt using API of https://github.com/mapstraction/mxn/

*/

define([
  'cdf/components/UnmanagedComponent',
  'cdf/Logger',
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  './GMapEngine',
  './GMapsOverlayComponentExt',
  'css!./GMapsOverlayComponent'],
  function(UnmanagedComponent, Logger, $, _, GMapEngine, GMapsOverlayComponentExt) {

  var GMapsOverlayComponent = UnmanagedComponent.extend({

    mapEngineOpts: undefined, //override this in preExec
    colormap: [[0, 102, 0, 255], [255, 255 ,0,255], [255, 0,0, 255]], //RGBA
    
    // Get color based on a Range
    getColorLegend: function (value, legendRanges) {
      var qtd = Object.keys(legendRanges.ranges).length;
      for(var j = 0; j < qtd; j++) {
        if((isNaN(legendRanges.ranges[j].min) && value <= legendRanges.ranges[j].max) ||
          (isNaN(legendRanges.ranges[j].max) && value >= legendRanges.ranges[j].min) ||
          (value >= legendRanges.ranges[j].min && value <= legendRanges.ranges[j].max)) {

          return legendRanges.ranges[j].color;
        }
      }
    },

    // Get color based on Continuous Color Map
    getColorMap: function() {

      var interpolate = function(a, b, n) {
        var c = [], d=[];
        var k, kk, step;
        for(k = 0; k < a.length; k++) {
          c[k] = [];
          for(kk = 0, step = (b[k] - a[k]) / n; kk < n; kk++) {
            c[k][kk] = a[k] + kk * step;
          }
        }
        for(k = 0; k < c[0].length; k++) {
          d[k] = [];
          for(kk = 0; kk < c.length; kk++) {
            d[k][kk] =  Math.round(c[kk][k]);
          }
        }
        return d;
      };
  
      var cmap = [];
  
      for(var k = 1; k < this.colormap.length; k++) {
        cmap = cmap.concat(interpolate(this.colormap[k - 1], this.colormap[k], 512));
      }
  
      return _.map(cmap, function(v) {
        return 'rgba('+ v.join(',') +')';
      });
    },
  
    _getMapDefinition: function(myself, callback) {

      if(!!myself.mapName & !myself.mapDefinition) {
        var url = (/\.[a-zA-Z]+$/).test(myself.mapName)
          // allow the map definition file to be in the sample folder
          ? GMapsOverlayComponentExt.getResourceUrl() + myself.mapName
          : GMapsOverlayComponentExt.getResourceUrl() + GMapsOverlayComponentExt.getBaseSolutionPluginRoot() +
            "cde/components/gmapsoverlay/map-def/" + myself.mapName + ".js"; 
        
        $.getJSON(url, function(json, callback) {
          if(json) {
            myself.mapDefinition = json;
          }
        });
      }
      //Logger.log('mapDefinition :' + _.keys(this.mapDefinition));
      callback(myself);
    },
  
    postProcessData: function(values, myself) {
  
      /*
       * do a postProcessing, something like a postPostFetch
       */

      var defaultFillOpacity = 0.6;
      var defaultStrokeWeight = 0.5;
      
      myself.queryResult = {};
      myself.isContinuousMapColor = $.isEmptyObject(myself.legend);
      var nrCols = values.metadata.length;
      
      for(var i in values.resultset) {
        var item = values.resultset[i];
        
        var value, color;
        if(nrCols < 3) { // DataSet onde a legenda é baseada no valor
          value = parseFloat(item[1]);
          color = "";
          myself.isColorDefinedInDS = false;
        } else { // DataSet com a 3a coluna informando qual cor utilizar, nao utilizar Legenda nem Mapa de Cor continuo para definir cor da regiao.
          value = item[1];
          color = item[2];
          myself.isColorDefinedInDS = true;
        }
        
        myself.queryResult[item[0]]  = {
          'value': value,
          'color': color
        };
        
        if(item.length > 2) {
          myself.queryResult[item[0]].payload = item.slice(2);
        }
      }

      myself._parseLegend(myself.isContinuousMapColor);
      
      // patch queryResult with color information
      if(myself.isContinuousMapColor) {
        var colormap = myself.getColorMap();
        var qvalues = _.map(myself.queryResult, function(q) { return q.value; });
        var minValue = _.min(qvalues), maxValue = _.max(qvalues);
        var n = colormap.length;
        _.each(myself.queryResult, function(v, key) {

          var level =  (v.value - minValue) / (maxValue - minValue);

          myself.queryResult[key] = _.extend({
            level: level,
            fillColor: colormap[Math.floor(level * (n - 1)) ],
            fillOpacity: defaultFillOpacity,
            strokeWeight: defaultStrokeWeight
          }, myself.queryResult[key]);

        });
      } else {
        _.each(myself.queryResult, function(v, key) {
        
          var color;
          if(myself.isColorDefinedInDS) {
            color = v.color;
          } else {
            color = myself.getColorLegend(v.value, myself.legendRanges);
          }

          myself.queryResult[key] = _.extend({
            fillColor: color,
            fillOpacity: defaultFillOpacity,
            strokeWeight: defaultStrokeWeight
          }, myself.queryResult[key]);

        });
      }

    },
  
    _parseLegend: function(isContinuousMapColor) {
  
      this.legendRanges = new Object;
    
      this.legendRanges.ranges = new Object;
      this.legendRanges.text = ((!this.legendText) ? "" : this.legendText);
      this.legendRanges.source = ((!this.sourceText) ? " " : this.sourceText);

      if(!isContinuousMapColor) {
        for(var i = 0; i < this.legend.length; i++) {
          var opts = this.legend[i][1].split(";");
          this.legendRanges.ranges[i] = new Object;
          this.legendRanges.ranges[i].min = parseFloat(opts[0]);
          this.legendRanges.ranges[i].max = parseFloat(opts[1]);
          this.legendRanges.ranges[i].color = opts[2];
          this.legendRanges.ranges[i].desc = this.legend[i][0];
        }
      }
    },
  
    update: function() {

      var myself = this;

      if($.isEmptyObject(myself.queryDefinition))  {  
        Logger.error("GMaps - Datasource not defined.");
        return;
      }
      
      if(!myself.mapName)  {  
        Logger.error("GMaps - Map Name not defined.");
        return;
      }
      
      if(!myself.mapHeight || !myself.mapWidth) {
        Logger.error("GMaps - Map Height and/or Width not defined.");
        return;
      }
      
      // first get the map definition (asynchronously), and then launch triggerQuery (asynchronously)
      myself._getMapDefinition(myself, function(myself) {
        myself.triggerQuery(myself.queryDefinition, function(values) {

          myself.postProcessData(values, myself);
          
          // Start Google Map stuff
          myself._initialize();

        });
      });
    },

    _initialize: function() {
      this.mapEngine = new GMapEngine();
      this.mapEngine.opts = $.extend(true, this.mapEngine.opts, this.mapEngineOpts);
      if(this.clickCallback){
        this.mapEngine.clickCallback = this.clickCallback;
      }
      this.mapEngine.init(this);
    },

    draw: function() {
      var myself = this;
      myself.ph = $("#" + myself.htmlObject);
      myself.ph.empty();

      myself.mapEngine.createMap(myself.ph[0], myself.centerLongitude, myself.centerLatitude, myself.defaultZoomLevel, myself.mapHeight, myself.mapWidth);
      myself.mapEngine.renderMap(myself.mapDefinition, myself.queryResult, ((!myself.defaultColor) ? "#EAEAEA" : myself.defaultColor), myself.legendRanges);
      myself.mapEngine.resetButton(myself.ph[0].id, myself.defaultZoomLevel, myself.centerLongitude, this.centerLatitude);

      if(myself.search == true) {
        myself.mapEngine.searchBox(myself.ph[0].id);
      }
      
      myself.mapEngine.renderLegend(
        myself.ph[0].id,
        myself.mapDefinition,
        myself.queryResult,
        myself.getColorMap(),
        [0, 0.5, 1],
        myself.legendRanges,
        myself.isContinuousMapColor,
        myself.isColorDefinedInDS);

    }

  });

  return GMapsOverlayComponent;

});
