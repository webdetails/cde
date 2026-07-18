/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
