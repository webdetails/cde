define([
  'cdf/lib/jquery',
  'amd!cdf/lib/underscore',
  'cdf/lib/mustache',
  'cdf/lib/BaseEvents',
  '../model/MapModel',
  'text!./ControlPanel.html',
  'css!./ControlPanel'
], function ($, _, Mustache, BaseEvents, MapModel, template) {

  var MODES = MapModel.Modes;

  var ControlPanel = BaseEvents.extend({
    constructor: function (domNode, model, configuration) {
      this.base();
      this.ph = $(domNode);
      this.model = model;
      this.configuration = configuration;
      return this;
    },

    render: function () {
      var viewModel = {
        mode: this.model.getMode(),
        configuration: this.configuration
      };
      var html = Mustache.render(template, viewModel);
      this.ph.empty().append(html);
      this._bindEvents();

      return this;
    },

    zoomOut: function () {
      this.trigger('zoom:out');
      return this;
    },
    zoomIn: function () {
      this.trigger('zoom:in');
      return this;
    },

    setPanningMode: function () {
      this.model.setPanningMode();
      return this;
    },

    setZoomBoxMode: function () {
      this.model.setZoomBoxMode();
      return this;
    },

    setSelectionMode: function () {
      this.model.setSelectionMode();
      return this;
    },

    _bindEvents: function () {
      var bindings = {
        '.map-control-zoom-out': this.zoomOut,
        '.map-control-zoom-in': this.zoomIn,
        '.map-control-pan': this.setPanningMode,
        '.map-control-zoombox': this.setZoomBoxMode,
        '.map-control-select': this.setSelectionMode
      };

      var me = this;
      _.each(bindings, function (callback, selector) {
        me.ph.find(selector).click(_.bind(callback, me));
      });
      this.listenTo(this.model, 'change:mode', _.bind(this._updateView, this));
    },

    _updateView: function(){
      var mode = this.model.getMode();
      this.ph.find('.map-control-panel')
        .removeClass(_.values(MapModel.Modes).join(' '))
        .addClass(mode)
    }

  });
  return ControlPanel;
});