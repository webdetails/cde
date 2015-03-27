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

define(['cdf/Dashboard.Clean', 'cde/components/GMapsOverlayComponent', 'cdf/lib/jquery'],
  function(Dashboard, GMapsOverlayComponent, $) {

  /**
   * ## The Google Maps Overlay Component
   */
  describe("The Google Maps Overlay Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var gMapsOverlayComponent = new GMapsOverlayComponent({
      type: "GMapsOverlayComponent",
      name: "gMapsOverlayComponent",
      priority: 5,
      executeAtStart: true,
      htmlObject: "sampleObject",
      listeners: [],
      preExecution: [],
      postExecution: [],
      parameters: [],
      mapName: "/fake/path/file.js",
      mapWidth: 750,
      mapHeight: 500,
      centerLatitude: 41.875696,
      centerLongitude: -87.624207,
      defaultZoomLevel: 10,
      legend: [],
      legendText: "Legend",
      search: false,
      queryDefinition: {
        dataAccessId: "GetCrimesByBeat",
        path: "/fake/path/file.cda"
      }
    });
  
    dashboard.addComponent(gMapsOverlayComponent);
  
    /**
     * ## The Google Maps Overlay Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(gMapsOverlayComponent, 'update').and.callThrough();
      dashboard.update(gMapsOverlayComponent);
      setTimeout(function() {
        expect(gMapsOverlayComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
