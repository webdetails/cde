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
  'cde/components/DuplicateComponent',
  'cdf/lib/jquery'
], function(Dashboard, CggDialComponent, DuplicateComponent, $) {

  /**
   * ## The Duplicate Component
   */
  describe("The Duplicate Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    dashboard.addParameter("param1", "30");
    dashboard.setParameterViewMode("param1", "unused");

    var render_cggDial1 = new CggDialComponent({
      type: "CggDialComponent",
      name: "render_cggDial1",
      priority: 5,
      executeAtStart: true,
      parameter: "param1",
      colors: [],
      intervals: [],
      htmlObject: "sampleObjectDup1",
      listeners: [],
      width: 300,
      height: 300
    });

    var duplicateComponent = new DuplicateComponent({
      type: "DuplicateComponent",
      name: "duplicateComponent",
      priority: 5,
      executeAtStart: true,
      components: ["cggDial1"],
      parameters: [],
      htmlObject: "sampleObjectDup2",
      targetHtmlObject: "sampleObjectDup3",
      listeners: []
    });

    dashboard.addComponents([render_cggDial1, duplicateComponent]);

    // inject sampleObject div
    var $htmlObject1 = $('<div>').attr('id', render_cggDial1.htmlObject),
        $htmlObject2 = $('<div>').attr('id', duplicateComponent.htmlObject),
        $htmlObject3 = $('<div>').attr('id', duplicateComponent.targetHtmlObject);

    /**
     * ## The Duplicate Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      $('body').append($htmlObject1)
               .append($htmlObject2)
               .append($htmlObject3);

      spyOn(duplicateComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      duplicateComponent.once("cdf:postExecution", function() {
        expect(duplicateComponent.update).toHaveBeenCalled();
        $htmlObject1.remove();
        $htmlObject2.remove();
        $htmlObject3.remove();
        done();
      });

      dashboard.update(duplicateComponent);
    });
  });
});
