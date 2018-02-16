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
  'cde/components/RelatedContentComponent'
], function(Dashboard, RelatedContentComponent) {

  /**
   * ## The Related Content Component
   */
  describe("The Related Content Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    var relatedContentComponent = new RelatedContentComponent({
      type: "RelatedContentComponent",
      name: "relatedContentComponent",
      priority: 5,
      relatedContent: [["Pentaho","http://www.pentaho.com/"],
                       ["Pentaho Community","http://community.pentaho.com/"],
                       ["Webdetails","http://www.webdetails.pt/"]],
      executeAtStart: true
    });

    dashboard.addComponent(relatedContentComponent);

    /**
     * ## The Related Content Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(relatedContentComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      relatedContentComponent.once("cdf:postExecution", function() {
        expect(relatedContentComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(relatedContentComponent);
    });
  });
});
