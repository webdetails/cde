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
  'cde/components/GoogleAnalyticsComponent',
  'cdf/lib/jquery'
], function(Dashboard, GoogleAnalyticsComponent, $) {

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
     * ## The Google Analytics Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(googleAnalyticsComponent, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) { return; });

      // listen to cdf:postExecution event
      googleAnalyticsComponent.once("cdf:postExecution", function() {
        expect(googleAnalyticsComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(googleAnalyticsComponent);
    });
  });
});
