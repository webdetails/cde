/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

define([], function() {

  var ViewManagerComponentExt = {

    getViewsEndpoint: function(name) {
      if(name && name.length) {
        return CONTEXT_PATH + "plugin/pentaho-cdf/api/views/" + name;
      }
      return CONTEXT_PATH + "plugin/pentaho-cdf/api/views";
    },

    getUrl: function(solution, path, file, view) {
      return CONTEXT_PATH + "api/repos/"
        + ((solution ? solution : "") + (path ? path : "") + (file ? file : "")).replace(/\//g, ':').replace(/\+/g, "%20")
        + "/generatedContent?viewId=" + view.replace(/\+/g, "%20");
    }
  };

  return ViewManagerComponentExt;

});
