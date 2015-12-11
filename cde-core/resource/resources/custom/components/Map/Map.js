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

define("cde/components/Map/Map.lifecycle", [
  "amd!cdf/lib/underscore"
], function(_) {
  "use strict";
  return {
    maybeToggleBlock: function(block) {
      if (!this.isSilent()) {
        block ? this.block() : this.unblock();
      }
    },

    getQueryData: function() {
      var query = this.queryState = this.query = this.dashboard.getQuery(this.queryDefinition);
      query.setAjaxOptions({async: true});
      query.fetchData(
        this.parameters,
        this.getSuccessHandler(_.bind(this.onDataReady, this)),
        this.getErrorHandler());
    },

    _concludeUpdate: function() {
      // google mapEngine implementation will still fetch data asynchronously before ca
      // so only here can we finish the lifecycle.
      this.postExec();
      this.maybeToggleBlock(false);
    }
  };

});

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

define("cde/components/Map/Map.selector", [], function() {
  "use strict";
  return {

    /**
     * Gets the current selection state
     * @method getValue
     * @public
     * @return {Array} List of strings containing the IDs of the selected items,
     * in the same format as they would be written to the parameter
     */
    getValue: function() {
      var selectedItems = this.model.leafs()
        .filter(function(m) {
          return m.getSelection() === true;
        })
        .map(function(m) {
          return m.get("id");
        })
        .value();
      return selectedItems;
    },

    /**
     * Sets the selection state
     * @method setValue
     * @public
     * @param {Array} value List of strings containing the IDs of the selected items,
     * which will be written to the parameter
     * @chainable
     */
    setValue: function(idList) {
      if (this.model) {
        this.model.setSelectedItems(idList);
      } else {
        throw "Model is not initialized";
      }
      return this;
    },

    updateSelection: function() {
      // Mark selected model items
      var idList = this.dashboard.getParameterValue(this.parameter);
      this.setValue(idList);
    },

    /**
     * Implement's CDF logic for updating the state of the parameter, by
     * invoking Dashboards.processChange()
     * @method processChange
     * @public
     * @param {Array} value List of strings containing the IDs of the selected items,
     * in the same format as they would be written to the parameter
     */
    processChange: function() {
      //console.debug('processChange was called: ', (new Date()).toISOString());
      this.dashboard.processChange(this.name);
      return this;
    }
  };

});

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

define("cde/components/Map/model/MapModel", [
  "cdf/lib/BaseSelectionTree",
  "amd!cdf/lib/underscore",
  "cdf/lib/jquery"
], function(BaseSelectionTree, _, $) {
  "use strict";
  var MODES = {
    "pan": "pan",
    "zoombox": "zoombox",
    "selection": "selection"
  };
  var GLOBAL_STATES = {
    "allSelected": "allSelected",
    "someSelected": "someSelected",
    "noneSelected": "noneSelected",
    "disabled": "disabled"
  };
  var LEAF_STATES = {
    "selected": "selected",
    "unselected": "unselected"
  };
  var ACTIONS = {
    "normal": "normal",
    "hover": "hover"
  };
  var FEATURE_TYPES = {
    "shapes": "shape",
    "markers": "marker"
  };
  var SelectionStates = BaseSelectionTree.SelectionStates;

  return BaseSelectionTree.extend({
    defaults: {
      id: undefined,
      label: "",
      isSelected: false,
      isHighlighted: false,
      isVisible: true,
      numberOfSelectedItems: 0,
      numberOfItems: 0,
      rawData: null,
      styleMap: {}
    },

    constructor: function() {
      this.base.apply(this, arguments);
      if (this.isRoot()) {
        this.setPanningMode();
        this.set("canSelect", true);
      }
    },

    setSelection: function() {
      if (this.root().get("canSelect") === true) {
        this.base.apply(this, arguments);
      }
    },

    setPanningMode: function() {
      if (this.isSelectionMode()) {
        this.trigger("selection:complete");
      }
      this.root().set("mode", MODES.pan);
      return this;
    },

    setZoomBoxMode: function() {
      this.root().set("mode", MODES.zoombox);
      return this;
    },

    setSelectionMode: function() {
      this.root().set("mode", MODES.selection);
      return this;
    },

    getMode: function() {
      return this.root().get("mode");
    },

    isPanningMode: function() {
      return this.root().get("mode") === MODES.pan;
    },

    isZoomBoxMode: function() {
      return this.root().get("mode") === MODES.zoombox;
    },

    isSelectionMode: function() {
      return this.root().get("mode") === MODES.selection;
    },

    isHover: function() {
      return this.get("isHighlighted") === true;
    },

    setHover: function(bool) {
      return this.set("isHighlighted", bool === true);
    },

    /**
     * Computes the node"s style, using inheritance.
     *
     * Rules:
     *
     */
    _getStyle: function(mode, globalState, state, action, dragState) {
      var myStyleMap = this.get("styleMap");

      var parentStyle;
      if (this.parent()) {
        parentStyle = this.parent()._getStyle(mode, globalState, state, action, dragState);
      } else {
        parentStyle = {};
      }

      return $.extend(true,
        getStyle(parentStyle, mode, globalState, state, action, dragState),
        getStyle(myStyleMap, mode, globalState, state, action, dragState)
      );
    },

    getStyle: function() {
      var mode = this.root().get("mode");
      var canSelect = this.root().get("canSelect") === true;
      var globalState = getGlobalState(canSelect ? this.root().getSelection() : "disabled");
      var state = (this.getSelection() === SelectionStates.ALL) ? LEAF_STATES.selected : LEAF_STATES.unselected;
      var action = this.isHover() === true ? ACTIONS.hover : ACTIONS.normal;
      var dragState = this.root().get("isDragging") ? "dragging" : "moving"; //EXPERIMENTAL
      return this._getStyle(mode, globalState, state, action, dragState);
    },

    getFeatureType: function() {
      return FEATURE_TYPES[this._getParents([])[1]];
    },

    _getParents: function(list) {
      list.unshift(this.get("id"));

      if (this.parent()) {
        return this.parent()._getParents(list);
      } else {
        return list;
      }
    }

  }, {
    Modes: MODES,
    States: LEAF_STATES,
    Actions: ACTIONS,
    FeatureTypes: FEATURE_TYPES,
    SelectionStates: BaseSelectionTree.SelectionStates
  });

  function getGlobalState(selectionState) {
    switch (selectionState) {
      case SelectionStates.ALL:
        return GLOBAL_STATES.allSelected;
      case SelectionStates.SOME:
        return GLOBAL_STATES.someSelected;
      case SelectionStates.NONE:
        return GLOBAL_STATES.noneSelected;
      case "disabled":
        return GLOBAL_STATES.disabled;
    }
  }

  function getStyle(config, mode, globalState, leafState, action, dragState) {
    var styleKeywords = [
      //["dragging", "moving"], //EXPERIMENTAL
      _.values(ACTIONS),
      _.values(LEAF_STATES),
      _.values(MODES),
      _.values(GLOBAL_STATES)
    ];

    var desiredKeywords = _.map(styleKeywords, function(list, idx) {
      return _.intersection(list, [[/*dragState || '',*/ action || "", leafState || "", mode || "", globalState || ""][idx]])[0];
    });

    return computeStyle(config, desiredKeywords);
  }

  function computeStyle(config, desiredKeywords) {
    var plainStyle = {};
    var compoundStyle = {};
    _.each(config, function(value, key) {
      if (_.isObject(value)) {
        compoundStyle[key] = value;
      } else {
        plainStyle[key] = value;
      }
    });

    //console.log('desiredKeywords', desiredKeywords);
    //console.log('computing plain style ', plainStyle);

    var style = _.reduce(compoundStyle, function(memo, value, key) {
      _.each(desiredKeywords, function(keyword) {
        if (keyword === key) {
          //console.log('computing compound key=', key, ' value=', value);
          $.extend(true, memo, computeStyle(value, desiredKeywords));
        }
      });
      return memo;
    }, plainStyle);
    return style;
  }

});

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

define("cde/components/Map/_getMapping", [
  "amd!cdf/lib/underscore"
], function(_) {
  "use strict";
  return getMapping;

  function getMapping(json) {
    var map = {};

    if (!json.metadata || json.metadata.length === 0) {
      return map;
    }

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

    var colToPropertyMapping = { // colName -> property
      "key": "id",
      "id": "id",
      "fill": "fill",
      "fillColor": "fill",
      "r": "r",
      // previously defined mappings
      "latitude": "latitude",
      "longitude": "longitude",
      "address": "address",
      "description": "description",
      "marker": "marker", //iconUrl
      "markerwidth": "markerWidth",
      "markerheight": "markerHeight",
      "popupcontents": "popupContents",
      "popupwidth": "popupWidth",
      "popupheight": "popupHeight"
    };

    var colNames = _.chain(json.metadata)
      .pluck("colName")
      .map(function(s) {
        return s.toLowerCase();
      })
      .value();

    map = _.chain(colNames)
      .map(function(colName, idx) {
        var property = colToPropertyMapping[colName];
        if (property) {
          return [property, idx];
        } else {
          return [colName, idx]; //be permissive on the mapping
        }
      })
      .compact()
      .object()
      .value();

    if ("latitude" in map || "longitude" in map) {
      map.addressType = "coordinates";
    }
    if ("address" in map && !map.addressType) {
      map.addressType = "address";
    }

    if (!map.id) {
      map.id = 0; //TODO: evaluate if this sort of hardcoding is really necessary
    }

    return map;
  }
});

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

