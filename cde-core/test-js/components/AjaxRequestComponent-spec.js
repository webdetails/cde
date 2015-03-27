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

define(['cdf/Dashboard.Clean', 'cde/components/AjaxRequestComponent', 'cdf/lib/jquery'],
  function(Dashboard, AjaxRequestComponent, $) {

  /**
   * ## The Ajax Request Component
   */
  describe("The Ajax Request Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var ajaxRequestComponent = new AjaxRequestComponent({
      type: "AjaxRequestComponent",
      name: "ajaxRequest",
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      url: 'dummy/fake',
      ajaxRequestType: 'json',
      asyncCall: true
    });
  
    dashboard.addComponent(ajaxRequestComponent);
  
    /**
     * ## The Ajax Request Component # Update Called
     */
    it("Update Called", function(done) {
      var ajax = spyOn($, "ajax").and.callFake(function(options) {
        options.complete(
          { responseText: "{\"queryInfo\": {\"totalRows\": 1}, \"resultset\": [\"row1\", [1,2,3,4,5]]}"},
          "finished faking");
      });
      spyOn(ajaxRequestComponent, 'update').and.callThrough();
      dashboard.update(ajaxRequestComponent);
      setTimeout(function() {
        expect(ajaxRequestComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
