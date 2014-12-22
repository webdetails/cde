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

define(['cdf/Dashboard', 'cde/components/ExportPopupComponent'],
  function(Dashboard, ExportPopupComponent) {

  /**
   * ## The ExportPopup Component
   */
  describe("The ExportPopup Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    var exportPopupComponent = new ExportPopupComponent(dashboard, {
      type: "ExportPopupComponent",
      name: "popup1",
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      listeners: []
    });
  
    dashboard.addComponent(exportPopupComponent);
  
    /**
     * ## The ExportPopup Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(exportPopupComponent, 'update').and.callThrough();
      dashboard.update(exportPopupComponent);
      setTimeout(function() {
        expect(exportPopupComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