define("cde/components/Map/FeatureStore/shapeConversion", [], function() {
  "use strict";
  return {
    simplifyPoints: function(points, precision_m) {
      if (precision_m < 0) {
        return points;
      }
      function properRDP(points, epsilon) {
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
        var firstPoint = points[0];
        var lastPoint = points[points.length - 1];
        if (points.length < 3) {
          return points;
        }
        var index = -1;
        var dist = 0;
        for (var i = 1; i < points.length - 1; i++) {
          var cDist = findPerpendicularDistance(points[i], firstPoint, lastPoint);
          if (cDist > dist) {
            dist = cDist;
            index = i;
          }
        }
        if (dist > epsilon) {
          // iterate
          var l1 = points.slice(0, index + 1);
          var l2 = points.slice(index);
          var r1 = properRDP(l1, epsilon);
          var r2 = properRDP(l2, epsilon);
          // concat r2 to r1 minus the end/startpoint that will be the same
          var rs = r1.slice(0, r1.length - 1).concat(r2);
          return rs;
        } else {
          return [firstPoint, lastPoint];
        }
      }

      function findPerpendicularDistance(p, p1, p2) {
        // if start and end point are on the same x the distance is the difference in X.
        var result;
        var slope;
        var intercept;
        if (p1[0] == p2[0]) {
          result = Math.abs(p[0] - p1[0]);
        } else {
          slope = (p2[1] - p1[1]) / (p2[0] - p1[0]);
          intercept = p1[1] - (slope * p1[0]);
          result = Math.abs(slope * p[0] - p[1] + intercept) / Math.sqrt(Math.pow(slope, 2) + 1);
        }

        return result;
      }

      return properRDP(points, precision_m / 6.3e6);

    }, // reducePoints

    exportShapeDefinition: function() {
      if (this.shapeDefinition) {
        window.open("data:text/json;charset=utf-8," + escape(JSON.stringify(this.shapeDefinition)));
      }
    }
  };

});

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

define("cde/components/Map/FeatureStore/resolveShapes", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "./shapeConversion"
], function($, _, ShapeConversion) {
  "use strict";
  return resolveShapes;

  function resolveShapes(json, mapping, configuration) {
    var addIn = this.getAddIn("ShapeResolver", configuration.addIns.ShapeResolver.name);
    var url = configuration.addIns.ShapeResolver.options.url;
    if (!addIn && url) {
      if (url.endsWith("json") || url.endsWith("js")) {
        addIn = this.getAddIn("ShapeResolver", "simpleJSON");
      } else {
        addIn = this.getAddIn("ShapeResolver", "kml");
      }
    }
    var deferred = $.Deferred();
    if (!addIn) {
      deferred.resolve({});
      return deferred.promise();
    }

    var idList = _.pluck(json.resultset, mapping.id);
    var st = {
      keys: idList, //TODO Consider keys -> ids
      ids: idList,
      tableData: json,
      _simplifyPoints: ShapeConversion.simplifyPoints,
      _parseShapeKey: configuration.addIns.ShapeResolver.options.parseShapeKey,
      _shapeSource: url
    };
    var promise = addIn.call(this, st, this.getAddInOptions("ShapeResolver", addIn.getName()));
    $.when(promise).then(function(result) {
      var shapeDefinitions = _.chain(result)
        .map(function(geoJSONFeature, key) {
          return [key, geoJSONFeature];
        })
        .object()
        .value();
      deferred.resolve(shapeDefinitions);
    });
    return deferred.promise();
  }

});

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

define("cde/components/Map/FeatureStore/resolveMarkers", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function($, _) {
  "use strict";
  return resolveMarkers;

  function resolveMarkers(json, mapping, configuration) {
    var addIn = this.getAddIn("LocationResolver", configuration.addIns.LocationResolver.name);

    var deferred = $.Deferred();
    if (!addIn) {
      deferred.resolve({});
      return deferred.promise();
    }

    var tgt = this;
    var opts = this.getAddInOptions("LocationResolver", addIn.getName());
    var markerDefinitions;
    if (mapping.addressType === "coordinates") {
      markerDefinitions = _.chain(json.resultset)
        .map(function(row) {
          var id = row[mapping.id];
          var location = [row[mapping.longitude], row[mapping.latitude]];
          return [id, createFeatureFromLocation(location)];
        })
        .object()
        .value();

    } else {
      markerDefinitions = _.chain(json.resultset)
        .map(function(row, rowIdx) {
          var promisedLocation = $.Deferred();
          var id = row[mapping.id];
          var address = mapping.address != undefined ? row[mapping.address] : undefined;
          var st = {
            data: row,
            position: rowIdx,
            address: address,
            addressType: mapping.addressType,

            key: id, //TODO: deprecate 'key' in favour of 'id'
            id: id,
            mapping: mapping,
            tableData: json,
            continuationFunction: function(location) {
              promisedLocation.resolve(createFeatureFromLocation(location));
            }
          };
          var props = ["country", "city", "county", "region", "state"];
          _.each(_.pick(mapping, props), function(propIdx, prop) {
            if (propIdx != undefined) {
              st[prop] = row[propIdx];
            }
          });
          try {
            addIn.call(tgt, st, opts);
          } catch (e) {
            promisedLocation.resolve(null);
          }
          return [id, promisedLocation.promise()];
        })
        .object()
        .value();
    }

    deferred.resolve(markerDefinitions);
    return deferred.promise();
  }

  function createFeatureFromLocation(location) {
    var longitude = location[0];
    var latitude = location[1];
    var feature = {
      geometry: {
        coordinates: [longitude, latitude],
        type: "Point",
        properties: {
          latitude: latitude,
          longitude: longitude
        }
      },
      type: "Feature"
    };
    return feature;
  }

});

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

