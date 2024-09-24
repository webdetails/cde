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
