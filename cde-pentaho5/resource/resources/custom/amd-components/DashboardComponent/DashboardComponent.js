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

define([
  'cdf/components/UnmanagedComponent',
  './DashboardComponentExt',
  'cdf/lib/jquery'],
  function(UnmanagedComponent, DashboardComponentExt, $) {

  var DashboardComponent = UnmanagedComponent.extend({

    update: function() {
      if(!this.preExec()) {
        return false;
      }
      var myself = this;
      require([DashboardComponentExt.getDashboardUrl(this.dashboardPath)], function(Dashboard) {
        myself.requiredDashboard = new Dashboard(myself.htmlObject);
        myself.mapDataSources();
        myself.unregisterEvents();
        myself.requiredDashboard.render();
        myself.mapParameters();
        myself.postExec();
      });
    },

    mapDataSources: function() {
      for (var i = 0; i < this.dataSourceMapping.length; i++) {
        this.requiredDashboard.setDataSource(
          this.dataSourceMapping[i][1],
          // TODO: should we copy the datasource, is it safe to use a reference?
          // $.extend({}, this.dashboard.getDataSource(this.dataSourceMapping[i][0])),
          this.dashboard.getDataSource(this.dataSourceMapping[i][0]),
          true);
      }
    },

    unregisterEvents: function() {
      if(this.registeredEvents) {
        for(var evts in this.registeredEvents) {
          for(var i = 0; i < this.registeredEvents[evts].length; i++) {
            this.dashboard.off(evts, this.registeredEvents[evts][i]);
          }
        }
      }
    },

    mapParameters: function() {
      var myself = this;
      var reqDash = this.requiredDashboard;
      this.registeredEvents = {};
      this.publicParameters = [];

      var getPublicParametersUrl = DashboardComponentExt.getDashboardParametersEndpoint() + this.dashboardPath;

      $.ajax({
        url: getPublicParametersUrl,
        type: "GET",
        async: true,
        success: function(data) {
          myself.publicParameters = data.parameters;
          myself.loopThroughMapping(function(myParam, otherParam) {
            if(myself.isParameterPublic(otherParam)) {
              var eventName = myParam + ":fireChange";
              var fun = function(evt) {
                reqDash.fireChange(otherParam, evt.value);
              };
              myself.dashboard.on(eventName, fun);

              if(!myself.registeredEvents[eventName]) {
                myself.registeredEvents[eventName] = [];
              }
              myself.registeredEvents[eventName].push(fun);
            }
          });
        },
        xhrFields: {
          withCredentials: true
        }
      });
    },

    loopThroughMapping: function(fun){
      for(var i = 0; i< this.parameterMapping.length; i++) {
        fun(this.parameterMapping[i][0], this.parameterMapping[i][1]);
      }
    },

    isParameterPublic: function(parameterName){
      for(var i = 0; i < this.publicParameters.length; i++) {
        if(parameterName === this.publicParameters[i]) {
          return true;
        }
      }
      return false;
     }
  });

  return DashboardComponent;

});
