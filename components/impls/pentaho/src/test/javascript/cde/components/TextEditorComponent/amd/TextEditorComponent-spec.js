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
  'cde/components/TextEditorComponent',
  'cdf/lib/jquery'
], function(Dashboard, TextEditorComponent, $) {

  /**
   * ## The Text Editor Component
   */
  describe("The Text Editor Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    var textEditorComponent = new TextEditorComponent({
      type: "TextEditorComponent",
      name: "textEditorComponent",
      priority: 5,
      executeAtStart: true,
      htmlObject: "sampleObjectTextEdit",
      parameters: [],
      parameter: undefined,
      listeners: []
    });

    dashboard.addComponent(textEditorComponent);

    /**
     * ## The Text Editor Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(textEditorComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      textEditorComponent.once("cdf:postExecution", function() {
        expect(textEditorComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(textEditorComponent);
    });
  });
});
