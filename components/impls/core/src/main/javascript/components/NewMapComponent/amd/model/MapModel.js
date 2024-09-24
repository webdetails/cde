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
      var dragState = this.root().get("isDragging")  ? "dragging" : "moving"; //EXPERIMENTAL
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
      return _.intersection(list, [[/*dragState || '',*/ action || "", leafState || "",  mode || "", globalState || ""][idx]])[0];
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
