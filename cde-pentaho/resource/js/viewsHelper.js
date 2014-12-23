/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
*/

wd = wd || {};
wd.helpers = wd.helpers || {};

wd.helpers.views = {
  getViewsEndpoint: function() {
    return "/content/pentaho-cdf/Views";
  },
  getSaveViewsEndpoint: function() {
    return getViewsEndpoint() + "?method=saveView";
  },
  getDeleteViewsEndpoint: function() {
    return getViewsEndpoint() + "?method=deleteView";
  },
  getListViewsEndpoint: function() {
    return getViewsEndpoint() + "?method=listViews";
  },
  getUrl: function (solution, file, path, view) {
    /* $.params uses + to encode spaces, we need
     * to use uri encoding-style "%20" instead
     */
    var params = $.param({
      file: this.file,
      path: this.path,
      solution: this.solution,
      view: this.name
    }).replace(/\+/g, "%20");

    if (/\.(wcdf|cdfde)$/.test(this.file)) {
      /* CDE mode*/
      return webAppPath + "/content/pentaho-cdf-dd/Render?" + params
    } else {
      /* CDF mode */
      return webAppPath + "/content/pentaho-cdf/renderWcdf?" + params
    }
  }
};
