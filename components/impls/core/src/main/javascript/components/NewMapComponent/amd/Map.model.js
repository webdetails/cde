/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "cdf/Logger",
  "./model/MapModel",
  "./_getMapping",
  "./FeatureStore/resolveShapes",
  "./FeatureStore/resolveMarkers"
], function($, _, Logger,
             MapModel,
             getMapping,
             resolveShapes, resolveMarkers) {

  "use strict";
  return {
    resolveFeatures: function(json) {
      var mapping = getMapping(json);
      this.mapping = $.extend(true, mapping, this.visualRoles);
      this.features = this.features || {};

      var me = this;
      var deferred;
      if (this.mapMode === "shapes") {
        deferred = this._resolveShapes(json, this.mapping, this.configuration)
          .then(function(shapeDefinition) {
            me.features.shapes = shapeDefinition;
            return json;
          });
      } else {
        if (this.mapMode === "markers") {
          deferred = this._resolveMarkers(json, this.mapping, this.configuration)
            .then(function(markerDefinitions) {
              me.features.markers = markerDefinitions;
              return json;
            });
        } else {
          deferred = $.when(json);
        }
      }
      return deferred.promise();
    },

    _resolveShapes: resolveShapes,

    _resolveMarkers: resolveMarkers,

    initModel: function(json) {

      this.model = new MapModel({
        styleMap: this.getStyleMap("global")
      });
      this.model.set("canSelect", this.configuration.isSelector);
      if (this.configuration.isSelector === true) {
        this.model.setSelectionMode();
      } else {
        this.model.setPanningMode();
      }

      var seriesRoot = this._initSeries(this.mapMode, json);
      if (json && json.metadata && json.resultset && json.resultset.length > 0) {
        this._addSeriesToModel(seriesRoot, json);
      }

    },

    _initSeries: function(seriesId, json) {
      var colormap = this.getColorMap();
      var seriesRoot = {
        id: seriesId,
        label: seriesId,
        styleMap: this.getStyleMap(seriesId),
        colormap: colormap,
        extremes: this._detectExtremes(json)
      };
      this.model.add(seriesRoot);
      return this.model.findWhere({id: seriesId});
    },

    visualRoles: {},

    scales: {
      fill: "default", //named colormap, or a colormap definition
      r: [10, 20]
    },

    attributeMapping: {
      fill: function(context, seriesRoot, mapping, row) {
        var value = row[mapping.fill];
        var colormap = seriesRoot.get("colormap") || this.getColorMap();
        if (_.isNumber(value)) {
          return this.mapColor(value,
            seriesRoot.get("extremes").fill.min,
            seriesRoot.get("extremes").fill.max,
            colormap
          );
        }
      },
      label: function(context, seriesRoot, mapping, row) {
        return _.isEmpty(row) ? undefined : (row[mapping.label] + "");
      },
      r: function(context, seriesRoot, mapping, row) {
        var value = row[mapping.r];
        if (_.isNumber(value)) {
          var rmin = this.scales.r[0];
          var rmax = this.scales.r[1];
          var v = seriesRoot.get("extremes").r;
          //var r = rmin + (value - v.min)/(v.max - v.min)*(rmax-rmin); //linear scaling
          var r = Math.sqrt(rmin * rmin + (rmax * rmax - rmin * rmin) * (value - v.min) / (v.max - v.min)); //sqrt scaling, i.e. area scales linearly with data
          if (_.isFinite(r)) {
            return r;
          }
        }
      }
    },

    _detectExtremes: function(json) {
      var extremes = _.chain(this.mapping)
        .map(function(colIndex, role) {
          if (!_.isFinite(colIndex)) {
            return [role, {}];
          }
          var values = _.pluck(json.resultset, colIndex);
          var obj;
          if (json.metadata[colIndex].colType === "Numeric") {
            obj = {
              type: "numeric",
              min: _.min(values),
              max: _.max(values)
            };
          } else {
            obj = {
              type: "categoric",
              items: _.uniq(values)
            };
          }
          return [role, obj];

        })
        .object()
        .value();

      return extremes;
    },

    _addSeriesToModel: function(seriesRoot, json) {
      var mapping = $.extend({}, this.mapping);

      var colNames = _.pluck(json.metadata, "colName");

      var me = this;
      var modes = MapModel.Modes,
        states = MapModel.States,
        actions = MapModel.Actions;
      var series = _.map(json.resultset, function(row, rowIdx) {

        var id = me._getItemId(mapping, row, rowIdx);
        var styleMap = {};

        _.each(modes, function(mode) {
          _.each(states, function(state) {
            _.each(actions, function(action) {
              _.each(me.attributeMapping, function(functionOrValue, attribute) {
                if (_.isUndefined(mapping[attribute]) || mapping[attribute] >= row.length) {
                  return; //don't bother running the function when attribute is not mapped to the data
                }
                var context = {
                  mode: mode,
                  state: state,
                  action: action
                };
                var value = _.isFunction(functionOrValue) ? functionOrValue.call(me, context, seriesRoot, mapping, row, rowIdx) : functionOrValue;
                if (_.isUndefined(value)) {
                  return;
                }
                styleMap[mode] = styleMap[mode] || {};
                styleMap[mode][state] = styleMap[mode][state] || {};
                styleMap[mode][state][action] = styleMap[mode][state][action] || {};
                styleMap[mode][state][action][attribute] = value;
              });
            });
          });

        });

        var shapeDefinition = me.features.shapes ? me.features.shapes[id] : undefined;
        var markerDefinition = me.features.markers ? me.features.markers[id] : undefined;
        var geoJSON = (seriesRoot.getFeatureType() === "shape") ? shapeDefinition : markerDefinition;

        return {
          id: id,
          label: id,
          styleMap: styleMap,
          geoJSON: geoJSON,
          rowIdx: rowIdx,
          rawData: row,
          data: _.object(_.zip(colNames, row))
        };

      });

      seriesRoot.add(series);
    },

    _getItemId: function(mapping, row, rowIdx) {
      var indexId = mapping.id;
      if (!_.isFinite(indexId)) {
        if (this.mapMode === "shapes") {
          indexId = 0;
        } else {
          indexId = -1; //Use rowIdx instead
        }
      }
      return (indexId >= 0 && indexId < row.length) ? row[indexId] : rowIdx;
    }

  };

});
