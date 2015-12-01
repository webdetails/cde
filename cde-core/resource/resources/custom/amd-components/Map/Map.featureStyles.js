define([
  "cdf/lib/jquery",
  "amd!cdf/lib/underscore",
  "cdf/Logger"
], function ($, _, Logger) {

  var styleMaps = {
    global: {
      "stroke-width": 1,
      stroke: "white",
      hover: {
        stroke: "black",
        cursor: "pointer"
      },
      unselected: {
        "fill-opacity":  0.2
      },
      selected: {
        "fill-opacity": 0.8
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
    "global_override_when_no_parameter_is_defined": {
      hover: {
        cursor: "default"
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
      this.configuration.isSelector ? {} : styleMaps.global_override_when_no_parameter_is_defined,
      styleMaps[styleName],
      localStyleMap.global,
      localStyleMap[styleName]
    );
    // TODO: Remove shapeSettings definition/property in the next major version.
    switch (styleName) {
      case "shapes":
        Logger.warn("Usage of the 'shapeSettings' property (including shapeSettings.fillOpacity, shapeSettings.strokeWidth and shapeSettings.strokeColor) is deprecated.");
        Logger.warn("Support for these properties will be removed in the next major version.");
      //return $.extend(true, styleMap, this.shapeSettings);
    }
    return styleMap;
  }


});