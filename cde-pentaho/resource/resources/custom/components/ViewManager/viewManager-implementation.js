/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

/* View Manager custom component for server version 4.8 */

var viewManagerComponent = BaseComponent.extend({
  update: function() {
    var myself = this;
    function fetchData() {
      $.ajax({
        type: "GET",
        url: webAppPath + wd.helpers.views.getListViewsEndpoint(),
        dataType: 'json',
        processData: false,
        success: function(response) {
          if(response.status === "error") {
            Dashboards.log(response.message, "warn");
            return;
          }
          myself.model.initViews(response.result);
          myself.view.render();
        },
        error: function() {
          Dashboards.log("Error fetching views data", "warn");
        },
        cache: false
      });
    };
    this.ph = $("#" + this.htmlObject);
    this.model = new wd.cdf.views.ViewManager();
    this.view = new wd.cdf.views.ViewManagerView({  
      el: this.ph.get(0),
      model: this.model
    });
    fetchData();
    this.model.on("update", fetchData, this);
  }
});
