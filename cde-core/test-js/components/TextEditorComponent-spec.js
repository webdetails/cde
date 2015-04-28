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

define(['cdf/Dashboard.Clean', 'cde/components/TextEditorComponent', 'cdf/lib/jquery'],
  function(Dashboard, TextEditorComponent, $) {

  /**
   * ## The Text Editor Component
   */
  describe("The Text Editor Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var textEditorComponent = new TextEditorComponent({
      type: "TextEditorComponent",
      name: "textEditorComponent",
      priority: 5,
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      parameter: undefined,
      listeners: []
    });
  
    dashboard.addComponent(textEditorComponent);
  
    /**
     * ## The Text Editor Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(textEditorComponent, 'update').and.callThrough();
      dashboard.update(textEditorComponent);
      setTimeout(function() {
        expect(textEditorComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
