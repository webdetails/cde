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
