var CggComponent = UnmanagedComponent.extend({

  ph: null,
  have_SVG: true,
  
  getScriptUrl: function() {
    return this.resourceFile;
  },

  getOutputType: function() {
    return this.have_SVG ? 'svg' : 'png';
  },

  detectSvg: function() {
    this.have_SVG = 
    !!( document.createElementNS && 
        document.createElementNS('http://www.w3.org/2000/svg', 'svg').createSVGRect);
  },

  update: function() {
    this.detectSvg();
    
    var url    = wd.helpers.cggHelper.getCggDrawUrl(),
        data   = this.processParams(),
        script = this.getScriptUrl(),
        myself = this,
        ph     = $('#' + this.htmlObject);
        
        
    if (this.have_SVG) {
      myself.triggerAjax({
          url: url,
          data: data,
          type: 'get'
        }, function(result) {
          try {
            // ideally we can just add the <svg> node to our document
            ph[0].appendChild(document.importNode(result.lastChild, true));
            ph.find("svg").width(myself.width).height(myself.height);
          } catch (e) {
            // In IE9, document.importNode doesn't work with mixed SVG and HTML, so we instead add the chart as an <object>
            var obj = myself.createObj(url, script, data,myself.width, myself.height);
            ph[0].innerHTML = arguments[2].responseText;
            ph.find("svg").width(myself.width).height(myself.height);
          }
        });
    } else {
      myself.synchronous(function() {
        ph.html('<img src="' + url + '?' + $.param(data) + '" width="' + myself.width + '" height="' + myself.height + '"/>');
      });
    }
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
    data.outputType = this.getOutputType();

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
      if(params.hasOwnProperty(p)) {
        pArray.push(escape(p) + '=' + escape(params[p]));
      }
    }
    objUrl += '&' + pArray.join('&');
    return objUrl;
  },

  createObj: function(url, script, data, width, height) {
    var obj = document.createElement('object');
    obj.setAttribute('type', 'image/svg+xml');
    obj.setAttribute('data', this.objectUrl(url, script, data));
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

  _processParametersCore: function(data) {
    data.paramvalue  = this.dashboard.getParameterValue(this.parameter);
    data.paramcolors = this.colors;
    data.paramscale  = this.intervals;
  }
});
