define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "./Map.ext",
  "cdf/Logger"
], function ($, _, MapExt, Logger) {
  "use strict";

  function cursor(image, fallback){
    var list = _.isString(image) ? [image] : image;
    return _.map(list, function(img){
      return "url(" + MapExt.getMarkerImgPath() + img + ")";
    }).join(", ") + fallback;
  }

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
      },
      pan: {
        //cursor: "move"
      },
      zoombox: {
        //cursor: cursor(["zoom-cursor.svg", "zoom-cursor.png", "zoom-cursor.cur"], "zoom-in")
      },
      selection: {

      },
      "dragging": {
        //cursor: "move"
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
    var localStyleMap = _.result(this, "styleMap") || {};
    var styleMap = $.extend(true, {},
      styleMaps.global,
      styleMaps[styleName]
    );

    // TODO: Remove shapeSettings definition/property in the next major version.
    switch (styleName) {
      case "shapes":
        Logger.warn("Usage of the 'shapeSettings' property (including shapeSettings.fillOpacity, shapeSettings.strokeWidth and shapeSettings.strokeColor) is deprecated.");
        Logger.warn("Support for these properties will be removed in the next major version.");
      return $.extend(true, styleMap, this.shapeSettings);
    }

    return $.extend(true, styleMap,
      localStyleMap.global,
      localStyleMap[styleName]
    );
  }


});