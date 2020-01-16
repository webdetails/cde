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
                "sublinks": [{"name": "CTools", "link": "https://community.hitachivantara.com/community/products-and-solutions/pentaho/ctools"},
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
