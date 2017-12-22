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
