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
