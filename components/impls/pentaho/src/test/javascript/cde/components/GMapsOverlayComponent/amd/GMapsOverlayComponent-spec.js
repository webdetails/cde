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
  'cde/components/GMapsOverlayComponent',
  'cdf/lib/jquery'
], function(Dashboard, GMapsOverlayComponent, $) {

  /**
   * ## The Google Maps Overlay Component
   */
  describe("The Google Maps Overlay Component #", function() {
    var dashboard = new Dashboard();

    dashboard.addDataSource("gMapsQuery", {
      dataAccessId: "GetCrimesByBeat",
      path: "/fake/path/file.cda"
    });

    dashboard.init();

    var gMapsOverlayComponent = new GMapsOverlayComponent({
      type: "GMapsOverlayComponent",
      name: "gMapsOverlayComponent",
      priority: 5,
      executeAtStart: true,
      htmlObject: "sampleObjectGMaps",
      listeners: [],
      preExecution: [],
      postExecution: [],
      parameters: [],
      mapName: "/fake/path/test.js",
      mapWidth: 750,
      mapHeight: 500,
      centerLatitude: 41.875696,
      centerLongitude: -87.624207,
      defaultZoomLevel: 10,
      legend: [],
      legendText: "Legend",
      search: false,
      queryDefinition: {dataSource: "gMapsQuery"}
    });

    dashboard.addComponent(gMapsOverlayComponent);

    /**
     * ## The Google Maps Overlay Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(gMapsOverlayComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        if(params.success) {
          params.success({"queryInfo":{"totalRows":"2"},"resultset":[["1011",257],["1012",177]],"metadata":[{"colIndex":0,"colType":"String","colName":"beat"},{"colIndex":1,"colType":"Numeric","colName":"counts"}]});
        }
      });

      // listen to cdf:postExecution event
      gMapsOverlayComponent.once("cdf:postExecution", function() {
        expect(gMapsOverlayComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(gMapsOverlayComponent);
    });
  });
});
