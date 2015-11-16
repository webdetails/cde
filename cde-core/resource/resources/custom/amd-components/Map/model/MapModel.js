define([
  'cdf/lib/BaseSelectionTree',
  'amd!cdf/lib/underscore',
  'cdf/lib/jquery'
], function (BaseSelectionTree, _, $) {
  var MODES = {
    'pan': 'pan',
    'zoombox': 'zoombox',
    'selection': 'selection'
  };
  var STATES = {
    'selected': 'selected',
    'unselected': 'unselected'
  };
  var ACTIONS = {
    'normal': 'normal',
    'hover': 'hover'
  };
  var FEATURE_TYPES = {
    'shapes': 'shape',
    'markers': 'marker'
  };
  var SelectionStates = BaseSelectionTree.SelectionStates;

  return BaseSelectionTree.extend({
    defaults: {
      id: undefined,
      label: '',
      isSelected: false,
      isHighlighted: false,
      isVisible: true,
      numberOfSelectedItems: 0,
      numberOfItems: 0,
      rawData: null,
      styleMap: {}
    },

    constructor: function(){
      this.base.apply(this, arguments);
      if (this.isRoot()){
        this.setPanningMode();
      }
    },

    setPanningMode: function () {
      if (this.isSelectionMode()){
        this.trigger('selection:complete');
      }
      this.root().set('mode', MODES.pan);
      return this;
    },

    setZoomBoxMode: function () {
      this.root().set('mode', MODES.zoombox);
      return this;
    },

    setSelectionMode: function () {
      this.root().set('mode', MODES.selection);
      return this;
    },

    getMode: function () {
      return this.root().get('mode');
    },

    isPanningMode: function () {
      return this.root().get('mode') === MODES.pan;
    },

    isZoomBoxMode: function () {
      return this.root().get('mode') === MODES.zoombox;
    },

    isSelectionMode: function () {
      return this.root().get('mode') === MODES.selection;
    },

    isHover: function(){
      return this.get('isHighlighted') === true;
    },

    setHover: function(bool){
      return this.set('isHighlighted', bool === true);
    },

    /**
     * Computes the node's style, using inheritance.
     *
     * Rules:
     *
     */
    _getStyle: function (mode, state, action) {
      var myStyleMap = this.get('styleMap');

      var parentStyle;
      if (this.parent()) {
        parentStyle = this.parent()._getStyle(mode, state, action);
      } else {
        parentStyle = {};
      }

      return $.extend(true,
        getStyle(parentStyle, mode, state, action),
        getStyle(myStyleMap, mode, state, action)
      );
    },

    getStyle: function () {
      var mode = this.root().get('mode');
      var state = (this.getSelection() === SelectionStates.ALL) ? STATES.selected : STATES.unselected;
      var action = this.isHover() === true?  ACTIONS.hover: ACTIONS.normal;
      return this._getStyle(mode, state, action);
    },

    getFeatureType: function () {
      return FEATURE_TYPES[this._getParents([])[1]];
    },

    _getParents: function (list) {
      list.unshift(this.get('id'));

      if (this.parent()) {
        return this.parent()._getParents(list);
      } else {
        return list;
      }
    }


  }, {
    Modes: MODES,
    States: STATES,
    Actions: ACTIONS,
    FeatureTypes: FEATURE_TYPES,
    SelectionStates: BaseSelectionTree.SelectionStates
  });



  function getStyle(config, mode, state, action) {
    var styleKeywords = [
      _.values(ACTIONS),
      _.values(STATES),
      _.values(MODES)
    ];

    var desiredKeywords = _.map(styleKeywords, function (list, idx) {
      return _.intersection(list, [[action || '', state || '', mode || ''][idx]])[0];
    });

    return computeStyle(config, desiredKeywords);
  }

  function computeStyle(config, desiredKeywords) {
    var plainStyle = {};
    var compoundStyle = {};
    _.each(config, function (value, key) {
      if (_.isObject(value)) {
        compoundStyle[key] = value;
      } else {
        plainStyle[key] = value;
      }
    });

    //console.log('desiredKeywords', desiredKeywords);
    //console.log('computing plain style ', plainStyle);

    var style = _.reduce(compoundStyle, function (memo, value, key) {
      _.each(desiredKeywords, function (keyword) {
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