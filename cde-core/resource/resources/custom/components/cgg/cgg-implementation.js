/**
 * Borrowed from Jamie Love's Protovis+SVGWeb
 *
 * Binds to the page ready event in a browser-agnostic
 * fashion (i.e. that works under IE!)
 */

if (typeof pv == 'undefined' || typeof pv.listenForPageLoad == 'undefined') {
  window.pv = {};
  pv.renderer = function() {
    return (typeof window.svgweb === "undefined") ? "nativesvg" : "svgweb";
  };
  pv.listenForPageLoad = function(listener) {
  
      // Catch cases where $(document).ready() is called after the
      // browser event has already occurred.
      if ( document.readyState === "complete" ) {
          listener();
      }
  
      if (pv.renderer() == "svgweb") {
          // SVG web adds addEventListener to IE.
          window.addEventListener( "SVGLoad", listener, false );
      } else {
          // Mozilla, Opera and webkit nightlies currently support this event
          if ( document.addEventListener ) {
              window.addEventListener( "load", listener, false );
  
          // If IE event model is used
          } else if ( document.attachEvent ) {
              window.attachEvent( "onload", listener );
          }
      }
  };
}

var CggComponent = BaseComponent.extend({

  ph: null,
  relComp: null,
  
  getScriptUrl: function() {
    return this.resourceFile;
  },

  getOutputType: function() {
    return 'svg';
  },

  update: function() {
    var url    = wd.helpers.cggHelper.getCggDrawUrl(),
        data   = this.processParams(),
        script = this.getScriptUrl(),
        myself = this,
        ph     = $('#' + myself.htmlObject);

    // TODO: integrate with CDF's async?

    // If the browser doesn't support SVG natively, we need SVGWeb to be loaded.
    // Jamie Love's Protovis+SVGWeb handles this elegantly.
    pv.listenForPageLoad(function () {
      $.ajax({
        url:  url,
        data: data,
        type: 'get', // CGG requires GET.
        // IE9 and "decent" browsers will succeed this call, so the success handler deals with those cases.
        success: function (xmlData) {
          ph.empty();
          try {
            // ideally we can just add the <svg> node to our document
            ph[0].appendChild(document.importNode(xmlData.lastChild, true));
            ph.find("svg").width(myself.width).height(myself.height);
          } catch (e) {
            // In IE9, document.importNode doesn't work with mixed SVG and HTML, so we instead add the chart as an <object>
            var obj = myself.createObj(url,script,data,myself.width,myself.height);
            ph[0].innerHTML = arguments[2].responseText;
            ph.find("svg").width(myself.width).height(myself.height);
          }
        },
        error:function() {
          // For some reason or another, IE8 fails to parse the SVG, and will throw us into the error handler.
          // If that's the case, we have to add the SVG as an <object>, in a way that SVGWeb can understand.
          ph.empty();
          if(pv.renderer() === "svgweb") {
            var obj = myself.createObj(url,script,data,myself.width,myself.height,true);
            svgweb.appendChild(obj, ph[0]);
          }
        }
      });
    });
  },

  /*
   * Transforms the CDF parameter array into a map we can feed to jQuery.ajax(), with
   */
  processParams: function() {
    var data = {};

    this._processParametersCore(data);

    // Check debug level and pass as parameter
    var level = this.dashboard.debug;
    if(level > 1) {
        data.paramdebug = true;
        data.paramdebugLevel = level;
    }

    data.script     = escape(this.getScriptUrl());
    data.outputType = this.getOutputType() || 'svg';

    return data;
  },

  _processParametersCore: function(data) {
    var dash = this.dashboard;
    var params = this.parameters;
    for (var i = 0, L = params.length ; i < L ; i ++) {
        var param = params[i];
        var value = dash.getParameterValue(param[1]);

        if($.isArray(value) && value.length == 1 && ('' + value[0]).indexOf(';') >= 0) {
            // Special case where single element will wrongly be treated as a parseable array by cda
            value = doCsvQuoting(value[0],';');
        }

        data["param" + param[0]] = value;
    }
  },

  /*
   * Produces an URL to use in <object> tags
   */
  objectUrl: function(baseUrl, script, params) {
    var objUrl = baseUrl + '?',
        pArray = [];

    for(var p in params) {
      if(params.hasOwnProperty(p)){
        pArray.push(escape(p) + '=' + escape(params[p]));
      }
    }
    objUrl += '&' + pArray.join('&');
    return objUrl;
  },

  createObj: function(url,script,data,width,height,svgweb) {
    var obj = (svgweb
      ? document.createElement('object', true)
      : document.createElement('object'));
    obj.setAttribute('type', 'image/svg+xml');
    obj.setAttribute('data', this.objectUrl(url,script,data));
    obj.setAttribute('width', width);
    obj.setAttribute('height', height);
    return obj;
  }
});


var CggDialComponent = CggComponent.extend({
  script: "system/pentaho-cdf-dd/resources/custom/components/cgg/charts/dial.js",
  
  getScriptUrl: function() {
    return this.script;
  },

  getOutputType: function() {
    return 'svg';
  },

  _processParametersCore: function(data) {
    data.paramvalue  = this.dashboard.getParameterValue(this.parameter);
    data.paramcolors = this.colors;
    data.paramscale  = this.intervals;
  }
});
