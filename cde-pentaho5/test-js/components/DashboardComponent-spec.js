/*!
 * Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company. All rights reserved.
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
  'cdf/Dashboard.Clean',
  'cde/components/DashboardComponent',
  'cdf/lib/jquery'
], function(Dashboard, DashboardComponent, $) {

  /**
   * ## The Dashboard Component
   */
  describe("The Dashboard Component #", function() {
    var dashboard;
    var dashboardComponent;
    var mapTest = "mappingTest";

    beforeEach(function() {
      dashboard = new Dashboard();
      dashboard.init();
      dashboard.addParameter("param1", "");
      dashboard.addParameter("param2", "");
      dashboard.addParameter("param3", "");

      dashboard.addDataSource("dataSource", {
        origin: "DashboardComponent"
      });

      dashboardComponent = new DashboardComponent({
        type: "DashboardComponent",
        name: "render_test",
        priority: 5,
        dashboardPath: "cde/test/dummyDashboard",
        parameterMapping: [["param1", "dummyParam"], ["param2", "privateParam"], ["param3", "multiMapParam"]],
        dataSourceMapping: [["dataSource", "dummyDataSource"]],
        executeAtStart: true,
        htmlObject: "sampleObject",
        listeners: [],
        oneWayMap: false
      });
      dashboard.addComponent(dashboardComponent);

    });

    var makeAjaxSpy = function(datasources) {
      var successObj = {
        parameters: ["dummyParam", "multiMapParam"],
        dataSources: datasources ? ["dummyDataSource"] : [],
        // split function to bypass i18n error
        split: function() { return ""; }
      };
      dataSources: ["dummyDataSource"],
      spyOn($, "ajax").and.callFake(function(params) {
        params.success(successObj);
      });
    };

    /**
     * ## The Dashboard Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      makeAjaxSpy();
      spyOn(dashboardComponent, 'update').and.callThrough();

      dashboardComponent.once('cdf:postExecution', function() {
        expect(dashboardComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # allows the correct use of parameter mapping
     */
    it("allows the correct use of parameter mapping", function(done) {
      makeAjaxSpy();

      dashboard.setParameter("param1", "beforeTrigger");
      dashboardComponent.once('cdf:postExecution', function() {
        expect(dashboardComponent.requiredDashboard.getParameterValue("dummyParam")).toEqual("beforeTrigger");
        dashboardComponent.requiredDashboard.once("dummyParam:fireChange", function() {
          expect(dashboardComponent.requiredDashboard.getParameterValue("dummyParam")).toEqual(mapTest);
          done();
        });
        dashboard.fireChange("param1", mapTest);
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # will not map private parameters
     */
    it("will not map private parameters", function(done) {
      makeAjaxSpy();

      dashboardComponent.once('cdf:postExecution', function() {
        expect(dashboardComponent.registeredEvents["param1:fireChange"].length).toEqual(1);
        expect(dashboardComponent.registeredEvents["param2:fireChange"]).not.toBeDefined();
        done();
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # allows the correct use of data sources mapping
     */
    it("allows the correct use of data sources mapping", function(done) {
      makeAjaxSpy(true);

      dashboardComponent.once('cdf:postExecution', function() {
        expect(dashboardComponent.requiredDashboard.getDataSource("dummyDataSource").origin).toEqual("DashboardComponent");
        done();
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # allow parameter mapping in both directions
     */
    it("allow parameter mapping in both directions", function(done) {
      makeAjaxSpy();

      dashboard.setParameter("param1", 1);
      dashboardComponent.once('cdf:postExecution', function() {
        dashboardComponent.requiredDashboard.once("dummyParam:fireChange", function() {
          expect(dashboard.getParameterValue("param1")).toEqual("1");
          expect(dashboard.getParameterValue("param1")).not.toEqual(1);
          done();
        });
        dashboardComponent.requiredDashboard.fireChange("dummyParam", "1");
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # should not allow parameter mapping in both directions if oneWayMap is activated
     */
    it("should not allow parameter mapping in both directions if oneWayMap is activated", function(done) {
      makeAjaxSpy();

      dashboardComponent.oneWayMap = true;
      dashboard.setParameter("param1", 1);
      dashboardComponent.once('cdf:postExecution', function() {
        dashboardComponent.requiredDashboard.once("dummyParam:fireChange", function() {
          expect(dashboard.getParameterValue("param1")).not.toEqual("1");
          done();
        });
        dashboardComponent.requiredDashboard.fireChange("dummyParam", "1");
      });

      dashboard.update(dashboardComponent);
    });
    /**
     * ## The Dashboard Component # should not allow parameter mapping if propagation is paused
     */
    it("should not allow parameter mapping if propagation is paused", function(done) {
      makeAjaxSpy();
 
      dashboardComponent.once('cdf:postExecution', function() {
        dashboardComponent.requiredDashboard.once("dummyParam:fireChange", function() {
          expect(dashboard.getParameterValue("param1")).not.toEqual("test");
          done();
        });
        dashboardComponent.pausePropagation();
        dashboardComponent.requiredDashboard.fireChange("dummyParam", "test");
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # should propagate all parameters when one parameter changes from the main dashboard to the required dashboard
     */
    it("should propagate all parameters when one parameter changes from the main dashboard to the required dashboard", function(done) {
      makeAjaxSpy();

      dashboardComponent.once('cdf:postExecution', function() {
        dashboardComponent.requiredDashboard.once("multiMapParam:fireChange", function() {
          expect(dashboardComponent.requiredDashboard.getParameterValue("multiMapParam")).toEqual("test");
          expect(dashboardComponent.requiredDashboard.getParameterValue("dummyParam")).toEqual("custom");
          done();
        });
        dashboard.setParameter("param1", "custom");
        dashboard.fireChange("param3", "test");
       
      });

      dashboard.update(dashboardComponent);
    });
    
    /**
     * ## The Dashboard Component # should propagate all parameters when one parameter changes from required dashboard to the main dashboard
     */
    it("should propagate all parameters when one parameter changes from required dashboard to the main dashboard", function(done) {
      makeAjaxSpy();

      dashboardComponent.once('cdf:postExecution', function() {
        dashboard.once("param1:fireChange", function() {
          expect(dashboard.getParameterValue("param1")).toEqual("test");
          expect(dashboard.getParameterValue("param3")).toEqual("custom");
          done();
        });
        dashboardComponent.requiredDashboard.setParameter("multiMapParam", "custom");
        dashboardComponent.requiredDashboard.fireChange("dummyParam", "test");
      });

      dashboard.update(dashboardComponent);
    });

    /**
     * ## The Dashboard Component # must wait for the required Dashboard to finish executing
     */
     it("allows the external dashboard to finish executing before itself", function(done) {
      makeAjaxSpy(true);
     
       dashboardComponent.once('cdf:postExecution', function() {
         expect(dashboardComponent.requiredDashboard.runningCalls).toEqual(1);
         done();
      });
       dashboard.update(dashboardComponent);
    });
  });
});
