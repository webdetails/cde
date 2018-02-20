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
  'cde/components/AjaxRequestComponent',
  'cdf/lib/jquery'
], function(Dashboard, AjaxRequestComponent, $) {

  /**
   * ## The Ajax Request Component
   */
  describe("The Ajax Request Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    var ajaxRequestComponent = new AjaxRequestComponent({
      type: "AjaxRequestComponent",
      name: "ajaxRequest",
      executeAtStart: true,
      htmlObject: "sampleObjectAjax",
      parameters: [],
      url: 'test/fake',
      ajaxRequestType: 'json',
      asyncCall: true
    });

    dashboard.addComponent(ajaxRequestComponent);

    /**
     * ## The Ajax Request Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(ajaxRequestComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        params.complete(
          {responseText: "{\"queryInfo\": {\"totalRows\": 1}, \"resultset\": [\"row1\", [3,4]]}"}
        );
      });

      // listen to cdf:postExecution event
      ajaxRequestComponent.once("cdf:postExecution", function() {
        expect(ajaxRequestComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(ajaxRequestComponent);
    });
  });
});
