define("cde/components/Map/Map.lifecycle", ["amd!cdf/lib/underscore"], function (e) {
  return {
    maybeToggleBlock: function (e) {
      this.isSilent() || (e ? this.block() : this.unblock());
    }, getQueryData: function () {
      var t = this.queryState = this.query = this.dashboard.getQuery(this.queryDefinition);
      t.setAjaxOptions({async: !0}), t.fetchData(this.parameters, this.getSuccessHandler(e.bind(this.onDataReady, this)), this.getErrorHandler());
    }, _concludeUpdate: function () {
      this.postExec(), this.maybeToggleBlock(!1)
    }
  }
}), define("cde/components/Map/Map.selector", [], function () {
  return {
    getValue: function () {
      var e = this.model.leafs().filter(function (e) {
        return e.getSelection() === !0;
      }).map(function (e) {
        return e.get("id")
      }).value();
      return e
    }, setValue: function (e) {
      if (!this.model)throw"Model is not initialized";
      return this.model.setSelectedItems(e), this
    }, updateSelection: function () {
      var e = this.dashboard.getParameterValue(this.parameter);
      this.setValue(e)
    }, processChange: function () {
      return console.debug("processChange was called: ", (new Date).toISOString()),
        this.dashboard.processChange(this.name), this
    }
  }
}), define("cde/components/Map/model/MapModel", ["cdf/lib/BaseSelectionTree", "amd!cdf/lib/underscore", "cdf/lib/jquery"], function (e, t, o) {
  function n(e, o, n, l) {
    var c = [t.values(s), t.values(a), t.values(r)], p = t.map(c, function (e, i) {
      return t.intersection(e, [[l || "", n || "", o || ""][i]])[0]
    });
    return i(e, p)
  }

  function i(e, n) {
    var r = {}, a = {};
    t.each(e, function (e, o) {
      t.isObject(e) ? a[o] = e : r[o] = e
    });
    var s = t.reduce(a, function (e, r, a) {
      return t.each(n, function (t) {
        t === a && o.extend(!0, e, i(r, n))
      }), e
    }, r);
    return s
  }

  var r = {
    pan: "pan", zoombox: "zoombox", selection: "selection"
  }, a = {
    selected: "selected", unselected: "unselected"
  }, s = {normal: "normal", hover: "hover"}, l = {shapes: "shape", markers: "marker"}, c = e.SelectionStates;
  return e.extend({
    defaults: {
      id: void 0, label: "", isSelected: !1, isHighlighted: !1, isVisible: !0,
      numberOfSelectedItems: 0, numberOfItems: 0, rawData: null, styleMap: {}
    }, constructor: function () {
      this.base.apply(this, arguments), this.isRoot() && this.setPanningMode()
    }, setPanningMode: function () {
      return this.isSelectionMode() && this.trigger("selection:complete"), this.root().set("mode", r.pan),
        this
    }, setZoomBoxMode: function () {
      return this.root().set("mode", r.zoombox), this
    }, setSelectionMode: function () {
      return this.root().set("mode", r.selection), this
    }, getMode: function () {
      return this.root().get("mode");
    }, isPanningMode: function () {
      return this.root().get("mode") === r.pan
    }, isZoomBoxMode: function () {
      return this.root().get("mode") === r.zoombox
    }, isSelectionMode: function () {
      return this.root().get("mode") === r.selection;
    }, isHover: function () {
      return this.get("isHighlighted") === !0
    }, setHover: function (e) {
      return this.set("isHighlighted", e === !0)
    }, _getStyle: function (e, t, i) {
      var r, a = this.get("styleMap");
      return r = this.parent() ? this.parent()._getStyle(e, t, i) : {}, o.extend(!0, n(r, e, t, i), n(a, e, t, i));
    }, getStyle: function () {
      var e = this.root().get("mode"), t = this.getSelection() === c.ALL ? a.selected : a.unselected, o = this.isHover() === !0 ? s.hover : s.normal;
      return this._getStyle(e, t, o)
    }, getFeatureType: function () {
      return l[this._getParents([])[1]];
    }, _getParents: function (e) {
      return e.unshift(this.get("id")), this.parent() ? this.parent()._getParents(e) : e;
    }
  }, {Modes: r, States: a, Actions: s, FeatureTypes: l, SelectionStates: e.SelectionStates});
}), define("cde/components/Map/_getMapping", ["amd!cdf/lib/underscore"], function (e) {
  function t(t) {
    var o = {};
    if (!t.metadata || 0 == t.metadata.length)return o;
    var n = {
      key: "id",
      id: "id", fill: "fill", fillColor: "fill", r: "r", latitude: "latitude", longitude: "longitude",
      address: "address", description: "description", marker: "marker", markerwidth: "markerWidth",
      markerheight: "markerHeight", popupcontents: "popupContents", popupwidth: "popupWidth",
      popupheight: "popupHeight"
    }, i = e.chain(t.metadata).pluck("colName").map(function (e) {
      return e.toLowerCase()
    }).value(), o = e.chain(i).map(function (e, t) {
      var o = n[e];
      return o ? [o, t] : [e, t];
    }).compact().object().value();
    return ("latitude" in o || "longitude" in o) && (o.addressType = "coordinates"),
    "address" in o && !o.addressType && (o.addressType = "address"), o.id || (o.id = 0), o
  }

  return t;
}), define("cde/components/Map/FeatureStore/shapeConversion", [], function () {
  var e = {
    simplifyPoints: function (e, t) {
      function o(e, t) {
        var i = e[0], r = e[e.length - 1];
        if (e.length < 3)return e;
        for (var a = -1, s = 0, l = 1; l < e.length - 1; l++) {
          var c = n(e[l], i, r);
          c > s && (s = c, a = l)
        }
        if (s > t) {
          var p = e.slice(0, a + 1), d = e.slice(a), u = o(p, t), h = o(d, t), m = u.slice(0, u.length - 1).concat(h);
          return m
        }
        return [i, r]
      }

      function n(e, t, o) {
        var n, i, r;
        return t[0] == o[0] ? n = Math.abs(e[0] - t[0]) : (i = (o[1] - t[1]) / (o[0] - t[0]),
          r = t[1] - i * t[0], n = Math.abs(i * e[0] - e[1] + r) / Math.sqrt(Math.pow(i, 2) + 1)), n
      }

      return 0 > t ? e : o(e, t / 63e5);
    }, exportShapeDefinition: function () {
      this.shapeDefinition && window.open("data:text/json;charset=utf-8," + escape(JSON.stringify(this.shapeDefinition)));
    }
  };
  return e
}), define("cde/components/Map/FeatureStore/resolveShapes", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "./shapeConversion"], function (e, t, o) {
  function n(n, i, r) {
    var a = this.getAddIn("ShapeResolver", r.addIns.ShapeResolver.name), s = r.addIns.ShapeResolver.options.url;
    !a && s && (a = s.endsWith("json") || s.endsWith("js") ? this.getAddIn("ShapeResolver", "simpleJSON") : this.getAddIn("ShapeResolver", "kml"));
    var l = e.Deferred();
    if (!a)return l.resolve({}), l.promise();
    var c = t.pluck(n.resultset, i.id), p = this, d = {
      keys: c,
      ids: c,
      tableData: n,
      _simplifyPoints: o.simplifyPoints,
      _parseShapeKey: r.addIns.ShapeResolver.options.parseShapeKey,
      _shapeSource: s
    }, u = a.call(p, d, this.getAddInOptions("ShapeResolver", a.getName()));
    return e.when(u).then(function (e) {
      var o = t.chain(e).map(function (e, t) {
        return [t, e]
      }).object().value();
      l.resolve(o)
    }),
      l.promise()
  }

  return n
}), define("cde/components/Map/FeatureStore/resolveMarkers", ["cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t) {
  function o(o, i, r) {
    var a = this.getAddIn("LocationResolver", r.addIns.LocationResolver.name), s = e.Deferred();
    if (!a)return s.resolve({}), s.promise();
    var l, c = this, p = this.getAddInOptions("LocationResolver", a.getName());
    return l = "coordinates" === i.addressType ? t.chain(o.resultset).map(function (e) {
      var t = e[i.id], o = [e[i.longitude], e[i.latitude]];
      return [t, n(o)]
    }).object().value() : t.chain(o.resultset).map(function (r, s) {
      var l = e.Deferred(), d = r[i.id], u = void 0 != i.address ? r[i.address] : void 0, h = {
        data: r, position: s, address: u, addressType: i.addressType, key: d, id: d, mapping: i, tableData: o,
        continuationFunction: function (e) {
          l.resolve(n(e))
        }
      }, m = ["country", "city", "county", "region", "state"];
      t.each(t.pick(i, m), function (e, t) {
        void 0 != e && (h[t] = r[e])
      });
      try {
        a.call(c, h, p)
      } catch (g) {
        l.resolve(null)
      }
      return [d, l.promise()]
    }).object().value(), s.resolve(l), s.promise();
  }

  function n(e) {
    var t = e[0], o = e[1], n = {
      geometry: {
        coordinates: [t, o], type: "Point", properties: {
          latitude: o, longitude: t
        }
      }, type: "Feature"
    };
    return n
  }

  return o
}), define("cde/components/Map/Map.model", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "cdf/Logger", "./model/MapModel", "./_getMapping", "./FeatureStore/resolveShapes", "./FeatureStore/resolveMarkers"], function (e, t, o, n, i, r, a) {
  return {
    resolveFeatures: function (t) {
      var o = i(t);
      this.mapping = e.extend(!0, o, this.visualRoles),
        this.features = this.features || {};
      var n, r = this;
      return n = "shapes" === this.mapMode ? this._resolveShapes(t, this.mapping, this.configuration).then(function (e) {
        return r.features.shapes = e, t
      }) : "markers" === this.mapMode ? this._resolveMarkers(t, this.mapping, this.configuration).then(function (e) {
        return r.features.markers = e, t
      }) : e.when(t), n.promise()
    }, _resolveShapes: r, _resolveMarkers: a,
    initModel: function (e) {
      this.model = new n({styleMap: this.getStyleMap("global")});
      var t = this._initSeries(this.mapMode, e);
      e && e.metadata && e.resultset && e.resultset.length > 0 && this._addSeriesToModel(t, e)
    }, _initSeries: function (e, t) {
      var o = this.getColorMap(), n = {
        id: e, label: e, styleMap: this.getStyleMap(e), colormap: o,
        extremes: this._detectExtremes(t)
      };
      return this.model.add(n), this.model.findWhere({
        id: e
      })
    }, visualRoles: {}, scales: {fill: "default", r: [10, 20]}, attributeMapping: {
      fill: function (e, o, i, r, a) {
        var s = r[i.fill], l = e.mode === n.Modes.pan && e.state === n.States.unselected && e.action === n.Actions.normal;
        l = l || e.mode === n.Modes.selection && e.state === n.States.selected && e.action === n.Actions.normal,
          l = !0;
        var c = o.get("colormap") || this.getColorMap(), p = e.mode === n.Modes.selection && e.state === n.States.unselected && e.action === n.Actions.normal;
        return p = !1, p && (c = t.map(c, this.toGrayscale)), t.isNumber(s) && (l || p) ? this.mapColor(s, o.get("extremes").fill.min, o.get("extremes").fill.max, c) : void 0;
      }, label: function (e, o, n, i, r) {
        return t.isEmpty(i) ? void 0 : i[n.label] + ""
      }, r: function (e, o, n, i, r) {
        var a = i[n.r];
        if (t.isNumber(a)) {
          var s = this.scales.r[0], l = this.scales.r[1], c = o.get("extremes").r, p = Math.sqrt(s * s + (l * l - s * s) * (a - c.min) / c.max - c.min);
          if (t.isFinite(p))return p
        }
      }
    }, _detectExtremes: function (e) {
      var o = t.chain(this.mapping).map(function (o, n) {
        if (!t.isFinite(o))return [n, {}];
        var i, r = t.pluck(e.resultset, o);
        return i = "Numeric" === e.metadata[o].colType ? {
          type: "numeric", min: t.min(r), max: t.max(r)
        } : {type: "categoric", items: t.uniq(r)}, [n, i];
      }).object().value();
      return o
    }, _addSeriesToModel: function (o, i) {
      var r = e.extend({}, this.mapping), a = t.pluck(i.metadata, "colName"), s = this, l = n.Modes, c = n.States, p = n.Actions, d = t.map(i.resultset, function (e, n) {
        var i = s._getItemId(r, e, n), d = {};
        t.each(l, function (i) {
          t.each(c, function (a) {
            t.each(p, function (l) {
              t.each(s.attributeMapping, function (c, p) {
                if (!(t.isUndefined(r[p]) || r[p] >= e.length)) {
                  var u = {mode: i, state: a, action: l}, h = t.isFunction(c) ? c.call(s, u, o, r, e, n) : c;
                  t.isUndefined(h) || (d[i] = d[i] || {},
                    d[i][a] = d[i][a] || {}, d[i][a][l] = d[i][a][l] || {}, d[i][a][l][p] = h)
                }
              })
            })
          })
        });
        var u = s.features.shapes ? s.features.shapes[i] : void 0, h = s.features.markers ? s.features.markers[i] : void 0, m = "shape" === o.getFeatureType() ? u : h;
        return {
          id: i, label: i, styleMap: d, geoJSON: m, rowIdx: n, rawData: e, data: t.object(t.zip(a, e))
        }
      });
      o.add(d)
    }, _getItemId: function (e, o, n) {
      var i = e.id;
      return t.isFinite(i) || (i = "shapes" === this.mapMode ? 0 : -1),
        i >= 0 && i < o.length ? o[i] : n
    }
  }
}), define("cde/components/Map/Map.configuration", ["cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t) {
  function o() {
    var o = {
      MarkerImage: {
        name: this.markerCggGraph ? "cggMarker" : this.markerImageGetter,
        options: {
          cggScript: this.markerCggGraph, parameters: this.cggGraphParameters, height: this.markerHeight,
          width: this.markerWidth, iconUrl: this.marker
        }
      }, ShapeResolver: {
        name: this.shapeResolver,
        options: {url: this.shapeSource, parseShapeKey: this.parseShapeKey}
      }, LocationResolver: {
        name: this.locationResolver || "openstreetmap", options: {}
      }, MapEngine: {
        name: this.mapEngineType,
        options: {
          rawOptions: {map: {}}, tileServices: this.tileServices, tileServicesOptions: this.tileServicesOptions,
          tilesets: t.isString(this.tilesets) ? [this.tilesets] : this.tilesets, API_KEY: this.API_KEY || window.API_KEY
        }
      }
    }, n = {
      center: {
        latitude: parseFloat(this.centerLatitude), longitude: parseFloat(this.centerLongitude)
      }, zoomLevel: {min: 0, max: 1 / 0, "default": this.defaultZoomLevel}
    };
    return e.extend(!0, {}, {
      isSelector: !t.isEmpty(this.parameter), addIns: o, styleMap: this.styleMap, viewport: n
    }, t.result(this, "options"));
  }

  return {getConfiguration: o}
}), define("cde/components/Map/Map.featureStyles", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "cdf/Logger"], function (e, t, o) {
  function n(n) {
    var r = t.result(this, "styleMap") || {}, a = e.extend(!0, {}, i.global, this.configuration.isSelector ? {} : i.global_override_when_no_parameter_is_defined, i[n], r.global, r[n]);
    switch (n) {
      case"shapes":
        o.warn("Usage of the 'shapeSettings' property (including shapeSettings.fillOpacity, shapeSettings.strokeWidth and shapeSettings.strokeColor) is deprecated."),
          o.warn("Support for these properties will be removed in the next major version.");
    }
    return a
  }

  var i = {
    global: {
      "stroke-width": 1, stroke: "white", hover: {
        stroke: "black", cursor: "pointer"
      }, unselected: {"fill-opacity": .2}, selected: {"fill-opacity": .8}
    }, global_override_when_no_parameter_is_defined: {
      unselected: {"fill-opacity": .8}, hover: {cursor: "default"}
    }, markers: {
      r: 10, symbol: "circle",
      labelAlign: "cm", labelYOffset: -20, fill: "red", "stroke-width": 2
    }, shapes: {
      "stroke-width": 1.5,
      normal: {"z-index": 0}, hover: {"z-index": 1}
    }
  };
  return {getStyleMap: n}
}), define("cde/components/Map/Map.colorMap", ["amd!cdf/lib/underscore"], function (e) {
  function t(t) {
    var o = e.clone(t);
    return e.isArray(t) ? (o = t, 3 === o.length && o.push(1)) : e.isString(t) && (t.startsWith("#") ? o = [parseInt(t.substring(1, 3), 16), parseInt(t.substring(3, 5), 16), parseInt(t.substring(5, 7), 16), 1] : t.startsWith("rgba") && (o = t.slice(5, -1).split(",").map(parseFloat))),
      o
  }

  function o(e, t, o) {
    var n, i, r, a = [], s = [];
    for (n = 0; n < e.length; n++)for (a[n] = [], i = 0, r = (t[n] - e[n]) / o; o > i; i++)a[n][i] = e[n] + i * r;
    for (n = 0; n < a[0].length && 3 > n; n++)for (s[n] = [], i = 0; i < a.length; i++)s[n][i] = Math.round(a[i][n]);
    return s
  }

  var n = {
    colormaps: {
      "default": ["#79be77", "#96b761", "#b6ae4c", "#e0a433", "#f4a029", "#fa8e1f", "#f47719", "#ec5f13", "#e4450f", "#dc300a"],
      default0: [[0, 102, 0, 1], [255, 255, 0, 1], [255, 0, 0, 1]], jet: [], gray: [[0, 0, 0, 255], [255, 255, 255, 1]],
      "french-flag": [[255, 0, 0, 1], [255, 254, 255, 1], [0, 0, 255, 1]]
    }, getColorMap: function () {
      var n = [];
      n = null == this.colormap || e.isArray(this.colormap) && !this.colormap.length ? e.clone(this.colormaps["default"]) : e.map(this.colormap, JSON.parse),
        n = e.map(n, t);
      var i = [];
      for (k = 1, L = n.length; k < L; k++)i = i.concat(o(n[k - 1], n[k], 32));
      return e.map(i, function (e) {
        return "rgba(" + e.join(",") + ")"
      })
    }, mapColor: function (e, t, o, n) {
      var i = n.length, r = (e - t) / (o - t);
      return n[Math.floor(r * (i - 1))]
    }, toGrayscale: function (e) {
      var o = t(e), n = Math.round(.2989 * o[0] + .587 * o[1] + .114 * o[2]), i = [n, n, n, o[3]];
      return "rgba(" + i.join(",") + ")"
    }
  };
  return n
}), define("text!cde/components/Map/ControlPanel/ControlPanel.html", [], function () {
  return '<div class="map-control-panel">\n    <div class="map-control-button map-control-zoom-in"></div>\n    <div class="map-control-button map-control-zoom-out"></div>\n    <div class="map-controls-mode {{mode}}">\n        <div class="map-control-button map-control-pan"></div>\n        <div class="map-control-button map-control-zoombox"></div>\n        {{#configuration.isSelector}}\n        <div class="map-control-button map-control-select"></div>\n        {{/configuration.isSelector}}\n    </div>\n</div>';
}), define("cde/components/Map/ControlPanel/ControlPanel", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "cdf/lib/mustache", "cdf/lib/BaseEvents", "../model/MapModel", "text!./ControlPanel.html", "css!./ControlPanel"], function (e, t, o, n, i, r) {
  var a = i.Modes, s = n.extend({
    constructor: function (t, o, n) {
      return this.base(), this.ph = e(t),
        this.model = o, this.configuration = n, this
    }, render: function () {
      var e = {
        mode: this.model.getMode(),
        configuration: this.configuration
      }, t = o.render(r, e);
      return this.ph.empty().append(t),
        this._bindEvents(), this
    }, zoomOut: function () {
      return this.trigger("zoom:out"), this
    },
    zoomIn: function () {
      return this.trigger("zoom:in"), this
    }, setPanningMode: function () {
      return this.model.setPanningMode(), this
    }, setZoomBoxMode: function () {
      return this.model.setZoomBoxMode(),
        this
    }, setSelectionMode: function () {
      return this.model.setSelectionMode(), this
    }, _bindEvents: function () {
      var e = {
        ".map-control-zoom-out": this.zoomOut,
        ".map-control-zoom-in": this.zoomIn,
        ".map-control-pan": this.setPanningMode,
        ".map-control-zoombox": this.setZoomBoxMode,
        ".map-control-select": this.setSelectionMode
      }, o = this;
      t.each(e, function (e, n) {
        o.ph.find(n).click(t.bind(e, o))
      }), this.listenTo(this.model, "change:mode", t.bind(this._updateView, this));
    }, _updateView: function () {
      var e = this.model.getMode();
      this.ph.find(".map-controls-mode").removeClass(t.values(a).join(" ")).addClass(e);
    }
  });
  return s
}), define("cde/components/Map/Map.tileServices", [], function () {
  var e = {
    "default": "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/${z}/${y}/${x}.png",
    apple: "http://gsp2.apple.com/tile?api=1&style=slideshow&layers=default&lang=en_US&z=${z}&x=${x}&y=${y}&v=9",
    google: "http://mt{switch:0,1,2,3}.googleapis.com/vt?x=${x}&y=${y}&z=${z}",
    mapquest: "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png",
    "mapquest-normal": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.png",
    "mapquest-hybrid": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/hyb/${z}/${x}/${y}.png",
    "mapquest-sat": "http://otile{switch:1,2,3,4}.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg",
    "mapbox-world-light": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-light/${z}/${x}/${y}.jpg",
    "mapbox-world-dark": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/mapbox.world-dark/${z}/${x}/${y}.jpg",
    "mapbox-terrain": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-9ijuk24y/${z}/${x}/${y}.jpg",
    "mapbox-satellite": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.map-qfyrx5r8/${z}/${x}/${y}.png",
    "mapbox-example": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.c7d2024a/${z}/${x}/${y}.png",
    "mapbox-example2": "https://{switch:a,b,c,d}.tiles.mapbox.com/v3/examples.bc17bb2a/${z}/${x}/${y}.png",
    openstreetmaps: "http://{switch:a,b,c}.tile.openstreetmap.org/${z}/${x}/${y}.png",
    openmapsurfer: "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-roads": "http://129.206.74.245:8001/tms_r.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-semitransparent": "http://129.206.74.245:8003/tms_h.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-hillshade": "http://129.206.74.245:8004/tms_hs.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-contour": "http://129.206.74.245:8006/tms_b.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-administrative": "http://129.206.74.245:8007/tms_b.ashx?x=${x}&y=${y}&z=${z}",
    "openmapsurfer-roads-grayscale": "http://129.206.74.245:8008/tms_rg.ashx?x=${x}&y=${y}&z=${z}",
    stamen: "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
    "stamen-terrain": "http://{switch:a,b,c,d}.tile.stamen.com/terrain/${z}/${x}/${y}.jpg",
    "stamen-terrain-background": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-background/${z}/${x}/${y}.jpg",
    "stamen-terrain-labels": "http://{switch:a,b,c,d}.tile.stamen.com/terrain-labels/${z}/${x}/${y}.jpg",
    "stamen-toner": "http://{switch:a,b,c,d}.tile.stamen.com/toner/${z}/${x}/${y}.png",
    "stamen-toner-lite": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lite/${z}/${x}/${y}.png",
    "stamen-toner-background": "http://{switch:a,b,c,d}.tile.stamen.com/toner-background/${z}/${x}/${y}.png",
    "stamen-toner-hybrid": "http://{switch:a,b,c,d}.tile.stamen.com/toner-hybrid/${z}/${x}/${y}.png",
    "stamen-toner-labels": "http://{switch:a,b,c,d}.tile.stamen.com/toner-labels/${z}/${x}/${y}.png",
    "stamen-toner-lines": "http://{switch:a,b,c,d}.tile.stamen.com/toner-lines/${z}/${x}/${y}.png",
    "stamen-toner-2010": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2010/${z}/${x}/${y}.png",
    "stamen-toner-2011": "http://{switch:a,b,c,d}.tile.stamen.com/toner-2011/${z}/${x}/${y}.png",
    "stamen-watercolor": "http://{switch:a,b,c,d}.tile.stamen.com/watercolor/${z}/${x}/${y}.jpg",
    "nokia-normal": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/${z}/${x}/${y}/256/png8",
    "nokia-normal-grey": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.grey/${z}/${x}/${y}/256/png8",
    "nokia-normal-transit": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day.transit/${z}/${x}/${y}/256/png8",
    "nokia-satellite": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/satellite.day/${z}/${x}/${y}/256/png8",
    "nokia-terrain": "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/terrain.day/${z}/${x}/${y}/256/png8",
    "arcgis-street": "http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Map/MapServer/tile/${z}/${y}/${x}",
    "arcgis-topographic": "http://services.arcgisonline.com/ArcGIS/rest/services/World_street_Topo/MapServer/tile/${z}/${y}/${x}",
    "arcgis-natgeo": "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/${z}/${y}/${x}",
    "arcgis-world": "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}",
    "arcgis-lightgray": "http://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/${z}/${y}/${x}.png",
    "arcgis-delorme": "http://services.arcgisonline.com/ArcGIS/rest/services/Specialty/DeLorme_World_Base_Map/MapServer/tile/${z}/${y}/${x}"
  };
  return {
    tileServices: e, otherTileServices: [], tileServicesOptions: {
      apple: {
        minZoom: 3,
        maxZoom: 14
      }
    }
  }
}), define("cde/components/Map/engines/MapEngine", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "cdf/lib/BaseEvents", "../model/MapModel"], function (e, t, o, n) {
  var i = n.SelectionStates, r = o.extend({
    tileServices: void 0, tileServicesOptions: void 0,
    tileLayer: function (e) {
    }, init: function () {
      var t = e.Deferred();
      return t.resolve(), t.promise();
    }, renderMap: function (e) {
    }, render: function (e) {
      this.model = e;
      var t = this;
      this.listenTo(this.model.root(), "change:mode", function (e, o) {
        var n = {
          selection: t.setSelectionMode, zoombox: t.setZoomBoxMode, pan: t.setPanningMode
        };
        n[o] && n[o].call(t), e.leafs().each(function (e) {
          t.updateItem(e)
        })
      }), this.listenTo(this.model, "change:isSelected change:isHighlighted change:isVisible", function (e, o) {
        e.children() || t.updateItem(e)
      }), e.leafs().each(function (e) {
        t.renderItem(e)
      }), e.isPanningMode() && t.setPanningMode(),
      e.isZoomBoxMode() && t.setZoomBoxMode(), e.isSelectionMode() && t.setSelectionMode()
    },
    updateViewport: function (e, t, o) {
    }, showPopup: function () {
    }, _wrapEvent: function (t) {
      return {
        model: t,
        data: e.extend(!0, {}, t.get("data"), t.get("rawData")),
        id: t.get("id"),
        featureType: t.getFeatureType(),
        style: t.getStyle(),
        isSelected: function () {
          return t.getSelection() === i.ALL
        }
      }
    }, toNativeStyle: function (e) {
      var o = {};
      return t.each(e, function (e, t) {
        switch (t) {
          case"visible":
          case"zIndex":
          case"fillColor":
          case"fillOpacity":
          case"strokeColor":
          case"strokeOpacity":
          case"strokeWidth":
        }
      }), o
    },
    wrapEvent: function (e, t) {
      return {
        latitude: void 0, longitude: void 0, data: void 0, feature: void 0,
        featureType: t, style: void 0, mapEngineType: "abstract", draw: function (e) {
        }, raw: void 0
      }
    }, _selectUrl: function (e, t) {
      for (var o = 1, n = (Math.sqrt(5) - 1) / 2, i = 0, r = e.length; r > i; i++)o *= e.charCodeAt(i) * n,
        o -= Math.floor(o);
      return t[Math.floor(o * t.length)]
    }, _switchUrl: function (e) {
      var t = e.match(/(http[s]?:\/\/[0-9a-z.]*?)\{switch:([a-z0-9,]+)\}(.*)/);
      if (!t || 0 == t.length)return e;
      for (var o = t[2].split(","), n = [], i = 0; i < o.length; i++)n.push(t[1] + o[i] + t[3]);
      return n
    }, _getTileServiceURL: function (e) {
      var t = this.tileServices[e];
      return t || e.length > 0 && e.indexOf("{") > -1 && (t = e),
        t
    }
  });
  return r
}), define("cde/components/Map/engines/openlayers2/MapEngineOpenLayers", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "../MapEngine", "cdf/lib/OpenLayers", "../../model/MapModel", "cdf/Logger", "css!./styleOpenLayers2"], function (e, t, o, n, i, r) {
  function a(e) {
    e.model && e.model.setSelection(SelectionStates.NONE)
  }

  function s(e) {
    var t = i.SelectionStates, o = {};
    return o[t.ALL] = t.NONE, o[t.SOME] = t.NONE, o[t.NONE] = t.ALL, function (t) {
      this.clickFeature(t);
      var n = t.attributes.model, i = o[n.getSelection()];
      n.setSelection(i), n.setHover(!1);
      var r = n.getFeatureType() + ":click";
      e.trigger("engine:selection:complete"), e.trigger(r, e.wrapEvent({feature: t}))
    }
  }

  var l = o.extend({
    map: void 0, API_KEY: 0, constructor: function (t) {
      this.base(), e.extend(this, t), this.layers = {},
        this.controls = {}
    }, toNativeStyle: function (e) {
      var o = {
        fill: "fillColor", "fill-opacity": "fillOpacity",
        stroke: "strokeColor", "stroke-opacity": "strokeOpacity", "stroke-width": "strokeWidth",
        r: "pointRadius", "z-index": "graphicZIndex", "icon-url": "externalGraphic", iconUrl: "externalGraphic",
        width: "graphicWidth", height: "graphicHeight", symbol: "graphicName", fillColor: "fillColor",
        fillOpacity: "fillOpacity", strokeColor: "strokeColor", strokeOpacity: "strokeOpacity",
        strokeWidth: "strokeWidth", zIndex: "graphicZIndex"
      }, n = {};
      return t.each(e, function (e, t) {
        var i = o[t];
        if (i)n[i] = e; else switch (t) {
          case"visible":
            n.display = e ? !0 : "none";
            break;
          default:
            n[t] = e
        }
      }), n
    }, wrapEvent: function (t) {
      var o, i = t.feature, r = t.feature.attributes.model, a = this.controls.mousePosition.lastXy;
      o = a ? this.map.getLonLatFromPixel(a).transform(this.map.getProjectionObject(), new n.Projection("EPSG:4326")) : {
        lat: void 0, lon: void 0
      };
      var s = this;
      return e.extend(this._wrapEvent(r), {
        mapEngineType: "openlayers2",
        latitude: o.lat, longitude: o.lon, feature: i, _popup: function (t, o) {
          var n = e.extend({
            width: 100,
            height: 100
          }, o || {});
          s.showPopup(null, i, n.height, n.width, t, null, null)
        }, draw: function (e) {
          var o = s.toNativeStyle(e);
          t.feature.layer.drawFeature(i, o)
        }, _setSelectedStyle: function (e) {
          t.feature.attributes.clickSelStyle = e
        }, _getSelectedStyle: function () {
          return t.feature.attributes.clickSelStyle;
        }, raw: t
      })
    }, renderItem: function (t) {
      if (t) {
        var o = this.layers[t.root().children().first().get("id")], n = t.get("geoJSON"), i = this;
        e.when(n).then(function (n) {
          if (n) {
            var r = i._geoJSONParser.parseFeature(n), a = t.getStyle();
            e.extend(!0, r, {attributes: {id: t.get("id"), model: t}, style: i.toNativeStyle(a)}), o.addFeatures([r]);
          }
        })
      }
    }, showPopup: function (o, i, r, a, s, l, c) {
      if (l && l.length > 0) {
        var p = e("<div/>");
        p.append(e("#" + l)),
          s = p.html()
      }
      var d = "featurePopup";
      void 0 != c && (d += c.substring(1));
      var u = i.geometry.getCentroid();
      i.lonlat = new n.LonLat(u.x, u.y);
      var h = new n.Popup.Anchored(d, i.lonlat, new n.Size(a, r), s, null, !0, null);
      i.popup = h, h.feature = i, t.each(this.map.popups, function (e) {
        e.hide()
      }), this.map.addPopup(h, !0);
    }, renderMap: function (e) {
      var o = new n.Projection("EPSG:900913"), i = new n.Projection("EPSG:4326"), r = {
        zoom: this.options.viewport.zoomLevel["default"],
        zoomDuration: 10,
        displayProjection: i,
        projection: o,
        controls: [new n.Control.Navigation, new n.Control.DragPan, new n.Control.PinchZoom, new n.Control.LayerSwitcher({
          ascending: !1
        }), new n.Control.ScaleLine, new n.Control.KeyboardDefaults, new n.Control.Attribution, new n.Control.TouchNavigation]
      };
      n.TileManager && (r.tileManager = new n.TileManager), this.map = new n.Map(e, r);
      var a = this;
      this.map.isValidZoomLevel = function (e) {
        var o = t.isFinite(a.options.viewport.zoomLevel.min) ? a.options.viewport.zoomLevel.min : 0, n = t.isFinite(a.options.viewport.zoomLevel.max) ? a.options.viewport.zoomLevel.max : this.getNumZoomLevels();
        return null != e && e >= o && n >= e
      }, this.addLayers(), this.addControls(), this.registerViewportEvents(),
        this._geoJSONParser = new n.Format.GeoJSON({
          ignoreExtraDims: !0, internalProjection: this.map.getProjectionObject(),
          externalProjection: i
        })
    }, addLayers: function () {
      var e = this;
      t.each(this.tilesets, function (t) {
        var o, i = t.slice(0).split("-")[0], r = t.slice(0).split("-").slice(1).join("-") || "default";
        switch (i) {
          case"googleXXX":
            o = new n.Layer.Google("Google Streets", {
              visibility: !0, version: "3"
            });
            break;
          case"opengeo":
            o = new n.Layer.WMS(t, "http://maps.opengeo.org/geowebcache/service/wms", {
              layers: r, bgcolor: "#A1BDC4"
            }, {wrapDateLine: !0, transitionEffect: "resize"});
            break;
          default:
            o = e.tileLayer(t)
        }
        e.map.addLayer(o), e.layers[t] = o
      }), this.layers.shapes = new n.Layer.Vector("Shapes", {
        rendererOptions: {zIndexing: !0}
      }), this.layers.markers = new n.Layer.Vector("Markers"),
        this.map.addLayers([this.layers.shapes, this.layers.markers])
    }, setPanningMode: function () {
      this.controls.clickCtrl.activate(), this.controls.zoomBox.deactivate(), this.controls.boxSelector.deactivate();
    }, setZoomBoxMode: function () {
      this.controls.clickCtrl.activate(), this.controls.zoomBox.activate(),
        this.controls.boxSelector.deactivate()
    }, setSelectionMode: function () {
      this.controls.clickCtrl.deactivate(),
        this.controls.boxSelector.activate(), this.controls.zoomBox.deactivate()
    }, zoomIn: function () {
      this.map.zoomIn()
    }, zoomOut: function () {
      this.map.zoomOut()
    }, updateViewport: function (e, o, i) {
      if (t.isFinite(i))this.map.zoomTo(i); else {
        var r = new n.Bounds, a = this.layers.markers.getDataExtent(), s = this.layers.shapes.getDataExtent();
        a || s ? (r.extend(a), r.extend(s)) : r = null, r ? this.map.zoomToExtent(r) : this.map.zoomTo(this.options.viewport.zoomLevel["default"]);
      }
      var l, c = new n.Projection("EPSG:4326");
      t.isFinite(o) && t.isFinite(e) ? (l = new n.LonLat(e, o).transform(c, this.map.getProjectionObject()),
        this.map.setCenter(l)) : r || (l = new n.LonLat(-10, 20).transform(c, this.map.getProjectionObject()),
        this.map.setCenter(l))
    }, addControls: function () {
      this._addControlMousePosition(), this._addControlHover(),
        this._addControlClick(), this._addControlBoxSelector(), this._addControlZoomBox()
    },
    _addControlMousePosition: function () {
      this.controls.mousePosition = new n.Control.MousePosition,
        this.map.addControl(this.controls.mousePosition)
    }, _addControlClick: function () {
      this.controls.clickCtrl = new n.Control.SelectFeature([this.layers.markers, this.layers.shapes], {
        clickout: !0, callbacks: {clickout: a(this), click: s(this)}
      }), this.controls.clickCtrl.handlers.feature.stopDown = !1,
        this.map.addControl(this.controls.clickCtrl)
    }, _addControlBoxSelector: function () {
      var e = this;
      this.controls.boxSelector = new n.Control.SelectFeature([this.layers.shapes, this.layers.markers], {
        clickout: !0, toggle: !0, multiple: !0, hover: !1, box: !0, callbacks: {
          clickout: a(this), click: s(this)
        }
      }), this.map.addControl(this.controls.boxSelector), this.controls.boxSelector.events.on({
        activate: function (e) {
          e.object.unselectAll()
        }, boxselectionend: function (o) {
          t.each(o.layers, function (e) {
            t.each(e.selectedFeatures, function (e) {
              var t = !e.attributes.model.getSelection();
              e.attributes.model.setSelection(t);
            })
          }), o.object.unselectAll(), e.trigger("engine:selection:complete")
        }
      })
    }, _addControlZoomBox: function () {
      this.controls.zoomBox = new n.Control.ZoomBox({zoomOnClick: !1}), this.map.addControl(this.controls.zoomBox);
    }, _addControlHover: function () {
      function e(e) {
        var o = {
          featurehighlighted: "mouseover",
          featureunhighlighted: "mouseout"
        };
        if (o[e.type]) {
          var n = e.feature.attributes.model;
          n.setHover("mouseover" === o[e.type]),
            t.trigger(n.getFeatureType() + ":" + o[e.type], t.wrapEvent(e))
        }
      }

      var t = this;
      this.controls.hoverCtrl = new n.Control.SelectFeature([this.layers.markers, this.layers.shapes], {
        hover: !0, highlightOnly: !0, renderIntent: "temporary", eventListeners: {
          featurehighlighted: e,
          featureunhighlighted: e
        }, outFeature: function (e) {
          if (this.hover)if (this.highlightOnly)if (e._lastHighlighter == this.id)if (e._prevHighlighter && e._prevHighlighter != this.id) {
            delete e._lastHighlighter;
            var t = this.map.getControl(e._prevHighlighter);
            t && (t.highlight(e),
              this.events.triggerEvent("featureunhighlighted", {feature: e}))
          } else this.unhighlight(e); else this.events.triggerEvent("featureunhighlighted", {
            feature: e
          }); else this.unselect(e)
        }
      }), this.controls.hoverCtrl.handlers.feature.stopDown = !1,
        this.map.addControl(this.controls.hoverCtrl), this.controls.hoverCtrl.activate()
    },
    updateItem: function (e) {
      var t = this.toNativeStyle(e.getStyle()), o = e.getFeatureType(), n = "marker" === o ? "markers" : "shapes", i = this.layers[n], r = i.getFeaturesByAttribute("id", e.get("id"))[0];
      r && (r.style = t, r.layer.drawFeature(r, t))
    }, tileLayer: function (e) {
      var o = this._getTileServiceURL(e), i = t.extend({
        transitionEffect: "resize"
      }, this.tileServicesOptions[e] || {});
      return new n.Layer.XYZ(e, this._switchUrl(o), t.extend({}, i));
    }, registerViewportEvents: function () {
      function e(e) {
        var t = this.map.getProjectionObject(), o = new n.Projection("EPSG:4326"), i = function (e) {
          var n;
          if (e) {
            var i = e.clone().transform(t, o);
            n = {latitude: i.lat, longitude: i.lon}
          } else n = {
            latitude: void 0, longitude: void 0
          };
          return n
        }, r = e.object.getExtent(), a = {
          northEast: {},
          southWest: {}
        };
        if (r) {
          var s = r.transform(t, o);
          a = {
            northEast: {
              latitude: s.top, longitude: s.right
            }, southWest: {latitude: s.bottom, longitude: s.left}
          }
        }
        var l = {
          zoomLevel: e.object.getZoom(),
          center: i(e.object.center), viewport: a, raw: e
        };
        return l
      }

      var o = this, i = {
        zoomend: "map:zoom",
        movestart: "map:center"
      };
      t.each(i, function (t, n) {
        o.map.events.register(n, o.map, function (n) {
          var i = e.call(o, n);
          o.trigger(t, i)
        })
      })
    }
  });
  return l
}), define("cde/components/Map/engines/google/MapComponentAsyncLoader", ["cdf/lib/jquery"], function (e) {
  var t = function (e) {
    var t, o = e.now();
    return function (n, i) {
      if (t)return t;
      var r, a = e.Deferred(), s = function () {
        a.resolve(window.google && google.maps ? google.maps : !1)
      }, l = "loadGoogleMaps_" + o++;
      return window.google && google.maps ? s() : window.google && google.load ? google.load("maps", n || 3, {
        callback: s
      }) : (r = e.extend({v: n || 3, callback: l}, i ? {key: i} : {}), window[l] = function () {
        s(),
          setTimeout(function () {
            try {
              delete window[l]
            } catch (e) {
            }
          }, 20)
      }, e.ajax({
        dataType: "script",
        data: r, url: "http://maps.googleapis.com/maps/api/js"
      })), t = a.promise()
    }
  }(e);
  return t;
}), define("cde/components/Map/engines/google/MapEngineGoogle", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "../MapEngine", "./MapComponentAsyncLoader", "../../model/MapModel", "css!./styleGoogle"], function (e, t, o, n, i) {
  function r(e, t, o, n, i, r, a) {
    this.startPoint_ = e, this.width_ = t, this.height_ = o, this.map_ = r,
      this.htmlContent_ = n, this.popupContentDiv_ = i, this.borderColor_ = a, this.div_ = null, this.setMap(r);
  }

  function a(e, t) {
    t.setSelection(t.getSelection() === s.ALL ? s.NONE : s.ALL)
  }

  var s = i.SelectionStates, l = o.extend({
    map: void 0, centered: !1, overlays: [], API_KEY: !1, selectedFeature: void 0, constructor: function (t) {
      this.base(), e.extend(this, t), this.controls = {}, this.controls.listenersHandle = {}
    }, init: function () {
      return e.when(n("3", this.API_KEY)).then(function (t) {
        r.prototype = new google.maps.OverlayView,
          r.prototype.onAdd = function () {
            var t = document.createElement("DIV");
            t.id = "MapOverlay",
              t.style.position = "absolute", this.borderColor_ ? t.style.border = "3px solid " + this.borderColor_ : t.style.border = "none";
            var o = this, n = e('<div id="MapOverlay_close" class="olPopupCloseBox" style="position: absolute;"></div>');
            n.click(function () {
              o.setMap(null)
            }), e(t).append(n), this.popupContentDiv_ && this.popupContentDiv_.length > 0 ? e(t).append(e("#" + this.popupContentDiv_)) : t.innerHTML = this.htmlContent_,
              this.div_ = t;
            var i = this.getPanes();
            i.overlayLayer.appendChild(t)
          }, r.prototype.draw = function () {
          var e = this.getProjection(), t = e.fromLatLngToDivPixel(this.startPoint_), o = this.div_;
          o.style.left = t.x + "px", o.style.top = t.y + 30 + "px", o.style.width = this.width_ + "px", o.style.height = this.height_ + "px";
        }, r.prototype.onRemove = function () {
          this.popupContentDiv_ && (e("#" + this.popupContentDiv_).append(e(this.div_)),
            e(this.div_).detach()), this.div_.style.display = "none", this.div_.parentNode.removeChild(this.div_),
            this.div_ = null
        }
      })
    }, wrapEvent: function (o, n) {
      var i = this, r = o.feature.getProperty("model");
      return e.extend(this._wrapEvent(r), {
        latitude: o.latLng.lat(), longitude: o.latLng.lng(),
        _popup: function (t, o) {
          var n = e.extend({width: 100, height: 100}, o || {});
          i.showPopup(null, feature, n.height, n.width, t, null, null);
        }, feature: o.feature, mapEngineType: "google3", draw: function (e) {
          var o = i.toNativeStyle(e);
          feature.setOptions(o), feature.setVisible(!1), feature.setVisible(t.has(e, "visible") ? !!e.visible : !0);
        }, setSelectedStyle: function (e) {
          feature.selStyle = e
        }, getSelectedStyle: function () {
          return feature.selStyle;
        }, isSelected: function () {
          return i.selectedFeature && i.selectedFeature[0] === data.key;
        }, raw: o
      })
    }, toNativeStyle: function (e, o) {
      var n = {
        fill: "fillColor", "fill-opacity": "fillOpacity",
        stroke: "strokeColor", "stroke-opacity": "strokeOpacity", "stroke-width": "strokeWeight",
        r: "scale", "z-index": "zIndex", fillColor: "fillColor", fillOpacity: "fillOpacity", strokeColor: "strokeColor",
        strokeOpacity: "strokeOpacity", strokeWidth: "strokeWeight", zIndex: "zIndex"
      }, i = {};
      return t.each(e, function (o, r) {
        var a = n[r];
        if (a)i[a] = o; else switch (r) {
          case"visible":
            i.display = o ? !0 : "none";
            break;
          case"icon-url":
            i.icon = o, i.size = new google.maps.Size(e.width, e.height);
            break;
          case"symbol":
            var s = {
              circle: google.maps.SymbolPath.CIRCLE
            }, l = s[o];
            i.path = t.isUndefined(l) ? o : l;
            break;
          default:
            i[r] = o
        }
      }), o && "marker" === o.getFeatureType() && (i.icon || (i = {icon: i})), i
    }, updateItem: function (e) {
      var t = e.get("id"), o = this.map.data.getFeatureById(t), n = this.toNativeStyle(e.getStyle(), e);
      this.map.data.overrideStyle(o, n)
    }, renderMap: function (e) {
      var t = {
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        disableDefaultUI: !0
      };
      this.map = new google.maps.Map(e, t), this.addLayers(), this.addControls(),
        this.registerViewportEvents()
    }, zoomExtends: function () {
      var e = new google.maps.LatLngBounds;
      return this.map.data.forEach(function (t) {
        "Point" == t.getGeometry().getType() && e.extend(t.getGeometry().get());
      }), e.isEmpty() ? !1 : (this.map.setCenter(e.getCenter()), this.map.fitBounds(e), !0)
    }, renderItem: function (o) {
      if (o) {
        var n = o.get("geoJSON"), i = this;
        e.when(n).then(function (n) {
          if (n) {
            e.extend(!0, n, {
              properties: {id: o.get("id"), model: o}
            });
            var r = i.map.data.addGeoJson(n, {
              idPropertyName: "id"
            });
            t.each(r, function (e) {
              var t = i.toNativeStyle(o.getStyle(), o);
              i.map.data.overrideStyle(e, t);
            })
          }
        })
      }
    }, addControls: function () {
      this._addControlHover(), this._addControlZoomBox(),
        this._addControlBoxSelector(), this._addLimitZoomLimits()
    }, _removelistenersHandle: function () {
      t.each(this.controls.listenersHandle, function (e) {
        e.remove()
      })
    }, _addControlHover: function () {
      function e(e, t) {
        var o = e.feature.getProperty("model");
        o.setHover("hover" === t)
      }

      this.map.data.addListener("mouseover", function (t) {
        e(t, "hover")
      }), this.map.data.addListener("mouseout", function (t) {
        e(t, "normal")
      })
    },
    _addControlZoomBox: function () {
      this.controls.zoomBox = {
        bounds: null, gribBoundingBox: null,
        mouseIsDown: !1
      }
    }, _addControlBoxSelector: function () {
      this.controls.boxSelector = {
        bounds: null,
        gribBoundingBox: null, mouseIsDown: !1
      }
    }, _addControlClick: function () {
      var e = this;
      this.map.data.addListener("click", function (t) {
        var o = event.feature.getProperty("model").getFeatureType();
        e.trigger(o + ":click", e.wrapEvent(t)),
          e.trigger("engine:selection:complete")
      })
    }, _addLimitZoomLimits: function () {
      var e = this, o = t.isFinite(e.options.viewport.zoomLevel.min) ? e.options.viewport.zoomLevel.min : 0, n = t.isFinite(e.options.viewport.zoomLevel.max) ? e.options.viewport.zoomLevel.max : null;
      google.maps.event.addListener(e.map, "zoom_changed", function () {
        e.map.getZoom() < o ? e.map.setZoom(o) : !t.isNull(n) && e.map.getZoom() > n && e.map.setZoom(n);
      })
    }, zoomIn: function () {
      this.map.setZoom(this.map.getZoom() + 1)
    }, zoomOut: function () {
      this.map.setZoom(this.map.getZoom() - 1)
    }, setPanningMode: function () {
      this._removelistenersHandle();
      var e = this;
      e.controls.listenersHandle.click = this._toggleOnClick()
    }, setZoomBoxMode: function () {
      this._removelistenersHandle();
      var e = this;
      e.controls.listenersHandle.click = this._toggleOnClick(),
        e.controls.listenersHandle.mousedown = google.maps.event.addListener(this.map, "mousedown", function (t) {
          e.model.isZoomBoxMode() && e._beginBox(e.controls.zoomBox, t)
        }), e.controls.listenersHandle.mousemove = google.maps.event.addListener(this.map, "mousemove", function (t) {
        var o = e.controls.zoomBox;
        e.model.isZoomBoxMode() && o.mouseIsDown && e._onBoxResize(o, t);
      }), e.controls.listenersHandle.mouseup = google.maps.event.addListener(this.map, "mouseup", function (t) {
        if (e.model.isZoomBoxMode() && e.controls.zoomBox.mouseIsDown) {
          e.controls.zoomBox.mouseIsDown = !1,
            e.controls.zoomBox.mouseUpPos = t.latLng;
          var o = e.controls.zoomBox.gribBoundingBox.getBounds(), n = new google.maps.LatLngBounds(o.getSouthWest(), o.getNorthEast());
          e.map.fitBounds(n), e.controls.zoomBox.gribBoundingBox.setMap(null), e.controls.zoomBox.gribBoundingBox = null,
            e.map.setOptions({draggable: !0})
        }
      })
    }, setSelectionMode: function () {
      this._removelistenersHandle();
      var e = this, o = e.controls.boxSelector;
      this.controls.listenersHandle.click = this._toggleOnClick(),
        this.controls.listenersHandle.mousedown = google.maps.event.addListener(this.map, "mousedown", function (t) {
          e.model.isSelectionMode() && e._beginBox(e.controls.boxSelector, t)
        }), e.controls.listenersHandle.mousemove = google.maps.event.addListener(this.map, "mousemove", function (t) {
        var o = e.controls.boxSelector;
        e.model.isSelectionMode() && o.mouseIsDown && e._onBoxResize(o, t);
      }), e.controls.listenersHandle.mouseup = google.maps.event.addListener(this.map, "mouseup", function (n) {
        e.model.isSelectionMode() && o.mouseIsDown && (o.mouseIsDown = !1, o.mouseUpPos = n.latLng,
          e.model.leafs().each(function (n) {
            var i = n.get("id");
            void 0 != e.map.data.getFeatureById(i) && e.map.data.getFeatureById(i).toGeoJson(function (i) {
              var r = i.geometry, s = !1, l = o.gribBoundingBox.getBounds();
              if ("MultiPolygon" == r.type)s = t.some(r.coordinates, function (e) {
                return t.some(e, function (e) {
                  return t.some(e, function (e) {
                    var t = new google.maps.LatLng(e[1], e[0]);
                    return l.contains(t)
                  })
                })
              }); else if ("Point" == r.type) {
                var c = new google.maps.LatLng(r.coordinates[1], r.coordinates[0]);
                s = l.contains(c)
              }
              s && a(e, n)
            })
          }), e.trigger("engine:selection:complete"), o.gribBoundingBox.setMap(null),
          o.gribBoundingBox = null, e.map.setOptions({draggable: !0}))
      })
    }, _toggleOnClick: function () {
      var e = this;
      return this.map.data.addListener("click", function (t) {
        var o = t.feature.getProperty("model");
        a(e, o), e.trigger("engine:selection:complete");
        var n = o.getFeatureType();
        e.trigger(n + ":click", e.wrapEvent(t));
      })
    }, _beginBox: function (e, t) {
      e.mouseIsDown = !0, e.mouseDownPos = t.latLng, this.map.setOptions({
        draggable: !1
      })
    }, _onBoxResize: function (e, t) {
      if (null !== e.gribBoundingBox) {
        var o = new google.maps.LatLngBounds(e.mouseDownPos, null);
        o.extend(t.latLng), e.gribBoundingBox.setBounds(o)
      } else e.gribBoundingBox = new google.maps.Rectangle({
        map: this.map, fillOpacity: .15, strokeWeight: .9, clickable: !1
      })
    }, unselectPrevShape: function (e, o, n) {
      var i = this, r = this.selectedFeature;
      if (r && r[0] !== e) {
        var a = r[1], s = r[2];
        t.each(a, function (e) {
          var o = i.toNativeStyle(s);
          e.setOptions(o), e.setVisible(!1), e.setVisible(t.has(s, "visible") ? !!s.visible : !0);
        })
      }
      this.selectedFeature = [e, o, n]
    }, addLayers: function () {
      for (var e = [], o = [], n = [], i = 0; i < this.tilesets.length; i++) {
        var r = this.tilesets[i].slice(0);
        o.push(r), n.push({mapTypeId: r}), this.tileServices[r] ? e.push(this.tileLayer(r)) : e.push("");
      }
      for (i = 0; i < e.length; i++)t.isEmpty(e[i]) || (this.map.mapTypes.set(o[i], e[i]), this.map.setMapTypeId(o[i]),
        this.map.setOptions(n[i]))
    }, updateViewport: function (e, t, o) {
      o || (o = 2), this.map.setZoom(o);
      this.zoomExtends() || this.map.panTo(new google.maps.LatLng(38, -9))
    }, tileLayer: function (e) {
      var o = t.extend({
        tileSize: new google.maps.Size(256, 256),
        minZoom: 1,
        maxZoom: 19
      }, this.tileServicesOptions[e] || {}), n = this._switchUrl(this._getTileServiceURL(e)), i = this;
      return new google.maps.ImageMapType(t.defaults({
        name: e.indexOf("/") >= 0 ? "custom" : e,
        getTileUrl: function (e, o) {
          var r = Math.pow(2, o);
          if (e.y < 0 || e.y >= r)return "404.png";
          e.x = (e.x % r + r) % r;
          var a;
          if (t.isArray(n)) {
            var s = t.template("${z}/${x}/${y}", {x: e.x, y: e.y, z: o}, {
              interpolate: /\$\{(.+?)\}/g
            });
            a = i._selectUrl(s, n)
          } else a = n;
          return t.template(a, {x: e.x, y: e.y, z: o}, {
            interpolate: /\$\{(.+?)\}/g
          })
        }
      }, o))
    }, showPopup0: function (o, n, i, a, s, l, c) {
      l && l.length > 0 && (s = e("#" + l).html());
      var p = new r(n.getGeometry().get(), a, i, s, l, this.map, c);
      this._popups = this._popups || [], t.each(this._popups, function (e) {
        e.setMap(null)
      }), this._popups.push(p);
    }, showPopup: function (e, o, n, i, r, a, s) {
      var l = new google.maps.InfoWindow({
        content: r, position: o.getGeometry().get(),
        maxWidth: i
      });
      this._popups = this._popups || [], t.each(this._popups, function (e) {
        e.close();
      }), l.open(this.map), this._popups.push(l)
    }, registerViewportEvents: function () {
      function e() {
        function e(e) {
          var t = {latitude: e.lat(), longitude: e.lng()};
          return t
        }

        function t(t) {
          o = t ? {
            northEast: e(t.getNorthEast()), southWest: e(t.getSouthWest())
          } : {
            northEast: {}, southWest: {}
          }
        }

        var o = t(this.map.getBounds()), n = {
          zoomLevel: this.map.getZoom(), center: e(this.map.getCenter() || new google.maps.LatLng),
          viewport: o, raw: this.map
        };
        return n
      }

      var o = this, n = {
        zoom_changed: "map:zoom", center_changed: "map:center"
      };
      t.each(n, function (t, n) {
        google.maps.event.addListener(o.map, n, function () {
          var n = e.call(o);
          o.trigger(t, n)
        })
      })
    }
  });
  return l
}), define("cde/components/Map/addIns/LocationResolver/geonames/geonames", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/lib/jquery"], function (e, t, o) {
  var n = {
    name: "geonames", label: "GeoNames", defaults: {
      username: "", url: "http://ws.geonames.org/searchJSON"
    }, implementation: function (e, t, n) {
      var i, r, a = t.address;
      a || (t.city ? (a = t.city, r = "P") : t.county ? (a = t.county,
        r = "A") : t.region ? (a = t.region, r = "A") : t.state ? (a = t.state, r = "A") : t.country && (a = t.country,
        r = "A"));
      var s = {
        q: a.replace(/&/g, ","), maxRows: 1, dataType: "json", username: n.username,
        featureClass: r
      };
      r && (s.featureClass = r);
      var l = function (e) {
        e.geonames && e.geonames.length > 0 && (i = [parseFloat(e.geonames[0].lng), parseFloat(e.geonames[0].lat)],
          t.continuationFunction(i))
      }, c = function () {
        t.continuationFunction(void 0)
      };
      return o.ajax({
        dataType: "json", url: n.url, method: "GET", data: s, success: l, error: c
      })
    }
  };
  return t.registerGlobalAddIn("NewMapComponent", "LocationResolver", new e(n)),
    n
}), define("cde/components/Map/addIns/LocationResolver/nominatim/nominatim", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t, o, n) {
  var i = {
    name: "openstreetmap", label: "OpenStreetMap", defaults: {
      url: "http://nominatim.openstreetmap.org/search",
      serviceParams: {format: "json", limit: "1"}, mapping: {
        street: "street", postalcode: "postalcode",
        city: "city", county: "county", state: "state", country: "country"
      }
    }, implementation: function (e, t, i) {
      if (t.latitude || t.longitude) {
        var r = [parseFloat(t.longitude), parseFloat(t.latitude)];
        return void t.continuationFunction(r)
      }
      var a = o.extend(!0, {}, i.serviceParams);
      n.each(n.keys(t), function (e) {
        if (!n.isFunction(t[e])) {
          var o = e.toLowerCase();
          o in i.mapping && (a[i.mapping[o]] = t[e]);
        }
      }), a.q && (a = {
        q: a.q + ", " + n.compact(n.map(i.mapping, function (e) {
          return a[e]
        })).join(", ")
      });
      var s = function (e) {
        if (e && e.length > 0) {
          var o = [parseFloat(e[0].lon), parseFloat(e[0].lat)];
          t.continuationFunction(o)
        }
      }, l = function () {
        t.continuationFunction(void 0)
      };
      return o.ajax({
        dataType: "json", method: "GET", url: i.url, data: o.extend({}, i.serviceParams, a), success: s,
        error: l
      })
    }
  };
  return t.registerGlobalAddIn("NewMapComponent", "LocationResolver", new e(i)),
    i
}), define("cde/components/Map/addIns/LocationResolver/mapquest/mapquest", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/lib/jquery", "amd!cdf/lib/underscore", "../nominatim/nominatim"], function (e, t, o, n, i) {
  var r = o.extend(!0, {}, i, {
    name: "mapquest", label: "MapQuest", defaults: {
      url: "http://open.mapquestapi.com/nominatim/v1/search"
    }
  });
  return t.registerGlobalAddIn("NewMapComponent", "LocationResolver", new e(r)), r;
}), define("cde/components/Map/addIns/MarkerImage/cggMarker/cggMarker", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/components/CggComponent.ext"], function (e, t, o) {
  var n = new e({
    name: "cggMarker", label: "CGG Marker", defaults: {}, implementation: function (e, n, i) {
      var r = o.getCggDrawUrl() + "?script=" + n.cggGraphName, a = {};
      n.width && (a.width = n.width),
      n.height && (a.height = n.height), a.noChartBg = !0;
      var s;
      for (s in n.parameters)a[s] = n.parameters[s];
      var l = t.debug;
      l > 1 && (a.debug = !0, a.debugLevel = l);
      for (s in a)void 0 !== a[s] && (r += "&param" + s + "=" + encodeURIComponent(a[s]));
      return r
    }
  });
  return t.registerGlobalAddIn("NewMapComponent", "MarkerImage", n), n
}), define("cde/components/Map/Map.ext", [], function () {
  var e = {
    getMarkerImgPath: function () {
      return CONTEXT_PATH + "api/repos/pentaho-cdf-dd/resources/custom/components/Map/images/";
    }
  };
  return e
}), define("cde/components/Map/addIns/MarkerImage/urlMarker/urlMarker", ["cdf/AddIn", "cdf/Dashboard.Clean", "../../../Map.ext"], function (e, t, o) {
  var n = new e({
    name: "urlMarker", label: "Url Marker", defaults: {
      defaultUrl: o.getMarkerImgPath() + "marker_grey.png",
      imagePath: o.getMarkerImgPath(),
      images: ["marker_grey.png", "marker_blue.png", "marker_grey02.png", "marker_orange.png", "marker_purple.png"]
    }, implementation: function (e, t, o) {
      return t.url ? t.url : t.position ? o.imagePath + o.images[t.position % o.images.length] || o.defaultUrl : o.defaultUrl;
    }
  });
  return t.registerGlobalAddIn("NewMapComponent", "MarkerImage", n), n
}), define("cde/components/Map/addIns/ShapeResolver/simpleJSON", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t, o, n) {
  function i(e) {
    var t = n.map(e, function (e) {
      return n.map(e, function (e) {
        return n.map(e, function (e) {
          return e.reverse()
        })
      })
    }), o = {
      type: "Feature", geometry: {
        type: "MultiPolygon", coordinates: t
      }, properties: {}
    };
    return o
  }

  var r = {
    name: "simpleJSON", label: "Simple JSON shape resolver",
    defaults: {url: ""}, implementation: function (e, t, r) {
      var a = o.Deferred(), s = r.url || t._shapeSource;
      return s ? o.ajax(s, {
        async: !0, type: "GET", dataType: "json", success: function (e) {
          var t = n.chain(e).map(function (e, t) {
            return [t, i(e)]
          }).object().value();
          a.resolve(t)
        }, error: function () {
          a.resolve({})
        }
      }) : a.resolve(null),
        a.promise()
    }
  };
  t.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new e(r))
}),
  define("cde/components/Map/addIns/ShapeResolver/kml", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t, o, n) {
    function i(e, t, i) {
      var a = {};
      return o(e).find("Placemark").each(function (e, s) {
        var l;
        if (n.isFunction(i))try {
          l = i(s)
        } catch (c) {
          l = o(s).find(t).text()
        } else l = o(s).find(t).text();
        var p = n.map(o(s).find("Polygon"), function (e) {
          var t = [];
          return n.each(["outerBoundaryIs", "innerBoundaryIs"], function (i) {
            var r = o(e).find(i + " LinearRing coordinates");
            n.each(r, function (e) {
              var i = o(e).text().trim();
              if (i.length > 0) {
                var r = n.map(i.split(" "), function (e) {
                  return n.map(e.split(",").slice(0, 2), parseFloat);
                });
                t.push(r)
              }
            })
          }), t
        });
        n.isEmpty(p) || a[l] || (a[l] = r(p))
      }), a
    }

    function r(e) {
      var t = {
        type: "Feature",
        geometry: {type: "MultiPolygon", coordinates: e}, properties: {}
      };
      return t
    }

    var a = {
      name: "kml",
      label: "KML shape resolver", defaults: {url: "", idSelector: "name", parseShapeKey: null},
      implementation: function (e, t, n) {
        var r = o.Deferred(), a = n.url || t._shapeSource, s = n.parseShapeKey || t._parseShapeKey;
        return a ? o.ajax(a, {
          async: !0, type: "GET", processData: !1, success: function (e) {
            var t = i(e, n.idSelector, s);
            r.resolve(t)
          }, error: function () {
            r.resolve({})
          }
        }) : r.resolve(null), r.promise()
      }
    };
    t.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new e(a));
  }), define("cde/components/Map/addIns/ShapeResolver/geoJSON", ["cdf/AddIn", "cdf/Dashboard.Clean", "cdf/Logger", "cdf/lib/jquery", "amd!cdf/lib/underscore"], function (e, t, o, n, i) {
  function r(e, t) {
    var o = i.chain(e.features).map(function (e, o) {
      var n = a(e, t) || o;
      return [n, e];
    }).object().value();
    return o
  }

  function a(e, t) {
    var o = e.id;
    return t && (o = e.properties[t] || o),
      o
  }

  var s = {
    name: "geoJSON", label: "GeoJSON shape resolver", defaults: {
      url: "", idPropertyName: ""
    }, implementation: function (e, t, i) {
      var a = n.Deferred(), s = i.url || t._shapeSource;
      return s ? n.ajax(s, {
        async: !0, type: "GET", dataType: "json", success: function (e) {
          var t = r(e, i.idPropertyName);
          a.resolve(t)
        }, error: function () {
          o.log("NewMapComponent geoJSON addIn: failed to retrieve data at" + s, "debug"),
            a.resolve({})
        }
      }) : (o.log("NewMapComponent geoJSON addIn: no url is defined", "debug"),
        a.resolve(null)), a.promise()
    }
  };
  t.registerGlobalAddIn("NewMapComponent", "ShapeResolver", new e(s));
}), define("cde/components/Map/addIns/mapAddIns", ["./LocationResolver/geonames/geonames", "./LocationResolver/nominatim/nominatim", "./LocationResolver/mapquest/mapquest", "./MarkerImage/cggMarker/cggMarker", "./MarkerImage/urlMarker/urlMarker", "./ShapeResolver/simpleJSON", "./ShapeResolver/kml", "./ShapeResolver/geoJSON"], function () {
}),
  define("cde/components/Map/Map", ["cdf/lib/jquery", "amd!cdf/lib/underscore", "cdf/components/UnmanagedComponent", "./Map.lifecycle", "./Map.selector", "./Map.model", "./Map.configuration", "./Map.featureStyles", "./Map.colorMap", "./ControlPanel/ControlPanel", "./Map.tileServices", "./engines/openlayers2/MapEngineOpenLayers", "./engines/google/MapEngineGoogle", "./addIns/mapAddIns", "css!./Map"], function (e, t, o, n, i, r, a, s, l, c, p, d, u) {
    var h = o.extend(n).extend(i).extend(r).extend(a).extend(s).extend(l).extend(p).extend({
      mapEngine: void 0, locationResolver: void 0, API_KEY: !1, update: function () {
        return this.preExec() ? (this.maybeToggleBlock(!0),
          this.configuration = this.getConfiguration(), void this._initMapEngine().then(t.bind(this.init, this)).then(t.bind(function () {
          this.queryDefinition && !t.isEmpty(this.queryDefinition) ? this.getQueryData() : this.onDataReady(this.testData || {});
        }, this))) : !1
      }, onDataReady: function (o) {
        return e.when(this.resolveFeatures(o)).then(t.bind(function (e) {
          this.initModel(e), this._initControlPanel(), this.updateSelection(), this._processMarkerImages();
        }, this)).then(t.bind(this.render, this)).then(t.bind(this._concludeUpdate, this))
      },
      _initMapEngine: function () {
        var t = e.extend(!0, {}, this.configuration.addIns.MapEngine.options, {
          options: this.configuration
        });
        return "google" == this.configuration.addIns.MapEngine.name ? this.mapEngine = new u(t) : this.mapEngine = new d(t),
          this.mapEngine.init()
      }, init: function () {
        var t = e('<div class="map-container"/>');
        t.css({
          position: "relative", overflow: "hidden", width: "100%", height: "100%"
        }), t.appendTo(this.placeholder().empty()),
          this._relayMapEngineEvents(), this._registerEvents(), this.mapEngine.renderMap(t.get(0)),
          this._initPopup()
      }, _initControlPanel: function () {
        var o = e('<div class="map-controls" />').prependTo(this.placeholder());
        this.controlPanel = new c(o, this.model, this.configuration), this.controlPanel.render();
        var n = this, i = {
          "zoom:in": t.bind(this.mapEngine.zoomIn, this.mapEngine),
          "zoom:out": t.bind(this.mapEngine.zoomOut, this.mapEngine)
        };
        t.each(i, function (e, o) {
          t.isFunction(e) && n.listenTo(n.controlPanel, o, e)
        })
      }, render: function () {
        this.mapEngine.render(this.model);
        var e = this.configuration.viewport.center.latitude, t = this.configuration.viewport.center.longitude, o = this.configuration.viewport.zoomLevel["default"];
        this.mapEngine.updateViewport(t, e, o)
      }, _relayMapEngineEvents: function () {
        var e = this.mapEngine, o = this, n = ["marker:click", "marker:mouseover", "marker:mouseout", "shape:click", "shape:mouseover", "shape:mouseout", "map:zoom", "map:center"];
        t.each(n, function (n) {
          o.listenTo(e, n, function () {
            var e = t.union([n], arguments);
            o.trigger.apply(o, e);
          })
        }), this.listenTo(this.mapEngine, "engine:selection:complete", function () {
          o.processChange();
        })
      }, _registerEvents: function () {
        var e = this;
        this.on("marker:click", function (o) {
          this.model.isPanningMode();
          var n;
          t.isFunction(e.markerClickFunction) && (n = e.markerClickFunction(o)), n !== !1 && e.model.isPanningMode() && t.isEmpty(this.parameter) && e.showPopup(o);
        }), this.on("shape:mouseover", function (o) {
          if (t.isFunction(e.shapeMouseOver)) {
            var n = e.shapeMouseOver(o);
            n && (n = t.isObject(n) ? n : {}, o.draw(t.defaults(n, {"z-index": 1}, o.style)))
          }
        }), this.on("shape:mouseout", function (o) {
          var n = {};
          t.isFunction(e.shapeMouseOut) && (n = e.shapeMouseOut(o)), n = t.isObject(n) ? n : {},
          t.size(n) > 0 && o.draw(t.defaults(n, o.style))
        }), this.on("shape:click", function (o) {
          if (t.isFunction(e.shapeMouseClick)) {
            e.shapeMouseClick(o);
            return
          }
        })
      }, _processMarkerImages: function () {
        function o(o) {
          var n = this.mapping || {}, r = o.get("rawData") || [], a = e.extend(!0, {}, i, {
            data: r, position: o.get("rowIdx"), height: r[n.markerHeight], width: r[n.markerWidth]
          }), s = this.configuration.addIns.MarkerImage.name, l = {}, c = {};
          "cggMarker" === s && (l = {
            cggGraphName: this.configuration.addIns.MarkerImage.options.cggScript,
            parameters: t.object(t.map(this.configuration.addIns.MarkerImage.options.parameters, function (e) {
              return [e[0], r[n[e[1]]]]
            }))
          });
          var p = this.getAddIn("MarkerImage", s);
          if (p) {
            e.extend(!0, a, l);
            var d = e.extend(!0, {}, this.getAddInOptions("MarkerImage", p.getName()), c), u = p.call(this.placeholder(), a, d);
            t.isObject(u) ? e.extend(!0, o.attributes.styleMap, u) : e.extend(!0, o.attributes.styleMap, {
              width: a.width, height: a.height, "icon-url": u
            })
          }
        }

        var n = this.model.findWhere({
          id: "markers"
        });
        if (n) {
          var i = {
            height: this.configuration.addIns.MarkerImage.options.height,
            width: this.configuration.addIns.MarkerImage.options.width,
            url: this.configuration.addIns.MarkerImage.options.iconUrl
          };
          n.leafs().each(t.bind(o, this)).value();
        }
      }, _initPopup: function () {
        if (this.popupContentsDiv) {
          var t = e("#" + this.popupContentsDiv), o = t.clone();
          this.popupContentsDiv && 1 != t.length && this.placeholder().append(o.html("None"))
        }
      }, showPopup: function (o) {
        var n = o.data || [], i = this;
        if (this.popupContentsDiv || n[i.mapping.popupContents]) {
          t.each(this.popupParameters, function (e) {
            i.dashboard.fireChange(e[1], n[i.mapping[e[0].toLowerCase()]])
          });
          var r = n[i.mapping.popupContentsHeight] || this.popupHeight, a = n[i.mapping.popupContentsWidth] || this.popupWidth, s = n[i.mapping.popupContents] || e("#" + this.popupContentsDiv).html(), l = "#394246", c = t.isUndefined(n.marker) && !this.markerCggGraph && t.isUndefined(i.marker) && "urlMarker" === i.configuration.addIns.MarkerImage.name;
          if (c) {
            var p = ["#394246", "#11b4eb", "#7a879a", "#e35c15", "#674f73"];
            l = p[o.model.get("rowIdx") % p.length];
          }
          this.mapEngine.showPopup(o.data, o.feature, r, a, s, this.popupContentsDiv, l)
        }
      }
    });
    return h;
  });