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
  'cde/components/ExportPopupComponent'
], function(Dashboard, ExportPopupComponent) {

  /**
   * ## The ExportPopup Component
   */
  describe("The ExportPopup Component #", function() {
    var dashboard;
    var exportPopupComponent;

    beforeEach(function () {
      dashboard = new Dashboard();

      dashboard.addParameter("array", ["one", "two"]);
      dashboard.addParameter("funcArray", function() { return ["funcOne", "funcTwo"]; });
      dashboard.addParameter("string", "stringOne;stringTwo");

      dashboard.init();

      exportPopupComponent = new ExportPopupComponent({
        type: "ExportPopupComponent",
        name: "popup1",
        executeAtStart: true,
        chartExportType: 'png',
        chartExportComponent: 'chart',
        htmlObject: "sampleObjectExportPop",
        parameters: [],
        listeners: []
      });

      dashboard.addComponent(exportPopupComponent);
    });





    /**
     * ## The ExportPopup Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      spyOn(exportPopupComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      exportPopupComponent.once("cdf:postExecution", function() {
        expect(exportPopupComponent.update).toHaveBeenCalled();
        done();
      });

      dashboard.update(exportPopupComponent);
    });

    /**
     * ## The ExportPopup Component # sets the export chart options
     */
    it("sets the export chart options", function() {
      dashboard.context = {path: 'fakePath/dashboard.wcdf'};

      exportPopupComponent.chartComponent = {
        parameters: [['array', 'array'],
                     ['funcArray', 'funcArray'],
                     ['string', 'string']]
      };
      var expectedResult = {
        outputType: 'png',
        script: 'fakePath/chart.js',
        paramarray: ['one', 'two'],
        paramfuncArray: ['funcOne', 'funcTwo'],
        paramstring: 'stringOne;stringTwo'
      };

      expect(exportPopupComponent.getExportChartOptions()).toEqual(expectedResult);
    });

    /**
     * ## The ExportPopup Component # builds the export chart URL
     */
    it("builds the export chart URL", function() {
      var options = {
        outputType: 'png',
        script: 'fakePath/chart.js',
        paramarray: ['one', 'two'],
        paramfuncArray: ['funcOne', 'funcTwo'],
        paramstring: 'stringOne;stringTwo'
      };

      var expectedResult = "/pentaho/plugin/cgg/api/services/draw?" +
                           "outputType=png&script=fakePath%2Fchart.js" +
                           "&paramarray=one&paramarray=two" +
                           "&paramfuncArray=funcOne&paramfuncArray=funcTwo" +
                           "&paramstring=stringOne%3BstringTwo";

      expect(exportPopupComponent.getExportChartUrl(options)).toEqual(expectedResult);
    });
  });
});
