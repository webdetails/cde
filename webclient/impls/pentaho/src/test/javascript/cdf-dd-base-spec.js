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
describe("CDF-DD-BASE tests", function() {

  describe("Template Synchronize Requests tests", function() {

    beforeEach(function() {

      cdfdd.layout = {init: function() {}};
      cdfdd.components = {initTables: function() {}};
      cdfdd.datasources = {initTables: function() {}};

      cdfdd.dashboardData = {
        layout: { rows: ["layout_test"]},
        components: { rows: ["comp_test"]},
        datasources: { rows: ["data_test"]}
      };

    });

    it("test load template when the template is empty",function() {

      SynchronizeRequests.selectTemplate = {
          structure: {
              layout: { rows: ["layout_temp"]},
              components: { rows: []},
              datasources: { rows: []}
          }
      };

      SynchronizeRequests.callbackLoadTemplate(true);

      expect( cdfdd.dashboardData.layout.rows[0] == "layout_temp" ).toBeTruthy()
      expect( cdfdd.dashboardData.components.rows[0] == "comp_test" ).toBeTruthy();
      expect( cdfdd.dashboardData.datasources.rows[0] == "data_test" ).toBeTruthy();

    });

    it("test load Template when the template is not empty", function() {

      SynchronizeRequests.selectTemplate = {
        "structure": {
          layout: { "rows": ["layout_templ"]},
          components: { rows: ["comp_templ"]},
          datasources: { rows: ["data_templ"]}
        }
      };

      SynchronizeRequests.callbackLoadTemplate(true);

      expect( cdfdd.dashboardData.layout.rows[0] == "layout_templ" ).toBeTruthy()
      expect( cdfdd.dashboardData.components.rows[0] == "comp_templ" ).toBeTruthy();
      expect( cdfdd.dashboardData.datasources.rows[0] == "data_templ" ).toBeTruthy();
    });
  });

  describe("SaveRequests Tests", function() {


    it("saveAsWidget test", function () {

      var saveParams = {
        operation: "saveas"
      };

      var folder = "/public/cde/widgets/";
      var file = "widget.wcdf";
      var submitUrl;

      spyOn($.prototype, "submit").and.callFake(function () {
        submitUrl = $(this[0]).attr("action");
      });

      SaveRequests.saveAsWidget(saveParams, folder, file, cdfdd);

      expect(submitUrl == "/pentaho/plugin/pentaho-cdf-dd/api/syncronizer/saveDashboard").toBe(true);
      expect(SaveRequests.saveRequestParams.selectedFolder == folder).toBe(true);
      expect(SaveRequests.saveRequestParams.selectedFile == file).toBe(true);

    });

    it("saveAsWidgetCallback test", function() {

      SaveRequests.saveRequestParams = {
        selectedFolder: "/public/cde/widgets/",
        selectedFile: "widget.wcdf",
        myself: cdfdd
      }

      spyOn(cdfdd, "saveSettingsRequest");
      spyOn(SaveRequests, "redirect");

      SaveRequests.saveAsWidgetCallback({status:"true"});
      expect(cdfdd.saveSettingsRequest.calls.mostRecent().args[0].widget).toBe(true);
      expect(SaveRequests.redirect).toHaveBeenCalled();
    });

  });

  describe("StyleRequests Tests", function() {

    it("syncStyles test", function() {
      var myself = {
        _legacyStyles: [],
        _requireStyles: []
      };

      spyOn($, "getJSON").and.callFake(function(url, params, success) {
        success({result: ["LStyle", "RStyleRequire", "LStyle - (plugin)", "RStyleRequire - (plugin)"]});
      });

      StylesRequests.syncStyles(myself);

      expect(myself._legacyStyles).toEqual(["LStyle", "LStyle - (plugin)"]);
      expect(myself._requireStyles).toEqual(["RStyle", "RStyle - (plugin)"]);

    });

  })

});
