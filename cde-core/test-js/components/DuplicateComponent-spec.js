/*!
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

define([
  'cdf/Dashboard.Clean',
  'cde/components/CggDialComponent',
  'cde/components/DuplicateComponent',
  'cdf/lib/jquery'],
  function(Dashboard, CggDialComponent, DuplicateComponent, $) {

  /**
   * ## The Duplicate Component
   */
  describe("The Duplicate Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.addParameter("param1", "30");
    dashboard.setParameterViewMode("param1", "unused");

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject1"));
    $("body").append($("<div>").attr("id", "sampleObject2"));
    $("body").append($("<div>").attr("id", "sampleObject3"));
  
    var render_cggDial1 = new CggDialComponent(dashboard, {
      type: "CggDialComponent",
      name: "render_cggDial1",
      priority: 5,
      executeAtStart: true,
      parameter: "param1",
      colors: [],
      intervals: [],
      htmlObject: "sampleObject1",
      listeners: [],
      width: 300,
      height: 300
    });

    var duplicateComponent = new DuplicateComponent(dashboard, {
      type: "DuplicateComponent",
      name: "duplicateComponent",
      priority: 5,
      executeAtStart: true,
      components: ["cggDial1"],
      parameters: [],
      htmlObject: "sampleObject2",
      targetHtmlObject: "sampleObject3",
      listeners: []
    });

    dashboard.addComponents([render_cggDial1, duplicateComponent]);
  
    /**
     * ## The Duplicate Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(duplicateComponent, 'update').and.callThrough();
      dashboard.update(duplicateComponent);
      setTimeout(function() {
        expect(duplicateComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