define("cde/components/Map/Map.model", [
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

define("cde/components/Map/Map.configuration", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function($, _) {
  "use strict";
  return {
    getConfiguration: getConfiguration
  };

  /**
   * Validates the configuration options and gathers them by context
   *
   * @returns {{addIns: {MarkerImage: *, ShapeResolver: *, LocationResolver: *}}}
   */
  function getConfiguration() {
    var addIns = {
      MarkerImage: {
        name: this.markerCggGraph ? "cggMarker" : this.markerImageGetter,
        options: {
          cggScript: this.markerCggGraph,
          parameters: this.cggGraphParameters,
          height: this.markerHeight,
          width: this.markerWidth,
          iconUrl: this.marker
        }
      },
      ShapeResolver: {
        name: this.shapeResolver,
        options: {
          url: this.shapeSource,
          parseShapeKey: this.parseShapeKey
        }
      },
      LocationResolver: {
        name: this.locationResolver || "openstreetmap",
        options: {}
      },
      MapEngine: {
        name: this.mapEngineType,
        options: {}
      }
    };

    var tiles = {
      services: this.tileServices,
      tilesets: (_.isString(this.tilesets)) ? [this.tilesets] : this.tilesets
    };

    //
    var controls = {
      doubleClickTimeoutMilliseconds: 300,
      enableKeyboardNavigation: true,
      enableZoomOnMouseWheel: false
    };

    var viewport = {
      extent: {
        southEast: {
          latitude: -72.7, //clip Antartica
          longitude: -180
        },
        northWest: {
          latitude: 84.2, //clip North Pole
          longitude: 180
        }
      },
      center: {
        latitude: parseFloat(this.centerLatitude),
        longitude: parseFloat(this.centerLongitude)
      },
      zoomLevel: {
        min: 0,
        max: Infinity,
        "default": this.defaultZoomLevel
      }

    };

    var configuration = $.extend(true, {}, {
      API_KEY: this.API_KEY || window.API_KEY,
      tiles: tiles,
      isSelector: !_.isEmpty(this.parameter),
      addIns: addIns,
      controls: controls,
      styleMap: this.styleMap,
      viewport: viewport
    });
    if (!_.isUndefined(this.options)) {
      configuration = $.extend(true, configuration, _.isFunction(this.options) ? this.options(configuration) : this.options);
    }
    return configuration;
  }

});

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

define("cde/components/Map/Map.ext", [], function() {

  return {
    getMarkerImgPath: function() {
      return CONTEXT_PATH + "api/repos/pentaho-cdf-dd/resources/custom/amd-components/Map/images/";
    }
  };

});

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

define("cde/components/Map/Map.featureStyles", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "./Map.ext",
  "cdf/Logger"
], function($, _, MapExt, Logger) {
  "use strict";

  var styleMaps = {
    global: {
      cursor: "inherit",
      "stroke-width": 1,
      stroke: "white",
      hover: {
        stroke: "black"
      },
      unselected: {
        "fill-opacity": 0.2
      },
      selected: {
        "fill-opacity": 0.8
      },
      disabled: {
        unselected: {
          "fill-opacity": 0.8
        }
      },
      noneSelected: {
        unselected: {
          "fill-opacity": 0.8
        }
      },
      allSelected: {
        selected: {
          "fill-opacity": 0.8
        }
      }
    },
    markers: {
      r: 10,
      symbol: "circle",
      //label: "Normal",
      labelAlign: "cm",
      labelYOffset: -20,
      fill: "red",
      "stroke-width": 2
    },
    shapes: {
      "stroke-width": 1.5,
      normal: {
        "z-index": 0
      },
      hover: {
        "z-index": 1
      }
    }
  };

  return {
    getStyleMap: getStyleMap
  };

  function getStyleMap(styleName) {
    var styleMap = $.extend(true, {},
      styleMaps.global,
      styleMaps[styleName]
    );

    // TODO: Remove shapeSettings definition/property in the next major version.
    switch (styleName) {
      case "shapes":
        Logger.warn("Usage of the 'shapeSettings' property (including shapeSettings.fillOpacity, shapeSettings.strokeWidth and shapeSettings.strokeColor) is deprecated.");
        Logger.warn("Support for these properties will be removed in the next major version.");
        $.extend(true, styleMap, this.shapeSettings);
        break;
    }

    var localStyleMap = _.result(this, "styleMap") || {};
    return $.extend(true, styleMap,
      localStyleMap.global,
      localStyleMap[styleName]
    );
  }

  /**
   * Builds a string associated with the CSS property cursor: e.g.
   * cursor(["image1.svg", "image1.png"], "default")
   * ->
   * "url(./image1.svg), url(./image1.png), default"
   * @param image
   * @param fallback
   * @returns {*}
   */
  function cursor(image, fallback) {
    var list = _.isString(image) ? [image] : image;
    return _.map(list, function(img) {
        return "url(" + MapExt.getMarkerImgPath() + img + ")";
      }).join(", ") + fallback;
  }

});

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

define("cde/components/Map/Map.colorMap", [
  "amd!cdf/lib/underscore"
], function(_) {
  "use strict";
  return {
    /** Mixin for handling color maps
     This should probably be elevated to a proper class with a nice database of colormaps
     or be replaced with a proper color handling library
     */
    colormaps: {
      "default": ["#79be77", "#96b761", "#b6ae4c", "#e0a433", "#f4a029", "#fa8e1f", "#f47719", "#ec5f13", "#e4450f", "#dc300a"],
      "default0": [[0, 102, 0, 1], [255, 255, 0, 1], [255, 0, 0, 1]], //RGBA
      "jet": [],
      "gray": [[0, 0, 0, 255], [255, 255, 255, 1]],
      "french-flag": [[255, 0, 0, 1], [255, 254, 255, 1], [0, 0, 255, 1]]
    },
    getColorMap: function() {

      var colorMap = [];
      if (this.colormap == null || (_.isArray(this.colormap) && !this.colormap.length)) {
        colorMap = _.clone(this.colormaps["default"]);
      } else {
        colorMap = _.map(this.colormap, JSON.parse);
      }
      colorMap = _.map(colorMap, color2array);

      /*
       _.each(colorMap, function (c, idx) {
       if (_.isString(c) && c.startsWith("#")) {
       colorMap[idx] = [parseInt(c.substring(1, 3), 16), parseInt(c.substring(3, 5), 16), parseInt(c.substring(5, 7), 16), 1];
       }
       });
       */
      var cmap = [];
      for (var k = 1, L = colorMap.length; k < L; k++) {
        cmap = cmap.concat(interpolate(colorMap[k - 1], colorMap[k], 32));
      }
      return _.map(cmap, function(v) {
        return "rgba(" + v.join(",") + ")";
      });
    },
    mapColor: function(value, minValue, maxValue, colormap) {
      var n = colormap.length;
      var level = (value - minValue) / (maxValue - minValue);
      return colormap[Math.floor(level * (n - 1))];
    },
    toGrayscale: function(color) {
      var rgba = color2array(color);
      var g = Math.round(Math.sqrt(0.2989 * rgba[0] * rgba[0] + 0.5870 * rgba[1] * rgba[1] + 0.1140 * rgba[2] * rgba[2]));
      var v = [g, g, g, rgba[3]];
      return "rgba(" + v.join(",") + ")";
    }
  };

  function color2array(color) {
    var rgba = _.clone(color);
    if (_.isArray(color)) {
      rgba = color;
      if (rgba.length === 3) {
        rgba.push(1);
      }
    } else if (_.isString(color)) {
      if (color[0] === "#") {
        rgba = [parseInt(color.substring(1, 3), 16), parseInt(color.substring(3, 5), 16), parseInt(color.substring(5, 7), 16), 1];
      } else if (color.substring(0, 4) === "rgba") {
        rgba = color.slice(5, -1).split(",").map(parseFloat); // assume rgba(R,G,B,A) format

      }
    }
    return rgba;
  }

  function interpolate(a, b, n) {
    var colormap = [], d = [];
    var k, kk, step;
    for (k = 0; k < a.length; k++) {
      colormap[k] = [];
      for (kk = 0, step = (b[k] - a[k]) / n; kk < n; kk++) {
        colormap[k][kk] = a[k] + kk * step;
      }
    }
    for (k = 0; k < colormap[0].length && k < 3; k++) {
      d[k] = [];
      for (kk = 0; kk < colormap.length; kk++) {
        d[k][kk] = Math.round(colormap[kk][k]);
      }
    }
    return d;
  }

});

define("text!cde/components/Map/ControlPanel/ControlPanel.html", [], function() {
  return '<div class="map-control-panel {{mode}}">\n    <div class="map-controls-zoom">\n        <div class="map-control-button map-control-zoom-in"></div>\n        <div class="map-control-button map-control-zoom-out"></div>\n        <div class="map-control-button map-control-zoombox"></div>\n    </div>\n    <div class="map-controls-mode">\n        {{#configuration.isSelector}}\n        <div class="map-control-button map-control-select"></div>\n        {{/configuration.isSelector}}\n        <div class="map-control-button map-control-pan"></div>\n    </div>\n</div>';
});

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

define("cde/components/Map/ControlPanel/ControlPanel", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "cdf/lib/mustache",
  "cdf/lib/BaseEvents",
  "../model/MapModel",
  "text!./ControlPanel.html",
  "css!./ControlPanel"
], function($, _, Mustache, BaseEvents, MapModel, template) {
  "use strict";
  return BaseEvents.extend({
    constructor: function(domNode, model, configuration) {
      this.base();
      this.ph = $(domNode);
      this.model = model;
      this.configuration = configuration;
      return this;
    },

    render: function() {
      var viewModel = {
        mode: this.model.getMode(),
        configuration: this.configuration
      };
      var html = Mustache.render(template, viewModel);
      this.ph.empty().append(html);
      this._bindEvents();

      return this;
    },

    zoomOut: function() {
      this.trigger("zoom:out");
      return this;
    },
    zoomIn: function() {
      this.trigger("zoom:in");
      return this;
    },

    setPanningMode: function() {
      this.model.setPanningMode();
      return this;
    },

    setZoomBoxMode: function() {
      this.model.setZoomBoxMode();
      return this;
    },

    setSelectionMode: function() {
      this.model.setSelectionMode();
      return this;
    },

    _bindEvents: function() {
      var bindings = {
        ".map-control-zoom-out": this.zoomOut,
        ".map-control-zoom-in": this.zoomIn,
        ".map-control-pan": this.setPanningMode,
        ".map-control-zoombox": this.setZoomBoxMode,
        ".map-control-select": this.setSelectionMode
      };

      var me = this;
      _.each(bindings, function(callback, selector) {
        me.ph.find(selector).click(_.bind(callback, me));
      });
      this.listenTo(this.model, "change:mode", _.bind(this._updateView, this));
    },

    _updateView: function() {
      var mode = this.model.getMode();
      this.ph.find(".map-control-panel")
        .removeClass(_.values(MapModel.Modes).join(" "))
        .addClass(mode);
    }
  });

});

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

