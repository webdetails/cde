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
        options: {
        }
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
