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

define(['cdf/Dashboard.Clean', 'cde/components/CggDialComponent', 'cdf/lib/jquery'],
  function(Dashboard, CggDialComponent, $) {

  /**
   * ## The Cgg Dial Component
   */
  describe("The Cgg Dial Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.addParameter("param1", "30");

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var cggDialComponent = new CggDialComponent({
      type: "CggDialComponent",
      name: "cggDialComponent",
      executeAtStart: true,
      parameter: "param1",
      colors: [],
      intervals: [],
      htmlObject: "sampleObject",
      listeners: [],
      width: 300,
      height: 300
    });
  
    dashboard.addComponent(cggDialComponent);
  
    /**
     * ## The Cgg Dial Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(cggDialComponent, 'update').and.callThrough();
      dashboard.update(cggDialComponent);
      setTimeout(function() {
        expect(cggDialComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
