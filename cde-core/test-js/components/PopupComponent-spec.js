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
  'cde/components/PopupComponent',
  'cdf/lib/jquery'
], function(Dashboard, PopupComponent, $) {

  /**
   * ## The Popup Component
   */
  describe("The Popup Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    var popupComponent = new PopupComponent({
      type: "PopupComponent",
      name: "popup1",
      executeAtStart: true,
      htmlObject: "sampleObjectPop",
      parameters: [],
      listeners: []
    });

    dashboard.addComponent(popupComponent);

    // inject samplePopObject div
    var $htmlObject = $('<div>').attr('id', popupComponent.htmlObject);
    $('body').append($htmlObject);

    /**
     * ## The Popup Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(popupComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      popupComponent.once("cdf:postExecution", function() {
        expect(popupComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(popupComponent);
    });

    /**
     * ## The Popup Component # can be cloned
     */
    it("can be cloned", function() {
      spyOn(popupComponent, 'clone').and.callThrough();

      var popupCloned = popupComponent.clone([], [], $("body"));

      expect(popupComponent.clone).toHaveBeenCalled();
      expect(popupCloned.ph.find("[id]").attr("id")).toEqual(popupComponent.htmlObject + "_1");
      $htmlObject.remove();
    });
  });
});
