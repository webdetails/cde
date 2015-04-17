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

define(['cdf/Dashboard.Clean', 'cde/components/NewMapComponent', 'cdf/lib/jquery'],
  function(Dashboard, NewMapComponent, $) {

  /**
   * ## The New Map Component
   */
  describe("The New Map Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var newMap = new NewMapComponent({
      type: "NewMapComponent",
      name: "newMap",
      executeAtStart: true,
      htmlObject: "sampleObject",
      parameters: [],
      listeners: [],
      tilesets: "mapquest"

    });
  
    dashboard.addComponent(newMap);
  
    /**
     * ## The New Map Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(newMap, 'update').and.callThrough();
      dashboard.update(newMap);
      setTimeout(function() {
        expect(newMap.update).toHaveBeenCalled();
        done();
      }, 100);
    });

  });
});
