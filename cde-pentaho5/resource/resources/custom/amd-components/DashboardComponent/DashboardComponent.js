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
  './DashboardComponentExt'],
  function(UnmanagedComponent, DashboardComponentExt) { 

  var DashboardComponent = UnmanagedComponent.extend({

    update: function() {
      if(!this.preExec()) {
        return false;
      }
      var myself = this;
      require([DashboardComponentExt.getDashboardUrl(this.dashboardPath)], function(Dashboard) {
        myself.requiredDashboard = new Dashboard(myself.htmlObject);
        myself.mapParameters();
        myself.requiredDashboard.render();
        myself.postExec();
      });
    },

    mapParameters: function() {
      var reqDash = this.requiredDashboard;

      if(this.registeredEvents) {
        for(var evts in this.registeredEvents) {
          for(var i = 0; i < this.registeredEvents[evts].length; i++) {
            this.dashboard.off(evts, this.registeredEvents[evts][i]);
          }
        }
      }
      
      this.registeredEvents = {};

      for(var i = 0; i < this.parameterMapping.length; i++) {
        var otherParamName = this.parameterMapping[i][1];
        var eventName = this.parameterMapping[i][0] + ":fireChange";
        var fun = function(evt) {
          reqDash.fireChange(otherParamName, evt.value);
        };
        this.dashboard.on(eventName, fun);

        if(!this.registeredEvents[eventName]) {
          this.registeredEvents[eventName] = [];
        }

        this.registeredEvents[eventName].push(fun);
      }
    }
    
  });

  return DashboardComponent;

});