define("cde/components/Map/Map.tileServices", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function($, _) {
  "use strict";
  /**
   * TileServices (servers providing png images representing the map)
   OpenStreetMaps default tiles are ugly, I found many nicer tilesets that work in both map engines (google/openlayers)
   To compare the various tilesets, visit http://mc.bbbike.org/mc/?num=2

   Example of valid values for the CDE property "tilesets"
   "mapquest"
   ["mapquest"]
   ["mapquest", "stamen"]
   "custom/static/localMapService/${z}/${x}/${y}.png"
   "http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png"
   */

  function mapObj(obj, callback) {
    return _.object(_.map(obj, function(value, key) {
      return [key, callback(value, key)];
    }));
  }

  var tileServices = $.extend({
      // list of tileset services that were tested and are working at 2013-11-04, see http://mc.bbbike.org/mc/?num=2 for comparison
      "google": {}, //"http://mt{switch:0,1,2,3}.googleapis.com/vt?x=${x}&y=${y}&z=${z}"
      "google-roadmap": {},
      "google-terrain": {},
      "google-satellite": {},
      "google-hybrid": {}
    },

    /*
     mapObj({
     "thunderforest-landscape": "https://${switch:a,b,c}.tile.thunderforest.com/landscape/${z}/${x}/${y}.png",
     "thunderforest-outdoors": "https://${switch:a,b,c}.tile.thunderforest.com/outdoors/${z}/${x}/${y}.png"
     }, function(url, key) {
     return {
     id: key,
     url: url,
     attribution: 'Map tiles by &copy;<a href="http://www.thunderforest.com">Thunderforest</a>, Data by &copy;<a href="http://openstreetmap.org/copyright">OpenStreetMap</a> Contributors',
     legaInfo: ["http://www.thunderforest.com/terms/"]
     };
     }),
     */

    mapObj({
      "openstreetmaps": "http://{switch:a,b,c}.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "openstreemaps-bw": "http://{switch:a,b}.tiles.wmflabs.org/bw-mapnik/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreeMap</a> contributors',
        legaInfo: ["http://www.openstreetmap.org/copyright"]
      };
    }),

    mapObj({
      "nokia-normal": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/${z}/${x}/${y}/256/png8",
      "nokia-normal-grey": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.grey/${z}/${x}/${y}/256/png8",
      "nokia-normal-transit": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.transit/${z}/${x}/${y}/256/png8",
      "nokia-satellite": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/satellite.day/${z}/${x}/${y}/256/png8",
      "nokia-terrain": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/terrain.day/${z}/${x}/${y}/256/png8"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="https://www.here.com/">HERE</a>',
        legalInfo: ["https://legal.here.com/en/terms/serviceterms/us/"]
      };
    }),

    mapObj({
      "mapquest": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-normal": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-hybrid": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/hyb/${z}/${x}/${y}.png", //MapQuest tile server
      "mapquest-sat": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg", //MapQuest tile server
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
        legalInfo: ["http://maps.stamen.com/"]
      };
    }),

    mapObj({
      "stamen": "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
      "stamen-terrain": "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
      "stamen-terrain-background": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-background/${z}/${x}/${y}.jpg",
      "stamen-terrain-labels": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-labels/${z}/${x}/${y}.jpg",
      "stamen-toner": "http://{switch:a,b,c,d}.tile.stamen.com/toner/${z}/${x}/${y}.png",
      "stamen-toner-lite": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lite/${z}/${x}/${y}.png",
      "stamen-toner-background": "http://{switch:a,b,c,d}.tile.stamen.com/toner-background/${z}/${x}/${y}.png",
      "stamen-toner-hybrid": "http://{switch:a,b,c,d}.tile.stamen.com/toner-hybrid/${z}/${x}/${y}.png",
      "stamen-toner-labels": "http://{switch:a,b,c,d}.tile.stamen.com/toner-labels/${z}/${x}/${y}.png",
      "stamen-toner-lines": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lines/${z}/${x}/${y}.png",
      "stamen-toner-2010": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2010/${z}/${x}/${y}.png",
      "stamen-toner-2011": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2011/${z}/${x}/${y}.png",
      "stamen-watercolor": "http://{switch:a,b,c,d}.tile.stamen.com/watercolor/${z}/${x}/${y}.jpg"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
        legalInfo: ["http://maps.stamen.com/"]
      };
    }),

    mapObj({
      // "mapbox-terrain": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-9ijuk24y/${z}/${x}/${y}.jpg",
      "mapbox-satellite": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-qfyrx5r8/${z}/${x}/${y}.png",

      "mapbox-control-room": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.control-room/${z}/${x}/${y}.png",

      "mapbox-blue-marble-jan": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jan/${z}/${x}/${y}.png",
      "mapbox-blue-marble-jul": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jul/${z}/${x}/${y}.png",
      "mapbox-blue-marble-jul-bw": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.blue-marble-topo-jul-bw/${z}/${x}/${y}.png",

      "mapbox-natural-earth-1": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-1/${z}/${x}/${y}.png",
      "mapbox-natural-earth-2": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-2/${z}/${x}/${y}.png",
      "mapbox-natural-earth-hypso": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-hypso/${z}/${x}/${y}.png",
      "mapbox-natural-earth-hypso-bathy": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.natural-earth-hypso-bathy/${z}/${x}/${y}.png",

      "mapbox-oceans-white": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.oceans-white/${z}/${x}/${y}.png",
      "mapbox-world-black": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-black/${z}/${x}/${y}.png",
      "mapbox-world-blank-bright": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blank-bright/${z}/${x}/${y}.png",
      "mapbox-world-blank-light": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blank-light/${z}/${x}/${y}.png",
      "mapbox-world-blue": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-blue/${z}/${x}/${y}.png",
      "mapbox-world-bright": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-bright/${z}/${x}/${y}.png",
      "mapbox-world-dark": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-dark/${z}/${x}/${y}.png",
      "mapbox-world-glass": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-glass/${z}/${x}/${y}.png",
      "mapbox-world-light": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-light/${z}/${x}/${y}.png",
      "mapbox-world-print": "https://{switch:a,b}.tiles.mapbox.com/v3/mapbox.world-print/${z}/${x}/${y}.png"
      //"mapbox-world-light4": "http://api.mapbox.com/v4/mapbox.light/${z}/${x}/${y}.png",
      //"mapbox-example": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.c7d2024a/${z}/${x}/${y}.png",
      //"mapbox-example2": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.bc17bb2a/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://mapbox.com/about/maps/">MapBox</a> &mdash; Data by &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        legalInfo: ["http://mapbox.com/about/maps/", "http://www.openstreetmap.org/copyright"]
      };
    }),

    mapObj({
      "openmapsurfer": "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-roads": "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-roads-grayscale": "http://129.206.74.245:8008/tms_rg.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-semitransparent": "http://129.206.74.245:8003/tms_h.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-hillshade": "http://129.206.74.245:8004/tms_hs.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-contour": "http://129.206.74.245:8006/tms_il.ashx?x=${x}&y=${y}&z=${z}",
      "openmapsurfer-administrative": "http://129.206.74.245:8007/tms_b.ashx?x=${x}&y=${y}&z=${z}"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://korona.geog.uni-heidelberg.de/contact.html">OpenMapSurfer</a>',
        legalInfo: ["http://korona.geog.uni-heidelberg.de/contact.html"]
      };
    }),

    mapObj({
      "cartodb-positron": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/light_all/${z}/${x}/${y}.png",
      "cartodb-positron-nolabels": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/light_nolabels/${z}/${x}/${y}.png",
      "cartodb-darkmatter": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/dark_all/${z}/${x}/${y}.png",
      "cartodb-darkmatter-nolabels": "http://{switch:a,b,c,d}.basemaps.cartocdn.com/dark_nolabels/${z}/${x}/${y}.png"
    }, function(url, key) {
      return {
        id: key,
        url: url,
        attribution: 'Map tiles by <a href="http://cartodb.com/attributions#basemaps">CartoDB</a>, under <a href="https://creativecommons.org/licenses/by/3.0/" target="_blank">CC BY 3.0</a>. Data by <a href="http://www.openstreetmap.org/" target="_blank">OpenStreetMap</a>, under ODbL.',
        legalInfo: ["https://cartodb.com/basemaps/"]
      };
    }),

    mapObj({
        "esri-street": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer">Esri</a>'
        },
        "esri-ocean-basemap": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Ocean_Basemap/MapServer">Esri</a>'
        },
        "esri-natgeo": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer">Esri</a>'
        },
        "esri-world": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer">Esri</a>'
        },
        "esri-lightgray": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer">Esri</a>'
        },
        "esri-delorme": {
          url: "http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/${z}/${y}/${x}",
          attribution: 'Map tiles by &copy; <a href="http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer">Esri</a>'
        }
      }, function(obj, key) {
        return $.extend(obj, {
          id: key,
          legalInfo: ["http://www.esri.com/legal/software-license", "http://downloads2.esri.com/ArcGISOnline/docs/tou_summary.pdf"]
        });
      }
    )
  );

  tileServices["default"] = tileServices["cartodb-positron-nolabels"];
  return {
    tileServices: tileServices,
    otherTileServices: [
      // These are tilesets using special code
      //"google"
    ],
    tileServicesOptions: {
      // WIP: interface for overriding defaults
      "apple": {minZoom: 3, maxZoom: 14}
    }
  };

});

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

