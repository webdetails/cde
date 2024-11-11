/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


define([
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
