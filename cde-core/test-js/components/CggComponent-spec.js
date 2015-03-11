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

define(['cdf/Dashboard.Clean', 'cde/components/CggComponent', 'cdf/lib/jquery'],
  function(Dashboard, CggComponent, $) {

  /**
   * ## The Cgg Component
   */
  describe("The Cgg Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.addParameter("param1", "");
    dashboard.addParameter("param2", "");

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var cggComponent = new CggComponent(dashboard, {
      type: "CggComponent",
      name: "cggComponent",
      priority: 5,
      executeAtStart: true,
      parameters: [["param1","param1"],["param2","param2"]],
      htmlObject: "sampleObject",
      resourceFile: "fake/path",
      listeners: [],
      width: 400,
      height: 400
    });
  
    dashboard.addComponent(cggComponent);
  
    /**
     * ## The Cgg Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(cggComponent, 'update').and.callThrough();
      dashboard.update(cggComponent);
      setTimeout(function() {
        expect(cggComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
