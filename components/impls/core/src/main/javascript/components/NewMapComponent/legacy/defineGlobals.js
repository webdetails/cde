(function() {

  var myglobals = {
    "cdf/lib/jquery": $,
    "amd!cdf/lib/underscore": _,
    "amd!cdf/lib/backbone": Backbone,
    "cdf/AddIn": AddIn,
    "cdf/lib/mustache": Mustache,
    "cdf/lib/Base": Base,
    "cdf/lib/BaseEvents": typeof(BaseEvents) === "undefined" ? {} : BaseEvents,
    "cdf/components/UnmanagedComponent": UnmanagedComponent,
    "cdf/lib/BaseSelectionTree": typeof(TreeFilter) === "undefined" ? {} : TreeFilter.Models.SelectionTree,
    "cdf/lib/OpenLayers": typeof(OpenLayers) === "undefined" ? {} : OpenLayers,
    "css!./Map": "",
    "css!./styleGoogle": "",
    "css!./styleOpenLayers2": "",
    "css!./ControlPanel/ControlPanel": ""
  };

  CONTEXT_PATH = Dashboards.getWebAppPath() + "/";

  for (var p in myglobals) {
    define(p, function() {
      return myglobals[p];
    });
  }

  define("cdf/Dashboard.Clean", function() {
    Dashboards.registerGlobalAddIn = Dashboards.registerGlobalAddIn || Dashboards.registerAddIn;
    return Dashboards;
  });

  define("cdf/components/CggComponent.ext", [], function() {
    var CggComponentExt = {
      getCggDrawUrl: function() {
        return CONTEXT_PATH + "plugin/cgg/api/services/draw";
      }
    };
    return CggComponentExt;
  });

  define("cdf/Logger", function() {
    return {
      log: Dashboards.log,
      debug: Dashboards.log,
      error: Dashboards.log,
      warn: Dashboards.log
    };
  });

  define("text!./ControlPanel.html", [], function() {
    return '<div class="map-control-panel {{mode}}">\n    <div class="map-controls-zoom">\n        <div class="map-control-button map-control-zoom-in"></div>\n        <div class="map-control-button map-control-zoom-out"></div>\n        <div class="map-control-button map-control-zoombox"></div>\n    </div>\n    <div class="map-controls-mode">\n        {{#configuration.isSelector}}\n        <div class="map-control-button map-control-select"></div>\n        {{/configuration.isSelector}}\n        <div class="map-control-button map-control-pan"></div>\n    </div>\n</div>';
  });

}());

