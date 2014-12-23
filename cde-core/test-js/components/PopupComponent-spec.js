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

define(['cdf/Dashboard', 'cde/components/PopupComponent', 'cdf/lib/jquery'],
  function(Dashboard, PopupComponent, $) {

  /**
   * ## The Popup Component
   */
  describe("The Popup Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var popupComponent = new PopupComponent(dashboard, {
      type: "PopupComponent",
      name: "popup1",
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      listeners: []
    });
  
    dashboard.addComponent(popupComponent);
  
    /**
     * ## The Popup Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(popupComponent, 'update').and.callThrough();
      dashboard.update(popupComponent);
      setTimeout(function() {
        expect(popupComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });

    /**
     * ## The Popup Component # can be cloned
     */
    it("can be cloned", function(done) {
      spyOn(popupComponent, 'clone').and.callThrough();
      var popupCloned = popupComponent.clone([], [], $("body"));
      setTimeout(function() {
        expect(popupComponent.clone).toHaveBeenCalled();
        expect(popupCloned.ph.find("[id]").attr("id")).toEqual(popupComponent.htmlObject + "_1");
        done();
      }, 100);
    });
  });
});
