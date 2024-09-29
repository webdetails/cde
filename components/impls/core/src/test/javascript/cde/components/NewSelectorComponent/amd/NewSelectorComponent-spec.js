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
