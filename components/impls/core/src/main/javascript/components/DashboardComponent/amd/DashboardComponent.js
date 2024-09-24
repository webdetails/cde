/*!
 * Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company. All rights reserved.
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
  './DashboardComponent.ext',
  'cdf/lib/jquery'
], function(UnmanagedComponent, DashboardComponentExt, $) {

  return UnmanagedComponent.extend({

    /**
     * @summary Pauses parameter propagation if true.
     * @description Flag that controls if the mapped parameters should propagate or not.
     *
     * @name cdf.components.DashboardComponent#_pause
     * @type {boolean}
     * @default false
     * @private
     */
    _pause: false,

    update: function() {
      if(!this.preExec()) {
        return false;
      }
      var myself = this;
      require([DashboardComponentExt.getDashboardUrl(this.dashboardPath)], function(Dashboard) {
        myself.requiredDashboard = new Dashboard(myself.htmlObject);
        myself.mapDataSources();
        myself.unregisterEvents();
        myself.requiredDashboard.setupDOM();
        myself.requiredDashboard._processComponents();
        myself.mapParameters(function() {
          myself.requiredDashboard.on('cdf:postInit', function(event) { 
            myself.postExec();
         });
        myself.requiredDashboard.init();
        });
      });
    },

    mapDataSources: function() {
      for(var i = 0; i < this.dataSourceMapping.length; i++) {
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

    mapParameters: function(callback) {
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
          myself.publicParameters = data.parameters || [];
          myself.loopThroughMapping(function(myParam, otherParam) {
            if(myself.isParameterPublic(otherParam)) {
              var eventName = myParam + ":fireChange";
              var fun = function(evt) {
                if((!myself._pause) &&
                  // deep comparison to avoid infinite loop after bubbling up the param change event
                  JSON.stringify(reqDash.getParameterValue(otherParam)) !== JSON.stringify(evt.value)) {

                  myself.loopThroughMapping(function(myParam, otherParam) {
                    reqDash.setParameter(otherParam, myself.dashboard.getParameterValue(myParam));
                  });
                  reqDash.fireChange(otherParam, evt.value);
                }
              };
              myself.dashboard.on(eventName, fun);
              reqDash.on(otherParam + ":fireChange", function (evt) {
                if(!myself._pause && myself.oneWayMap == false) {
                  if(JSON.stringify(myself.dashboard.getParameterValue(myParam)) !== JSON.stringify(evt.value)) {
                    myself.loopThroughMapping(function(myParam, otherParam) {
                      myself.dashboard.setParameter(myParam, reqDash.getParameterValue(otherParam));
                    });
                  }
                  // buble up the param change event
                  myself.dashboard.fireChange(myParam, evt.value);
                }
              });

              if(!myself.registeredEvents[eventName]) {
                myself.registeredEvents[eventName] = [];
              }
              myself.registeredEvents[eventName].push(fun);
              myself.requiredDashboard.setParameter(otherParam, myself.dashboard.getParameterValue(myParam));
            }
          });
          callback();
        },
        error: function(err) {
          myself.failExec(err);
        },
        xhrFields: {
          withCredentials: true
        }
      });
    },

    /**
     * @summary Pauses parameter propagation.
     * @description <p>Method that causes parameters to stop propagating</p>.
     *              <p>To resume propagation {@link cdf.components.DashboardComponent#resumePropagation|resumePropagation} should be called.</p>
     */
    pausePropagation: function () {
      this._pause = true;
    },

    /**
     * @summary Resumes parameter propagation.
     * @description <p>Method that allows parameters to propagate.</p>
     *              <p>To stop propagation {@link cdf.components.DashboardComponent#pausePropagation|pausePropagation} should be called.</p>
     */
    resumePropagation: function () {
      this._pause = false;
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

});