define("cde/components/Map/engines/MapEngine", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "cdf/lib/BaseEvents",
  "../model/MapModel"
], function($, _, BaseEvents, MapModel) {
  "use strict";
  return BaseEvents.extend({
    tileServices: undefined,
    tileServicesOptions: undefined,
    $map: null,
    tileLayer: function(name) {
    },
    init: function() {
      var deferred = $.Deferred();
      deferred.resolve();
      return deferred.promise();
    },
    renderMap: function(target) {
    },
    render: function(model) {
      this.model = model;
      var me = this;

      //this.listenTo(this.model.root(), 'change:isDragging', function (model, value) {
      //  model.leafs()
      //    .filter(function(m) {
      //      // Improve the perfomance by reducing the number of updated items down to 1.
      //      return m.isHover();
      //    })
      //    .each(function (m) {
      //      me.updateItem(m);
      //    })
      //});

      this.listenTo(this.model.root(), "change:mode", function(model, value) {
        var modes = {
          "selection": me.setSelectionMode,
          "zoombox": me.setZoomBoxMode,
          "pan": me.setPanningMode
        };
        modes[value] && modes[value].call(me);
        model.leafs().each(function(m) {
          me.updateItem(m);
        });
      });

      this.listenTo(this.model, "change:isSelected change:isHighlighted change:isVisible", function(model, value) {
        if (model.parent() === model.root()) {
          // children of root ("markers" and "shapes") are virtual bags of items
          // don't react to their events.
          return;
        }
        model.leafs().each(function(m) {
          //console.log('updating item ', m.get('id'), 'in reaction to', model.get('id'));
          me.updateItem(m);
        });
      });

      model.leafs().each(function(m) {
        me.renderItem(m);
      });
      if (model.isPanningMode()) {
        me.setPanningMode();
      }
      if (model.isZoomBoxMode()) {
        me.setZoomBoxMode();
      }
      if (model.isSelectionMode()) {
        me.setSelectionMode();
      }

    },
    updateViewport: function(centerLongitude, centerLatitude, zoomLevel) {
    },
    showPopup: function() {
    },
    _wrapEvent: function(modelItem) {
      return {
        model: modelItem,
        data: $.extend(true, {}, modelItem.get("data"), modelItem.get("rawData")), //TODO review this: data, or rawData?
        id: modelItem.get("id"),
        featureType: modelItem.getFeatureType(),
        style: modelItem.getStyle(),
        isSelected: function() {
          return modelItem.getSelection() === MapModel.SelectionStates.ALL;
        }
      };
    },
    toNativeStyle: function(foreignStyle) {
      var validStyle = {};
      _.each(foreignStyle, function(value, key) {
        switch (key) {
          case "visible":
          case "zIndex":
          case "fillColor":
          case "fillOpacity":
          case "strokeColor":
          case "strokeOpacity":
          case "strokeWidth":
        }
      });
      return validStyle;
    },
    wrapEvent: function(event, featureType) {
      return {
        latitude: undefined,
        longitude: undefined,
        data: undefined,
        feature: undefined,
        featureType: featureType,
        style: undefined, //feature-specific styling
        mapEngineType: "abstract",
        draw: function(style) {
        },
        //isSelected: undefined, // not ready for inclusion yet
        raw: undefined
      };
    },
    _updateMode: function(mode) {
      this.$map.removeClass(_.values(MapModel.Modes).join(" "))
        .addClass(MapModel.Modes[mode]);
    },
    _updateDrag: function(isDragging) {
      this.model.set("isDragging", !!isDragging);
      this.$map
        .toggleClass("dragging", !!isDragging)
        .toggleClass("moving", !isDragging);
    },
    _selectUrl: function(paramString, urls) {
      /**
       * Method: selectUrl
       * selectUrl() implements the standard floating-point multiplicative
       *     hash function described by Knuth, and hashes the contents of the
       *     given param string into a float between 0 and 1. This float is then
       *     scaled to the size of the provided urls array, and used to select
       *     a URL.
       *
       * Parameters:
       * paramString - {String}
       * urls - {Array(String)}
       *
       * Returns:
       * {String} An entry from the urls array, deterministically selected based
       *          on the paramString.
       */
      var product = 1;
      /**
       * Constant: URL_HASH_FACTOR
       * {Float} Used to hash URL param strings for multi-WMS server selection.
       *         Set to the Golden Ratio per Knuth's recommendation.
       */
      var URL_HASH_FACTOR = (Math.sqrt(5) - 1) / 2;
      for (var i = 0, len = paramString.length; i < len; i++) {

        product *= paramString.charCodeAt(i) * URL_HASH_FACTOR;
        product -= Math.floor(product);
      }
      return urls[Math.floor(product * urls.length)];
    },
    _switchUrl: function(url) {
      /*
       * support multiple servers in URL config
       * http://{switch:a,b}.tile.bbbike.org -> ["http://a.tile.bbbike.org", "http://a.tile.bbbike.org" ]
       */
      var list = url.match(/(http[s]?:\/\/[0-9a-z.]*?)\{switch:([a-z0-9,]+)\}(.*)/); // /(http:\/\/[0-9a-z]*?)\{switch:([a-z0-9,]+)\}(.*)/);

      if (!list || list.length == 0) {
        return url;
      }
      var servers = list[2].split(",");
      //var url_list = [];
      //for (var i = 0; i < servers.length; i++) {
      //  url_list.push(list[1] + servers[i] + list[3]);
      //}
      var url_list = _.map(servers, function(server) {
        return list[1] + server + list[3];
      });
      return url_list;
    },
    _getTileServiceURL: function(name) {
      var tileService = this.options.tiles.services[name];
      var urlTemplate = _.isObject(tileService) ? tileService.url : tileService;
      if (!urlTemplate) {
        // Allow the specification of an url from CDE
        if ((name.length > 0) && (name.indexOf("{") > -1)) {
          urlTemplate = name;
          //name = 'custom';
        }
      }
      return urlTemplate;
    },
    _getTileServiceAttribution: function(name) {
      var tileService = this.options.tiles.services[name];
      return _.isObject(tileService) ? tileService.attribution : "";
    },
    _getTileServiceOptions: function(name) {
      var tileService = this.options.tiles.services[name];
      var opts = _.isObject(tileService) ? tileService.options : {};
      return opts || {};
    },
    _createClickHandler: function(singleClick, doubleClick, timeout) {
      var me = this;
      var clicks = 0;
      return function() {
        clicks++;
        var self = this;
        var args = _.map(arguments, _.identity);
        args.unshift(me);
        if (clicks === 1) {
          setTimeout(function() {
            if (clicks === 1) {
              _.isFunction(singleClick) && singleClick.apply(self, args);
            } else {
              _.isFunction(doubleClick) && doubleClick.apply(self, args);
            }
            clicks = 0;
          }, timeout || me.options.controls.doubleClickTimeoutMilliseconds || 300);
        }
      };
    }

  });

});

/**
 * This file is a patch to OpenLayers 2.13.1
 * See https://github.com/ahocevar/ol2/blob/482e61aad4ba758897524f607f7986ece1655327/lib/OpenLayers/Layer/Google/v3.js
 */

