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
