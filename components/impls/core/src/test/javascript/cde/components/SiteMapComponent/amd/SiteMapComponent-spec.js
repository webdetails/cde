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
  'cde/components/SiteMapComponent',
  'cdf/lib/jquery'
], function(Dashboard, SiteMapComponent, $) {

  /**
   * ## The Site Map Component
   */
  describe("The Site Map Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    dashboard.addParameter("param1", function() {
      return [{"name": "Pentaho", "link": "http://www.pentaho.com/"},
              {"name": "Pentaho Community", "link": "http://community.pentaho.com/"},
              {"name": "Webdetails",
                "link": "http://www.webdetails.pt/",
                "sublinks": [{"name": "CTools", "link": "https://community.hitachivantara.com/s/topic/0TO1J0000017kVNWAY/ctools"},
                             {"name": "Showcase", "link": "http://www.webdetails.pt/showcase/"}]}];
    }());

    var siteMapComponent = new SiteMapComponent({
      type: "SiteMapComponent",
      name: "siteMapComponent",
      priority: 5,
      htmlObject: "sampleObjectSiteMap",
      parameters: [],
      executeAtStart: true,
      siteMapParameter: "param1",
      listeners: []
    });

    dashboard.addComponent(siteMapComponent);

    /**
     * ## The Site Map Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(siteMapComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      siteMapComponent.once("cdf:postExecution", function() {
        expect(siteMapComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(siteMapComponent);
    });
  });
});
