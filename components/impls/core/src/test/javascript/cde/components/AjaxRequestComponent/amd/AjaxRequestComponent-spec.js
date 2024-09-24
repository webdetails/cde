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
