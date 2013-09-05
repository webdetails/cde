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

  update : function() {
    var url = '../cgg/draw',
      data = this.processParams(),
      script = this.resourceFile,
      myself = this,
      ph = $('#' + myself.htmlObject);

    // If the browser doesn't support SVG natively, we need SVGWeb to be loaded.
    // Jamie Love's Protovis+SVGWeb handles this elegantly.
    pv.listenForPageLoad(function () {
      $.ajax({
        url: url,
        data: data,
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
        error:function(){
          // For some reason or another, IE8 fails to parse the SVG, and will throw us into the error handler.
          // If that's the case, we have to add the SVG as an <object>, in a way that SVGWeb can understand.
          ph.empty();
          var obj = myself.createObj(url,script,data,myself.width,myself.height,true);
          svgweb.appendChild(obj, ph[0]);
        }
      });
    });
  },

  /*
   * Transforms the CDF parameter array into a map we can feed to jQuery.ajax(), with
   */
  processParams: function() {
    var data = {};
    for (var i = 0; i < this.parameters.length; i ++) {
        var param = this.parameters[i];
        data["param" + param[0]] = Dashboards.getParameterValue(param[1]);
    }
    data.script = escape(this.resourceFile);
    data.outputType='svg';
    return data;
  },

  /*
   * Produces an URL to use in <object> tags
   */
  objectUrl: function(baseUrl, script, params) {

    var objUrl = baseUrl + '?',
      pArray = [];

    for (var p in params) {
      if (params.hasOwnProperty(p)){
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


CggDialComponent = CggComponent.extend({
  script: "system/pentaho-cdf-dd/resources/custom/components/cgg/charts/dial.js",

  processParams: function() {
    var data = {
      paramvalue: Dashboards.getParameterValue(this.parameter),
      paramcolors: this.colors,
      paramscale: this.intervals
    };
    data.script = escape(this.script);
    data.outputType='svg';
    return data;
  }

});
