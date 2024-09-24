/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
LoadRequests = {
  loadDashboard: function () {}
};

PreviewRequests = {
  
  status: false,

  previewDashboard: function(saveParams, _href) {
    
    if(saveParams.operation !== "saveas") {
      this.status = false;
      return;
    }
    if(saveParams.file.indexOf("_tmp.wcdf") === -1) {
      this.status = false;
      return;
    }
    if(_href.indexOf( "_tmp.wcdf" ) === -1) {
      this.status = false;
      return;
    }

    this.status = true;

  },

  getPreviewUrl: function( sol, path, file, style ) {
    return pluginUrl + "/renderer/render?" +
        "solution=" + sol + "&path="+path+"&file="+file+"&style="+style+"&bypassCache=true" +
        "&root=" + window.location.host;
  }
};

SaveRequests = {
  saveSettings: function() {},
  saveAsDashboard: function() {}
};

SolutionTreeRequests = {
  getExplorerFolderEndpoint: function() {}
};

pluginUrl = "/pentaho/plugin/pentaho-cdf-dd/api";

CDFDDFileName = "test.cdfde";

Dashboards = {
    i18nSupport: {
      prop: function(name) {
        return name;
      }
    },
    escapeHtml: function(input) {
      // using Negative Lookahead when replacing '&' to make sure we don't
      // double escape
      var escaped = input
      .replace(/&(?!amp;)(?!lt;)(?!gt;)(?!#34;)(?!#39;)/g,"&amp;")
      .replace(/</g,"&lt;")
      .replace(/>/g,"&gt;")
      .replace(/'/g,"&#39;")
      .replace(/"/g,"&#34;");
      return escaped;
    }
};

Mustache = {
  compile: function () {}
};


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
    if (cdfddLogEnabled && level <= cdfddLogLevel && typeof console !== 'undefined') {
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
