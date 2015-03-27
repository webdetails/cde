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

define([
  'cdf/Dashboard.Clean',
  'cde/components/NewSelectorComponent',
  'cdf/lib/jquery'],
  function(Dashboard, NewSelectorComponent, $) {

  /**
   * ## The New Selector Component
   */
  describe("The New Selector Component #", function() {

    var dashboard = new Dashboard();

    dashboard.addParameter("paramS", "");
    dashboard.addParameter("param1", "");

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var newSelectorComponent = new NewSelectorComponent({
      type: "NewSelectorComponent",
      name: "newSelectorComponent",
      parameter: "param1",
      title: "Press to select",
      searchParam: "paramS",
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      valuesArray: [],
      valueAsId: true,
      listeners: [],
      multiselect: true,
      chartDefinition: {
        dataAccessId: "Char2Col",
        path: "/dummy/path/toFile.cda"
      }
    });

    dashboard.addComponent(newSelectorComponent);

    /**
     * ## The New Selector Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(newSelectorComponent, 'update').and.callThrough();
      dashboard.update(newSelectorComponent);
      setTimeout(function() {
        expect(newSelectorComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });

  });
});
