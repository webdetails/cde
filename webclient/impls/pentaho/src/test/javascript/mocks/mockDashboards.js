PreviewRequests = {

  status: false,

  previewDashboard: function( saveParams, _href ) {

    if( saveParams.operation != "saveas" ) {
      this.status = false;
      return;
    }
    if( saveParams.file.indexOf( "_tmp.wcdf" ) == -1 ) {
      this.status = false;
      return;
    }
    if( _href.indexOf( "_tmp.wcdf" ) == -1 ) {
      this.status = false;
      return;
    }

    this.status = true;

  },

  getPreviewUrl: function( sol, path, file, style ) {
    return pluginUrl+"/renderer/render?solution="+sol+"&path="+path+"&file="+file+"&style="+style+"&bypassCache=true&root="+window.location.host;
  }
};

webAppPath = "/pentaho";

pluginUrl = "/pentaho/plugin/pentaho-cdf-dd/api";

CDFDDFileName = "test.cdfde";

Dashboards = {};

Mustache = {
  compile: function () {}
}


Logger = Base.extend({

  ERROR: 0,
  WARN: 1,
  INFO: 2,
  DEBUG: 3,
  name: "",

  logDescription: ["ERROR", "WARN", "INFO", "DEBUG"],

  constructor: function(name) {
    this.name = name;
  },

  log: function(level, str) {
    if (cdfddLogEnabled && level <= cdfddLogLevel && typeof console != 'undefined') {
      console.log(" - [" + this.name + "] " + this.logDescription[level] + ": " + str);
    }
  },
  error: function(str) {
    this.log(this.ERROR, str);
  },
  warn: function(str) {
    this.log(this.WARN, str);
  },
  info: function(str) {
    this.log(this.INFO, str);
  },
  debug: function(str) {
    this.log(this.DEBUG, str);
  }

});