define("cde/components/Map/engines/openlayers2/OpenLayers_patchLayerGooglev3", ["cdf/lib/OpenLayers"], function(OpenLayers) {

  /* Copyright (c) 2006-2013 by OpenLayers Contributors (see authors.txt for
   * full list of contributors). Published under the 2-clause BSD license.
   * See license.txt in the OpenLayers distribution or repository for the
   * full text of the license. */

  /**
   * @requires OpenLayers/Layer/Google.js
   */

  /**
   * Constant: OpenLayers.Layer.Google.v3
   *
   * Mixin providing functionality specific to the Google Maps API v3.
   *
   * To use this layer, you must include the GMaps v3 API in your html. To match
   * Google's zoom animation better with OpenLayers animated zooming, configure
   * your map with a zoomDuration of 10:
   *
   * (code)
   * new OpenLayers.Map('map', {zoomDuration: 10});
   * (end)
   *
   * Note that this layer configures the google.maps.map object with the
   * "disableDefaultUI" option set to true. Using UI controls that the Google
   * Maps API provides is not supported by the OpenLayers API.
   */
  OpenLayers.Layer.Google.v3 = {

    /**
     * Constant: DEFAULTS
     * {Object} It is not recommended to change the properties set here. Note
     * that Google.v3 layers only work when sphericalMercator is set to true.
     *
     * (code)
     * {
       *     sphericalMercator: true,
       *     projection: "EPSG:900913"
       * }
     * (end)
     */
    DEFAULTS: {
      sphericalMercator: true,
      projection: "EPSG:900913"
    },

    /**
     * APIProperty: animationEnabled
     * {Boolean} If set to true, the transition between zoom levels will be
     *     animated (if supported by the GMaps API for the device used). Set to
     *     false to match the zooming experience of other layer types. Default
     *     is true. Note that the GMaps API does not give us control over zoom
     *     animation, so if set to false, when zooming, this will make the
     *     layer temporarily invisible, wait until GMaps reports the map being
     *     idle, and make it visible again. The result will be a blank layer
     *     for a few moments while zooming.
     */
    animationEnabled: true,

    /**
     * Method: loadMapObject
     * Load the GMap and register appropriate event listeners.
     */
    loadMapObject: function() {
      if (!this.type) {
        this.type = google.maps.MapTypeId.ROADMAP;
      }
      var mapObject;
      var cache = OpenLayers.Layer.Google.cache[this.map.id];
      if (cache) {
        // there are already Google layers added to this map
        mapObject = cache.mapObject;
        // increment the layer count
        ++cache.count;
      } else {
        // this is the first Google layer for this map
        // create GMap
        var center = this.map.getCenter();
        var container = document.createElement("div");
        container.className = "olForeignContainer";
        container.style.width = "100%";
        container.style.height = "100%";
        mapObject = new google.maps.Map(container, {
          center: center ?
            new google.maps.LatLng(center.lat, center.lon) :
            new google.maps.LatLng(0, 0),
          zoom: this.map.getZoom() || 0,
          mapTypeId: this.type,
          disableDefaultUI: true,
          keyboardShortcuts: false,
          draggable: false,
          disableDoubleClickZoom: true,
          scrollwheel: false,
          streetViewControl: false,
          tilt: (this.useTiltImages ? 45 : 0)
        });
        var googleControl = document.createElement("div");
        googleControl.style.width = "100%";
        googleControl.style.height = "100%";
        mapObject.controls[google.maps.ControlPosition.TOP_LEFT].push(googleControl);

        // cache elements for use by any other google layers added to
        // this same map
        cache = {
          googleControl: googleControl,
          mapObject: mapObject,
          count: 1
        };
        OpenLayers.Layer.Google.cache[this.map.id] = cache;
      }
      this.mapObject = mapObject;
      this.setGMapVisibility(this.visibility);
    },

    /**
     * APIMethod: onMapResize
     */
    onMapResize: function() {
      if (this.visibility) {
        google.maps.event.trigger(this.mapObject, "resize");
      }
    },

    /**
     * Method: setGMapVisibility
     * Display the GMap container and associated elements.
     *
     * Parameters:
     * visible - {Boolean} Display the GMap elements.
     */
    setGMapVisibility: function(visible) {
      var cache = OpenLayers.Layer.Google.cache[this.map.id];
      var map = this.map;
      if (cache) {
        var type = this.type;
        var layers = map.layers;
        var layer;
        for (var i = layers.length - 1; i >= 0; --i) {
          layer = layers[i];
          if (layer instanceof OpenLayers.Layer.Google &&
            layer.visibility === true && layer.inRange === true) {
            type = layer.type;
            visible = true;
            break;
          }
        }
        var container = this.mapObject.getDiv();
        if (visible === true) {
          if (container.parentNode !== map.div) {
            if (!cache.rendered) {
              container.style.visibility = "hidden";
              var me = this;
              google.maps.event.addListenerOnce(this.mapObject, "tilesloaded", function() {
                cache.rendered = true;
                container.style.visibility = "";
                me.setGMapVisibility(true);
                me.moveTo(me.map.getCenter());
                cache.googleControl.appendChild(map.viewPortDiv);
                me.setGMapVisibility(me.visible);
              });
            } else {
              cache.googleControl.appendChild(map.viewPortDiv);
            }
            map.div.appendChild(container);
            google.maps.event.trigger(this.mapObject, "resize");
          }
          this.mapObject.setMapTypeId(type);
        } else if (cache.googleControl.hasChildNodes()) {
          map.div.appendChild(map.viewPortDiv);
          map.div.removeChild(container);
        }
      }
    },

    /**
     * Method: getMapContainer
     *
     * Returns:
     * {DOMElement} the GMap container's div
     */
    getMapContainer: function() {
      return this.mapObject.getDiv();
    },

    //
    // TRANSLATION: MapObject Bounds <-> OpenLayers.Bounds
    //

    /**
     * APIMethod: getMapObjectBoundsFromOLBounds
     *
     * Parameters:
     * olBounds - {<OpenLayers.Bounds>}
     *
     * Returns:
     * {Object} A MapObject Bounds, translated from olBounds
     *          Returns null if null value is passed in
     */
    getMapObjectBoundsFromOLBounds: function(olBounds) {
      var moBounds = null;
      if (olBounds != null) {
        var sw = this.sphericalMercator ?
          this.inverseMercator(olBounds.bottom, olBounds.left) :
          new OpenLayers.LonLat(olBounds.bottom, olBounds.left);
        var ne = this.sphericalMercator ?
          this.inverseMercator(olBounds.top, olBounds.right) :
          new OpenLayers.LonLat(olBounds.top, olBounds.right);
        moBounds = new google.maps.LatLngBounds(
          new google.maps.LatLng(sw.lat, sw.lon),
          new google.maps.LatLng(ne.lat, ne.lon)
        );
      }
      return moBounds;
    },

    /************************************
     *                                  *
     *   MapObject Interface Controls   *
     *                                  *
     ************************************/

    // LonLat - Pixel Translation

    /**
     * APIMethod: getMapObjectLonLatFromMapObjectPixel
     *
     * Parameters:
     * moPixel - {Object} MapObject Pixel format
     *
     * Returns:
     * {Object} MapObject LonLat translated from MapObject Pixel
     */
    getMapObjectLonLatFromMapObjectPixel: function(moPixel) {
      var size = this.map.getSize();
      var lon = this.getLongitudeFromMapObjectLonLat(this.mapObject.center);
      var lat = this.getLatitudeFromMapObjectLonLat(this.mapObject.center);
      var res = this.map.getResolution();

      var delta_x = moPixel.x - (size.w / 2);
      var delta_y = moPixel.y - (size.h / 2);

      var lonlat = new OpenLayers.LonLat(
        lon + delta_x * res,
        lat - delta_y * res
      );

      if (this.wrapDateLine) {
        lonlat = lonlat.wrapDateLine(this.maxExtent);
      }
      return this.getMapObjectLonLatFromLonLat(lonlat.lon, lonlat.lat);
    },

    /**
     * APIMethod: getMapObjectPixelFromMapObjectLonLat
     *
     * Parameters:
     * moLonLat - {Object} MapObject LonLat format
     *
     * Returns:
     * {Object} MapObject Pixel transtlated from MapObject LonLat
     */
    getMapObjectPixelFromMapObjectLonLat: function(moLonLat) {
      var lon = this.getLongitudeFromMapObjectLonLat(moLonLat);
      var lat = this.getLatitudeFromMapObjectLonLat(moLonLat);
      var res = this.map.getResolution();
      var extent = this.map.getExtent();
      return this.getMapObjectPixelFromXY((1 / res * (lon - extent.left)),
        (1 / res * (extent.top - lat)));
    },

    /**
     * APIMethod: setMapObjectCenter
     * Set the mapObject to the specified center and zoom
     *
     * Parameters:
     * center - {Object} MapObject LonLat format
     * zoom - {int} MapObject zoom format
     */
    setMapObjectCenter: function(center, zoom) {
      if (this.animationEnabled === false && zoom != this.mapObject.zoom) {
        var mapContainer = this.getMapContainer();
        google.maps.event.addListenerOnce(
          this.mapObject,
          "idle",
          function() {
            mapContainer.style.visibility = "";
          }
        );
        mapContainer.style.visibility = "hidden";
      }
      this.mapObject.setOptions({
        center: center,
        zoom: zoom
      });
    },

    // Bounds

    /**
     * APIMethod: getMapObjectZoomFromMapObjectBounds
     *
     * Parameters:
     * moBounds - {Object} MapObject Bounds format
     *
     * Returns:
     * {Object} MapObject Zoom for specified MapObject Bounds
     */
    getMapObjectZoomFromMapObjectBounds: function(moBounds) {
      return this.mapObject.getBoundsZoomLevel(moBounds);
    },

    /************************************
     *                                  *
     *       MapObject Primitives       *
     *                                  *
     ************************************/

    // LonLat

    /**
     * APIMethod: getMapObjectLonLatFromLonLat
     *
     * Parameters:
     * lon - {Float}
     * lat - {Float}
     *
     * Returns:
     * {Object} MapObject LonLat built from lon and lat params
     */
    getMapObjectLonLatFromLonLat: function(lon, lat) {
      var gLatLng;
      if (this.sphericalMercator) {
        var lonlat = this.inverseMercator(lon, lat);
        gLatLng = new google.maps.LatLng(lonlat.lat, lonlat.lon);
      } else {
        gLatLng = new google.maps.LatLng(lat, lon);
      }
      return gLatLng;
    },

    // Pixel

    /**
     * APIMethod: getMapObjectPixelFromXY
     *
     * Parameters:
     * x - {Integer}
     * y - {Integer}
     *
     * Returns:
     * {Object} MapObject Pixel from x and y parameters
     */
    getMapObjectPixelFromXY: function(x, y) {
      return new google.maps.Point(x, y);
    }

  };

  return OpenLayers;
})
;

/*!
 * Modified by Webdetails.
 * JavaScript - loadGoogleMaps( version, apiKey, language )
 *
 * - Load Google Maps API using jQuery Deferred.
 * Useful if you want to only load the Google Maps API on-demand.
 * - Requires jQuery 1.5
 *
 * Copyright (c) 2011 Glenn Baker
 * Dual licensed under the MIT and GPL licenses.
 *
 * The MIT License (MIT)
 * Copyright (c) 2011 Glenn Baker
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

define("cde/components/Map/engines/google/MapComponentAsyncLoader", [
  "cdf/lib/jquery"
], function($) {
  "use strict";
  return (function($) {
    var now = $.now();
    var promise;

    return function(version, apiKey) {

      if (promise) {
        return promise;
      }

      //Create a Deferred Object
      var deferred = $.Deferred();
      //Declare a resolve function, pass google.maps for the done functions
      var resolve = function() {
        deferred.resolve(window.google && google.maps ? google.maps : false);
      };
      //If google.maps exists, then Google Maps API was probably loaded with the <script> tag
      if (window.google && google.maps) {
        resolve();
        //If the google.load method exists, lets load the Google Maps API in Async.
      } else {
        if (window.google && google.load) {
          google.load("maps", version || 3, {
            //"other_params": "sensor=false",
            "callback": resolve
          });
          //Last, try pure jQuery Ajax technique to load the Google Maps API in Async.
        } else {
          var callbackName = "loadGoogleMaps_" + (now++);
          //Ajax URL params
          var params = $.extend({
              "v": version || 3,
              //'sensor': false,
              "callback": callbackName
            },
            apiKey ? {"key": apiKey} : {}
          );

          //Declare the global callback
          window[callbackName] = function() {
            resolve();
            //Delete callback
            setTimeout(function() {
              try {
                delete window[callbackName];
              } catch (e) {
              }
            }, 20);

          };

          //Can't use the jXHR promise because 'script' doesn't support 'callback=?'
          $.ajax({
            dataType: "script",
            data: params,
            url: "http://maps.googleapis.com/maps/api/js"
          });

        }
      }
      promise = deferred.promise();
      return promise;
    };

  })($);

});

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
 * the license for the specific language governing your rights and limitatio

 /*
 * OpenLayers engine.
 *
 */
