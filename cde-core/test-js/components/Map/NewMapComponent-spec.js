/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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
