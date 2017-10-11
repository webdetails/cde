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
  'cde/components/NewSelectorComponent',
  'cdf/lib/jquery'
], function(Dashboard, NewSelectorComponent, $) {

  /**
   * ## The New Selector Component
   */
  describe("The New Selector Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    dashboard.addParameter("paramS", "");
    dashboard.addParameter("param1", "");

    dashboard.addDataSource("testQuery", {
      dataAccessId: "testDatId",
      path: "/dummy/path/toFile.cda"
    });

    var newSelectorComponent = new NewSelectorComponent({
      type: "NewSelectorComponent",
      name: "newSelectorComponent",
      parameter: "param1",
      title: "Press to select",
      searchParam: "paramS",
      executeAtStart: true,
      htmlObject: "sampleObjectNewSelect",
      parameters: [],
      valuesArray: [],
      valueAsId: true,
      listeners: [],
      multiselect: true,
      chartDefinition: {dataSource: "testQuery"}
    });

    dashboard.addComponent(newSelectorComponent);

    /**
     * ## The New Selector Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(newSelectorComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        if(params.success) {
          params.success({"queryInfo":{"totalRows":"2","pageStart":"0","pageSize":"54"},"resultset":[[1,"Cars"],[2,"Bikes"]],"metadata":[{"colIndex":0,"colType":"Integer","colName":"id"},{"colIndex":1,"colType":"String","colName":"value"}]});
        }
      });

      // listen to cdf:postExecution event
      newSelectorComponent.once("cdf:postExecution", function() {
        expect(newSelectorComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(newSelectorComponent);
    });
  });
});