define("cde/components/Map/engines/openlayers2/MapEngineOpenLayers", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "../MapEngine",
  "./OpenLayers_patchLayerGooglev3",
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

define("cde/components/Map/engines/google/MapEngineGoogle", [
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "../MapEngine",
  "./MapComponentAsyncLoader",
  "../../model/MapModel",
  "css!./styleGoogle"
], function($, _, MapEngine, MapComponentAsyncLoader, MapModel) {
  "use strict";

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
          feature.selStyle = style;
        },
        _getSelectedStyle: function() {
          return feature.selStyle;
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
      var listeners = this.controls.listenersHandle;
      listeners.click = this._toggleOnClick();
      listeners.clearOnClick = this._clearOnClick();
    },

    setZoomBoxMode: function() {
      this._removeListeners();
      this._updateMode("zoombox");
      this._updateDrag(false);
      var me = this;
      var control = this.controls.zoomBox;
      var listeners = this.controls.listenersHandle;

      listeners.click = this._toggleOnClick();

      var onMouseDown = function(e) {
        if (me.model.isZoomBoxMode()) {
          me._beginBox(control, e);
        }
      };
      listeners.mousedown = google.maps.event.addListener(this.map, "mousedown", onMouseDown);
      listeners.mousedownData = this.map.data.addListener("mousedown", onMouseDown);

      var onMouseMove = function(e) {
        if (me.model.isZoomBoxMode() && control.mouseIsDown) {
          me._onBoxResize(control, e);
        }
      };
      listeners.mousemove = google.maps.event.addListener(this.map, "mousemove", onMouseMove);
      listeners.mousemoveData = this.map.data.addListener("mousemove", onMouseMove);

      var onMouseUp = this._endBox(control,
        function() {
          return me.model.isZoomBoxMode();
        },
        function(bounds) {
          me.map.fitBounds(bounds);
        }
      );
      listeners.mouseup = google.maps.event.addListener(this.map, "mouseup", onMouseUp);
      listeners.mouseupData = this.map.data.addListener("mouseup", onMouseUp);
    },

    setSelectionMode: function() {
      this._removeListeners();
      this._updateMode("selection");
      this._updateDrag(false);
      var me = this;
      var control = me.controls.boxSelector;
      var listeners = this.controls.listenersHandle;

      listeners.toggleOnClick = this._toggleOnClick();
      listeners.clearOnClick = this._clearOnClick();

      var onMouseDown = function(e) {
        if (me.model.isSelectionMode()) {
          //console.log('Mouse up!');
          me._beginBox(control, e);
        }
      };
      listeners.mousedown = google.maps.event.addListener(this.map, "mousedown", onMouseDown);
      listeners.mousedownData = this.map.data.addListener("mousedown", onMouseDown);

      var onMouseMove = function(e) {
        if (me.model.isSelectionMode() && control.mouseIsDown) {
          me._onBoxResize(control, e);
        }
      };
      listeners.mousemove = google.maps.event.addListener(this.map, "mousemove", onMouseMove);
      listeners.mousemoveData = this.map.data.addListener("mousemove", onMouseMove);

      var onMouseUp = this._endBox(control,
        function() {
          return me.model.isSelectionMode();
        },
        function(bounds) {
          //console.log('Mouse up!');
          me.model.leafs()
            .each(function(m) {
              var id = m.get("id");
              if (me.map.data.getFeatureById(id) != undefined) {
                $.when(m.get("geoJSON")).then(function(obj) {
                  // Area contains shape
                  if (isInBounds(obj.geometry, bounds)) {
                    addToSelection(m);
                  }
                });
              }
            });
          me.trigger("engine:selection:complete");
        }
      );

      listeners.mouseup = google.maps.event.addListener(this.map, "mouseup", onMouseUp);
      listeners.mouseupData = this.map.data.addListener("mouseup", onMouseUp);
    },

    /*-----------------------------*/
    _clearOnClick: function() {
      var me = this;
      return google.maps.event.addListener(this.map, "click", function(event) {
        clearSelection(me.model);
        //console.log('Click on map!');
        me.trigger("engine:selection:complete");
      });
    },

    _toggleOnClick: function() {
      var me = this;
      return this.map.data.addListener("click", function(event) {
        //console.log('Click on feature!');
        var modelItem = event.feature.getProperty("model");
        toggleSelection(modelItem);
        me.trigger("engine:selection:complete");
        var featureType = modelItem.getFeatureType();
        me.trigger(featureType + ":click", me.wrapEvent(event));
      });
    },

    _beginBox: function(control, e) {
      control.mouseIsDown = true;
      control.mouseDownPos = e.latLng;
      this._updateDrag(true);
      this.map.setOptions({
        draggingCursor: "inherit", // allows CSS to control mouse cursor
        draggableCursor: "inherit",
        draggable: false
      });
    },

    _endBox: function(control, condition, callback) {
      var me = this;
      return function(e) {
        if (condition() && control.mouseIsDown && control.gribBoundingBox) {
          control.mouseIsDown = false;
          control.mouseUpPos = e.latLng;
          var bounds = control.gribBoundingBox.getBounds();

          callback(bounds);

          control.gribBoundingBox.setMap(null);
          control.gribBoundingBox = null;

          me._updateDrag(false);
          me.map.setOptions({
            draggingCursor: "inherit",
            draggableCursor: "inherit",
            draggable: true
          });
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

    unselectPrevShape: function(key, shapes, shapeStyle) {
      var myself = this;
      var prevSelected = this.selectedFeature;
      if (prevSelected && prevSelected[0] !== key) {
        var prevShapes = prevSelected[1];
        var prevStyle = prevSelected[2];
        _.each(prevShapes, function(s) {
          var validStyle = myself.toNativeStyle(prevStyle);
          s.setOptions(validStyle);
          s.setVisible(false);
          s.setVisible(_.has(prevStyle, "visible") ? !!prevStyle.visible : true);
        });
      }
      this.selectedFeature = [key, shapes, shapeStyle];
    },

    addLayers: function() {
      //Prepare tilesets as overlays
      var layers = [],
        layerIds = [],
        layerOptions = [];
      for (var k = 0; k < this.options.tiles.tilesets.length; k++) {
        var thisTileset = this.options.tiles.tilesets[k].slice(0);
        layerIds.push(thisTileset);
        layerOptions.push({
          mapTypeId: thisTileset
        });

        if (this.options.tiles.services[thisTileset]) {
          layers.push(this.tileLayer(thisTileset));
          var attribution = this._getTileServiceAttribution(thisTileset);
          if (!_.isEmpty(attribution)) {
            this.$attribution.append($("<div>" + attribution + "</div>"));
          }
        } else {
          layers.push("");
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
              var s = _.template("${z}/${x}/${y}", {
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

define("cde/components/Map/addIns/LocationResolver/geonames/geonames", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/lib/jquery"
], function(AddIn, Dashboard, $) {
  "use strict";
  var geonames = {
    name: "geonames",
    label: "GeoNames",
    defaults: {
      username: "",
      url: "http://ws.geonames.org/searchJSON"
    },
    implementation: function(tgt, st, opt) {
      var location;
      var name = st.address;
      var featureClass;
      if (!name) {
        //Check city
        if (st.city) {
          name = st.city;
          featureClass = "P";
        } else if (st.county) {
          name = st.county;
          featureClass = "A";
        } else if (st.region) {
          name = st.region;
          featureClass = "A";
        } else if (st.state) {
          name = st.state;
          featureClass = "A";
        } else if (st.country) {
          name = st.country;
          featureClass = "A";
        }
      }

      var params = {
        q: name.replace(/&/g, ","),
        maxRows: 1,
        dataType: "json",
        username: opt.username,
        featureClass: featureClass
      };
      if (featureClass) {
        params.featureClass = featureClass;
      }
      var onSuccess = function(result) {
        if (result.geonames && result.geonames.length > 0) {
          location = [parseFloat(result.geonames[0].lng),
            parseFloat(result.geonames[0].lat)];
          st.continuationFunction(location);
        }
      };
      var onError = function() {
        st.continuationFunction(undefined);
      };
      return $.ajax({
        dataType: "json",
        url: opt.url,
        method: "GET",
        data: params,
        success: onSuccess,
        error: onError
      });
    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(geonames));

  return geonames;

});

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

define("cde/components/Map/addIns/LocationResolver/nominatim/nominatim", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, $, _) {
  "use strict";
  var nominatim = {
    name: "openstreetmap",
    label: "OpenStreetMap",
    defaults: {
      url: "http://nominatim.openstreetmap.org/search",
      serviceParams: {
        format: "json",
        limit: "1"
      },
      mapping: {
        "street": "street",
        "postalcode": "postalcode",
        "city": "city",
        "county": "county",
        "state": "state",
        "country": "country"
      }
    },
    implementation: function(tgt, st, opt) {
      if (st.latitude || st.longitude) {
        var location = [parseFloat(st.longitude),
          parseFloat(st.latitude)];
        st.continuationFunction(location);
        return;
      }

      var params = $.extend(true, {}, opt.serviceParams);

      _.each(_.keys(st), function(key) {
        if (_.isFunction(st[key])) {
          return;
        }
        var keyLower = key.toLowerCase();
        if (keyLower in opt.mapping) {
          params[opt.mapping[keyLower]] = st[key];
        } else {
          // unrecognized fields go here
          //params['q'] = [ (params['q'] || ''), st[key] ].join(', ');
        }

      });

      if (params["q"]) {
        // we can't have "q=" and the more specific fields simultaneously.
        // so we use only "q="
        params = {
          q: params["q"] + ", " + _.compact(_.map(opt.mapping, function(field) {
            return params[field];
          })).join(", ")
        };
      }

      var onSuccess = function(result) {
        if (result && result.length > 0) {
          var location = [parseFloat(result[0].lon),
            parseFloat(result[0].lat)];
          st.continuationFunction(location);
        }
      };
      var onError = function() {
        st.continuationFunction([]);
      };
      return $.ajax({
        dataType: "json",
        method: "GET",
        url: opt.url,
        data: $.extend({}, opt.serviceParams, params),
        success: onSuccess,
        error: onError
      });

    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(nominatim));

  return nominatim;

});

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

define("cde/components/Map/addIns/LocationResolver/mapquest/mapquest", [
  "cdf/lib/jquery",
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "../nominatim/nominatim"
], function($, AddIn, Dashboard, nominatim) {
  "use strict";
  var mapquest = $.extend(true, {}, nominatim, {
    name: "mapquest",
    label: "MapQuest",
    defaults: {
      url: "http://open.mapquestapi.com/nominatim/v1/search"
    }
  });

  Dashboard.registerGlobalAddIn("NewMapComponent", "LocationResolver", new AddIn(mapquest));

  return mapquest;

});

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

define("cde/components/Map/addIns/MarkerImage/cggMarker/cggMarker", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/components/CggComponent.ext"
], function(AddIn, Dashboard, CggComponentExt) {
  "use strict";
  var cggMarker = {
    name: "cggMarker",
    label: "CGG Marker",
    defaults: {},
    implementation: function(tgt, st, opt) {
      var url = CggComponentExt.getCggDrawUrl() + "?script=" + st.cggGraphName;

      var cggParameters = {};
      if (st.width) {
        cggParameters.width = st.width;
      }
      if (st.height) {
        cggParameters.height = st.height;
      }

      cggParameters.noChartBg = true;
      var parameter;

      for (parameter in st.parameters) {
        cggParameters[parameter] = st.parameters[parameter];
      }

      // Check debug level and pass as parameter
      var level = Dashboard.debug; //TODO: review
      if (level > 1) {
        cggParameters.debug = true;
        cggParameters.debugLevel = level;
      }

      for (parameter in cggParameters) {
        if (cggParameters[parameter] !== undefined) {
          url += "&param" + parameter + "=" + encodeURIComponent(cggParameters[parameter]);
        }
      }

      return url;

    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", new AddIn(cggMarker));

  return cggMarker;

});

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

define("cde/components/Map/addIns/MarkerImage/urlMarker/urlMarker", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "../../../Map.ext"
], function(AddIn, Dashboard, NewMapComponentExt) {
  "use strict";
  var urlMarker = {
    name: "urlMarker",
    label: "Url Marker",
    defaults: {
      defaultUrl: NewMapComponentExt.getMarkerImgPath() + "marker_grey.png",
      imagePath: NewMapComponentExt.getMarkerImgPath(),
      images: [
        "marker_grey.png",
        "marker_blue.png",
        "marker_grey02.png",
        "marker_orange.png",
        "marker_purple.png"
      ]
    },
    implementation: function(tgt, st, opt) {
      if (st.url) {
        return st.url;
      }
      if (st.position) {
        return opt.imagePath + opt.images[st.position % opt.images.length] || opt.defaultUrl;
      }

      return opt.defaultUrl;
    }
  };

  Dashboard.registerGlobalAddIn("NewMapComponent", "MarkerImage", new AddIn(urlMarker));

  return urlMarker;

});

/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

define("cde/components/Map/addIns/ShapeResolver/simpleJSON", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, $, _) {
  "use strict";
  var simpleJSON = {
    name: "simpleJSON",
    label: "Simple JSON shape resolver",
    defaults: {
      url: "" //url for the resource containing the json map definitions
    },
    implementation: function(tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url) {
        $.ajax(url, {
          async: true,
          type: "GET",
          dataType: "json",
          success: function(latlonMap) {
            deferred.resolve(_.chain(latlonMap)
              .map(function(multiPolygonLatLon, key) {
                return [key, multiPolygonToGeoJSON(multiPolygonLatLon)];
              })
              .object()
              .value());
          },
          error: function() {
            deferred.resolve({});
          }
        });
      } else {
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function multiPolygonToGeoJSON(latLonMultiPolygon) {
    var lonLatMultiPolygon = _.map(latLonMultiPolygon, function(polygon) {
      return _.map(polygon, function(lineString) {
        return _.map(lineString, function(point) {
          return point.reverse();
        });
      });
    });

    return {
      type: "Feature",
      geometry: {
        type: "MultiPolygon",
        coordinates: lonLatMultiPolygon
      },
      properties: {}
    };
  }

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(simpleJSON));

  return simpleJSON;
});

/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
define("cde/components/Map/addIns/ShapeResolver/kml", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, $, _) {
  "use strict";
  var kml = {
    name: "kml",
    label: "KML shape resolver",
    defaults: {
      url: "", //url for the resource containing the kml data
      idSelector: "name",
      parseShapeKey: null
    },
    implementation: function(tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource,
        parseShapeKey = opt.parseShapeKey || st._parseShapeKey;

      if (url) {
        $.ajax(url, {
          async: true,
          type: "GET",
          processData: false,
          success: function(data) {
            deferred.resolve(getShapeFromKML(data, opt.idSelector, parseShapeKey));
          },
          error: function() {
            deferred.resolve({});
          }
        });
      } else {
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function getShapeFromKML(rawData, idSelector, parseShapeKey) {
    /*
     Parse a KML file, return a JSON dictionary where each key is associated with an array of shapes of the form
     mymap = {'Cascais:'[ [[lat0, long0],[lat1, long1]] ]}; // 1 array with a list of points
     */
    var mymap = {};

    $(rawData).find("Placemark").each(function(idx, y) {
      var key;
      if (_.isFunction(parseShapeKey)) {
        try {
          key = parseShapeKey(y);
        } catch (e) {
          key = $(y).find(idSelector).text();
        }
      } else {
        key = $(y).find(idSelector).text();
      }

      // Create an array for the strings that define the (closed) curves in a Placemark
      var polygonArray = _.map($(y).find("Polygon"), function(yy) {
        var polygon = [];
        _.each(["outerBoundaryIs", "innerBoundaryIs"], function(b) {
          var polygonObj = $(yy).find(b + " LinearRing coordinates");
          //if(polygonObj.length >0) {
          _.each(polygonObj, function(v) {
            var s = $(v).text().trim();
            if (s.length > 0) {
              var p = _.map(s.split(" "), function(el) {
                return _.map(el.split(",").slice(0, 2), parseFloat);//.reverse();
              });
              //p =  this.reducePoints(p.slice(0, pp.length -1), precision_m); // this would reduce the number of points in the shape
              polygon.push(p);
            }
          });
          //}
        });
        return polygon;
      });
      if (_.isEmpty(polygonArray)) {
        return;
      }
      if (!mymap[key]) {
        mymap[key] = multiPolygonToGeoJSON(polygonArray);
      }
    });

    return mymap;
  }

  function multiPolygonToGeoJSON(polygonArray) {
    return {
      type: "Feature",
      geometry: {
        type: "MultiPolygon",
        coordinates: polygonArray
      },
      properties: {}
    };
  }

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(kml));

  return kml;

});

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

define("cde/components/Map/addIns/ShapeResolver/geoJSON", [
  "cdf/AddIn",
  "cdf/Dashboard.Clean",
  "cdf/Logger",
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore"
], function(AddIn, Dashboard, Logger, $, _) {
  "use strict";
  var geoJSON = {
    name: "geoJSON",
    label: "GeoJSON shape resolver",
    defaults: {
      url: "", //url for the resource containing the json map definitions
      idPropertyName: "" //GeoJSON feature property that will be used to index the feature
    },
    implementation: function(tgt, st, opt) {
      var deferred = $.Deferred();
      var url = opt.url || st._shapeSource;
      if (url) {
        $.ajax(url, {
          async: true,
          type: "GET",
          dataType: "json",
          success: function(json) {
            var map = toMappedGeoJSON(json, opt.idPropertyName);
            deferred.resolve(map);
          },
          error: function() {
            Logger.log("NewMapComponent geoJSON addIn: failed to retrieve data at" + url, "debug");
            deferred.resolve({});
          }
        });
      } else {
        Logger.log("NewMapComponent geoJSON addIn: no url is defined", "debug");
        deferred.resolve(null);
      }
      return deferred.promise();
    }
  };

  function toMappedGeoJSON(json, idPropertyName) {
    var map = _.chain(json.features)
      .map(function(feature, idx) {
        var id = getFeatureId(feature, idPropertyName) || idx;
        return [id, feature];
      })
      .object()
      .value();
    return map;
  }

  function getFeatureId(feature, idPropertyName) {
    var id = feature.id;
    if (idPropertyName) {
      id = feature.properties[idPropertyName] || id;
    }
    return id;
  }

  Dashboard.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new AddIn(geoJSON));

  return geoJSON;
});

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

define("cde/components/Map/addIns/mapAddIns", [
  "./LocationResolver/geonames/geonames",
  "./LocationResolver/nominatim/nominatim",
  "./LocationResolver/mapquest/mapquest",
  "./MarkerImage/cggMarker/cggMarker",
  "./MarkerImage/urlMarker/urlMarker",
  "./ShapeResolver/simpleJSON",
  "./ShapeResolver/kml",
  "./ShapeResolver/geoJSON"
], function() {

});

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

define("cde/components/Map/Map", [
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
        function redrawUponCallback(event, callback, extraDefaults) {
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

