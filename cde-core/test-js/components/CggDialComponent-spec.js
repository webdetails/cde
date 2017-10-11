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
