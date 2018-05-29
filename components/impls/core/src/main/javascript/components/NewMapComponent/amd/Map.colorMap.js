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
