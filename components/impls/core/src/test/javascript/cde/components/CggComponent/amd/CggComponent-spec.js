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
  'cde/components/CggComponent',
  'cdf/lib/jquery'
], function(Dashboard, CggComponent, $) {

  /**
   * ## The Cgg Component
   */
  describe("The Cgg Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    dashboard.addParameter("param1", "");
    dashboard.addParameter("param2", "");

    var cggComponent = new CggComponent({
      type: "CggComponent",
      name: "cggComponent",
      priority: 5,
      executeAtStart: true,
      parameters: [["param1","param1"],["param2","param2"]],
      htmlObject: "sampleObjectCgg",
      resourceFile: "test/path",
      listeners: [],
      width: 400,
      height: 400
    });
  
    dashboard.addComponent(cggComponent);

    // inject sampleObject div
    var $htmlObject = $('<div>').attr('id', cggComponent.htmlObject);
  
    /**
     * ## The Cgg Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      $('body').append($htmlObject);

      spyOn(cggComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        params.success(
          new DOMParser().parseFromString(
            "<svg><rect x='0' width='300' height='300' y='0' style='fill:white'/></svg>",
            'image/svg+xml'));
      });

      // listen to cdf:postExecution event
      cggComponent.once("cdf:postExecution", function() {
        expect(cggComponent.update).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(cggComponent);
    });

    /**
     * ## The Cgg Component # check if preExec is called
     */
    it("check if preExec is called", function() {
      spyOn(cggComponent, 'update').and.callThrough();
      spyOn(cggComponent, 'preExec');
      spyOn(cggComponent, "triggerAjax");
      spyOn(cggComponent, "synchronous");
      cggComponent.update();

      expect(cggComponent.preExec).toHaveBeenCalled();
      expect(cggComponent.triggerAjax).not.toHaveBeenCalled();
      expect(cggComponent.synchronous).not.toHaveBeenCalled();
    });
  });
});
