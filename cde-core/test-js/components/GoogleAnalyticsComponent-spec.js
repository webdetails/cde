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

define(['cdf/Dashboard.Clean', 'cde/components/GoogleAnalyticsComponent'],
  function(Dashboard, GoogleAnalyticsComponent) {

  /**
   * ## The Google Analytics Component
   */
  describe("The Google Analytics Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.init();

    var googleAnalyticsComponent = new GoogleAnalyticsComponent({
      type: "GoogleAnalyticsComponent",
      name: "googleAnalyticsComponent",
      priority: 5,
      executeAtStart: true,
      htmlObject: undefined,
      gaTrackingId: "UA-xxxxxxx-x",
      listeners: []
    });
  
    dashboard.addComponent(googleAnalyticsComponent);
  
    /**
     * ## The Google Analytics Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(googleAnalyticsComponent, 'update').and.callThrough();
      dashboard.update(googleAnalyticsComponent);
      setTimeout(function() {
        expect(googleAnalyticsComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
