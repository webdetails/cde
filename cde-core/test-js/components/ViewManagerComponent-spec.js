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

define(['cdf/Dashboard.Clean', 'cde/components/ViewManagerComponent', 'cdf/lib/jquery'],
  function(Dashboard, ViewManagerComponent, $) {

  /**
   * ## The View Manager Component
   */
  describe("The View Manager Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var viewManagerComponent = new ViewManagerComponent({
      type: "ViewManagerComponent",
      name: "viewManagerComponent",
      htmlObject: "sampleObject",
      priority: 5,
      executeAtStart: true,
      listeners: []
    });
  
    dashboard.addComponent(viewManagerComponent);
  
    /**
     * ## The View Manager Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(viewManagerComponent, 'update').and.callThrough();
      dashboard.update(viewManagerComponent);
      setTimeout(function() {
        expect(viewManagerComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
