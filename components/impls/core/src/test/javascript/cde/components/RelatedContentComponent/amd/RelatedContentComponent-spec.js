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
