LoadRequests = {
  loadDashboard: function () {}
}


CDFDDFileName = "test.wcdf";

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
