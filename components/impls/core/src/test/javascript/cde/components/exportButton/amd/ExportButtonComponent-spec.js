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
  'cde/components/ExportButtonComponent',
  'cdf/components/TableComponent',
  'cdf/lib/jquery'
], function(Dashboard, ExportButtonComponent, TableComponent, $) {

  /**
   * ## The Export Button Component
   */
  describe("The Export Button Component #", function() {
    var dashboard = new Dashboard();

    dashboard.init();

    var exportButtonComponent = new ExportButtonComponent({
      type: "ExportButtonComponent",
      name: "expButton1",
      executeAtStart: true,
      htmlObject: "sampleObjectExportButton",
      priority: 5,
      exportPage: true,
      label: "TestExport",
      componentName: "",
      parameters: [],
      outputType: "csv",
      listeners: []
    });

    dashboard.addDataSource("tableQuery", {
      queryType: "cda",
      dataAccessId: "1",
      path: "samplePath",
      catalog: "mondrian:/SteelWheels",
      jndi: "SampleData",
      query: function() {
        return "SELECT NON EMPTY {[Measures].[Sales]} ON COLUMNS, " +
          "NON EMPTY TopCount([Customers].[All Customers].Children, 50.0, [Measures].[Sales]) " +
          "ON ROWS FROM [SteelWheelsSales]";
      }
    });

    var tableDefinition = {
     colHeaders: ["Customers", "Sales"],
     colTypes: ['string', 'numeric'],
     colFormats: [null, '%.0f'],
     colWidths: ['500px', null],
     displayLength: 10,
     filter: "true",
     colSearchable: ["0"],
     dataSource: "tableQuery"
    };

    var tableWithFilter= new TableComponent({	 
      type: "TableComponent",
      name: "table",
      htmlObject: "SampleTable",
      executeAtStart: true,
      chartDefinition: tableDefinition,
      postExecution: function() {
        $("#"+this.htmlObject).prepend('<input value="wrong">');
      }
    });

    dashboard.addComponent(exportButtonComponent);

    // inject sampleObject div
    var $htmlObject = $('<div>').attr('id', exportButtonComponent.htmlObject);

    /**
     * ## The Export Button Component # allows a dashboard to execute update
     */
    it("allows a dashboard to execute update", function(done) {
      $('body').append($htmlObject);

      spyOn(exportButtonComponent, 'update').and.callThrough();

      // listen to cdf:postExecution event
      exportButtonComponent.once("cdf:postExecution", function() {
        expect(exportButtonComponent.update).toHaveBeenCalled();
        $htmlObject.remove();
        done();
      });

      dashboard.update(exportButtonComponent);

    });

    dashboard.addComponent(tableWithFilter);

    var $htmlObject2 = $('<div>').attr('id',tableWithFilter.htmlObject);
    var expected = "right";
    
    /**
     * ## The Export Button Component # discern between an input and the filter input
     */
    it("discern between an input and the filter input",function(done) {
      $('body').append($htmlObject)
               .append($htmlObject2);
      spyOn(exportButtonComponent, 'getFilterSettings').and.callThrough();
      spyOn($, 'ajax').and.callFake(function(params) {
        params.success('{"metadata":["Sales"],"values":[["Euro+ Shopping Channel","914.11"],["Mini Gifts Ltd.","6558.02"]]}');
      });

      tableWithFilter.once("cdf:postExecution",function() {
        $("[type=search]").val('right');
        expect(exportButtonComponent.getFilterSettings(tableWithFilter).dtFilter).toEqual(expected);
        $htmlObject.remove();
        $htmlObject2.remove();
        done();
      });

      dashboard.update(tableWithFilter);
      
    });

    it("follows an export page option",function() {
      expect(exportButtonComponent.getFilterSettings(exportButtonComponent).exportPage).toEqual(true);

      var exportButtonComponentWithFullDataExport = new ExportButtonComponent({
        type: "ExportButtonComponentWithFullDataExport",
        name: "expButton2",
        executeAtStart: true,
        htmlObject: "sampleObjectExportButton",
        priority: 5,
        exportPage: false,
        label: "TestExport2",
        componentName: "",
        parameters: [],
        outputType: "csv",
        listeners: []
      });

      expect(
        exportButtonComponentWithFullDataExport.getFilterSettings(exportButtonComponentWithFullDataExport).exportPage
      ).toEqual(false);
    });
   });
  });
