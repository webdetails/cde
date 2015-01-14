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

define(['cdf/Dashboard', 'cde/components/SiteMapComponent', 'cdf/lib/jquery'],
  function(Dashboard, SiteMapComponent, $) {

  /**
   * ## The Site Map Component
   */
  describe("The Site Map Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.addParameter("param1", function() {
      return [
        {"name": "Pentaho", "link": "http://www.pentaho.com/"},
        {"name": "Pentaho Community", "link": "http://community.pentaho.com/"},
        {
          "name": "Webdetails",
          "link": "http://www.webdetails.pt/",
          "sublinks": [
            {"name": "CTools", "link": "http://www.webdetails.pt/ctools/"},
            {"name": "Showcase", "link": "http://www.webdetails.pt/showcase/"}
          ]
        }];
    }());

    dashboard.init();

    // inject sampleObject div
    $("body").append($("<div>").attr("id", "sampleObject"));

    var siteMapComponent = new SiteMapComponent(dashboard, {
      type: "SiteMapComponent",
      name: "siteMapComponent",
      priority: 5,
      htmlObject: "sampleObject",
      parameters: [],
      executeAtStart: true,
      siteMapParameter: "param1",
      listeners: []
    });
  
    dashboard.addComponent(siteMapComponent);
  
    /**
     * ## The Site Map Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(siteMapComponent, 'update').and.callThrough();
      dashboard.update(siteMapComponent);
      setTimeout(function() {
        expect(siteMapComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });
  });
});
