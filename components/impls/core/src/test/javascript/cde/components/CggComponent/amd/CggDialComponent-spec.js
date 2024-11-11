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
  'cde/components/CggDialComponent',
  'cdf/lib/jquery'
], function(Dashboard, CggDialComponent, $) {

  /**
   * ## The Cgg Dial Component
   */
  describe("The Cgg Dial Component #", function() {
    var dashboard = new Dashboard();

    dashboard.addParameter("param1", "30");

    dashboard.init();

    var cggDialComponent = new CggDialComponent({
      type: "CggDialComponent",
      name: "cggDialComponent",
      executeAtStart: true,
      parameter: "param1",
      colors: [],
      intervals: [],
      htmlObject: "sampleObjectCggDial",
      listeners: [],
      width: 300,
      height: 300
    });
  
    dashboard.addComponent(cggDialComponent);

    // inject sampleObject div
    var $htmlObject = $('<div>').attr('id', cggDialComponent.htmlObject);
  
    /**
     * ## The Cgg Dial Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      expect(cggDialComponent.priority).toBe(6);

      $('body').append($htmlObject);

      spyOn(cggDialComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        params.success(
          new DOMParser().parseFromString(
            "<svg><rect x='0' width='300' height='300' y='0' style='fill:white'/></svg>",
            'image/svg+xml'));
      });

      // listen to cdf:postExecution event
      cggDialComponent.once("cdf:postExecution", function() {
        expect(cggDialComponent.update).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(cggDialComponent);
    });
  });
});
