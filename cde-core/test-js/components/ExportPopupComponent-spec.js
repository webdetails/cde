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

define(['cdf/Dashboard.Clean', 'cde/components/ExportPopupComponent'],
  function(Dashboard, ExportPopupComponent) {

  /**
   * ## The ExportPopup Component
   */
  describe("The ExportPopup Component #", function() {
  
    var dashboard = new Dashboard();

    dashboard.addParameter("array", ["one", "two"]);
    dashboard.addParameter("funcArray", function() {return ["funcOne", "funcTwo"]});
    dashboard.addParameter("string", "stringOne;stringTwo");

    dashboard.init();

    var exportPopupComponent = new ExportPopupComponent({
      type: "ExportPopupComponent",
      name: "popup1",
      executeAtStart: true,
      chartExportType: 'png',
      chartExportComponent: 'chart',
      htmlObject: "sampleObject",
      parameters: [],
      listeners: []
    });
  
    dashboard.addComponent(exportPopupComponent);
  
    /**
     * ## The ExportPopup Component # Update Called
     */
    it("Update Called", function(done) {
      spyOn(exportPopupComponent, 'update').and.callThrough();
      dashboard.update(exportPopupComponent);
      setTimeout(function() {
        expect(exportPopupComponent.update).toHaveBeenCalled();
        done();
      }, 100);
    });

    /**
     * ## The ExportPopup Component # Export Chart Options
     */
    it("Export Chart Options", function() {
      dashboard.context = {
        path: 'fakePath/dashboard.wcdf'
      };
      exportPopupComponent.chartComponent = {
        parameters: [
          ['array', 'array'],
          ['funcArray', 'funcArray'],
          ['string', 'string']
        ]
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
     * ## The ExportPopup Component # Export Chart URL
     */
    it("Export Chart URL", function() {
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
