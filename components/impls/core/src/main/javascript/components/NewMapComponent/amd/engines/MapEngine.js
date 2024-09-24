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
