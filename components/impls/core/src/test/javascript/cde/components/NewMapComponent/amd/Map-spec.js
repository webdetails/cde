/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


define([
  'cdf/Dashboard.Clean',
  'cde/components/NewMapComponent',
  'cdf/lib/jquery'
], function(Dashboard, NewMapComponent, $) {

  /**
   * ## The New Map Component
   */
  describe("The New Map Component #", function() {

    var htmlObject = "newMapComponentObj";
    var dashboard, newMap, $htmlObject;

    beforeEach(function() {
      newMap = new NewMapComponent({
        type: "NewMapComponent",
        name: "newMap",
        executeAtStart: true,
        htmlObject: htmlObject,
        parameters: [],
        listeners: [],
        tilesets: "mapquest"
      });
      dashboard = new Dashboard();
      dashboard.init();
      dashboard.addComponent(newMap);
      $htmlObject = $('<div>').attr('id', htmlObject);
    });

    var setExtraOptions = function() {
      newMap.preExecution = function() { this.preExecutionCalled = true; };
      newMap.postFetch = function() { this.postFetchCalled = true; };
      newMap.postExecution = function() { this.postExecutionCalled = true; };
    };

    /**
     * ## The New Map Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      $('body').append($htmlObject);

      spyOn(newMap, 'update').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {});

      // listen to cdf:postExecution event
      newMap.once("cdf:postExecution", function() {
        expect(newMap.update).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(newMap);
    });

    /**
     * ## The New Map Component # follows a proper synchronous lifecycle
     */
    it("follows a proper synchronous lifecycle", function(done) {
      $('body').append($htmlObject);
      setExtraOptions();
      spyOn(newMap, 'maybeToggleBlock').and.callThrough();

      // listen to cdf:preExecution event
      newMap.once("cdf:preExecution", function() {
        expect(newMap.preExecutionCalled).toEqual(true);
        expect(newMap.postExecutionCalled).toBeUndefined();
      });
      // listen to cdf:postExecution event
      newMap.once("cdf:postExecution", function() {
        expect(newMap.preExecutionCalled).toEqual(true);
        expect(newMap.postExecutionCalled).toEqual(true);
        expect(newMap.maybeToggleBlock).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(newMap);
    });

    /**
     * ## The New Map Component # follows a proper asynchronous lifecycle
     */
    it("follows a proper asynchronous lifecycle", function(done) {
      $('body').append($htmlObject);
      setExtraOptions();
      newMap.queryDefinition = {dataSource: "dummy"};
      dashboard.addDataSource("dummy", {
        dataAccessId: "dummy",
        path: "/test/dummy.cda"
      });
      spyOn(newMap, 'maybeToggleBlock').and.callThrough();
      spyOn($, "ajax").and.callFake(function(params) {
        params.success({});
      });

      // listen to cdf:preExecution event
      newMap.once("cdf:preExecution", function() {
        expect(newMap.preExecutionCalled).toEqual(true);
        expect(newMap.postFetchCalled).toBeUndefined();
        expect(newMap.postExecutionCalled).toBeUndefined();
      });
      // listen to cdf:postFetch event
      newMap.once("cdf:postFetch", function() {
        expect(newMap.preExecutionCalled).toEqual(true);
        expect(newMap.postFetchCalled).toEqual(true);
        expect(newMap.postExecutionCalled).toBeUndefined();
        newMap.postFetchCalled = true;
      });
      // listen to cdf:postExecution event
      newMap.once("cdf:postExecution", function() {
        expect(newMap.preExecutionCalled).toEqual(true);
        expect(newMap.postFetchCalled).toEqual(true);
        expect(newMap.postExecutionCalled).toEqual(true);
        expect(newMap.maybeToggleBlock).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(newMap);
    });
  });
});
