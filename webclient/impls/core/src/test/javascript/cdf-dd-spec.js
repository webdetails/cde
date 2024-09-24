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

describe("CDF-DD tests", function() {

  it("knows empty filename is a new file",function() {
    expect(cdfdd.isNewFile(null)).toBeTruthy();
  });

  it("knows a /null/null/null filename is a new file",function() {
    expect(cdfdd.isNewFile('/null/null/null')).toBeTruthy();
  });

  it("knows anything else is not a new file",function() {
    expect(cdfdd.isNewFile('/public/plugin-samples/pentaho-cdf-dd/cde_sample.wcdf')).toBeFalsy();
  });

  describe("## combinedSaveAs Tests", function() {
    beforeEach(function(done) {
      spyOn(SaveRequests, "saveAsDashboard");
      spyOn(SolutionTreeRequests, "getExplorerFolderEndpoint");
      spyOn(CDFDDUtils, "promptNotification");

      done();
    });

    afterEach(function(done) {
      $(".layout-popup").remove();
      done();
    });

    it("Default layout should be in dashboard mode", function() {
      cdfdd.combinedSaveAs();

      //Test if it is in dashboard save mode
      expect($(".layout-popup #dashRadio").is(":checked")).toBe(true);
      expect($(".layout-popup #widgetRadio").is(":checked")).toBe(false);

      //Test if the elements visible are only the ones related to dashboard save mode
      expect($(".layout-popup #saveAsFolderExplorer").is(":visible")).toBe(true);
      expect($(".layout-popup #fileInput").is(":visible")).toBe(true);
      expect($(".layout-popup #titleInput").is(":visible")).toBe(true);
      expect($(".layout-popup #descriptionInput").is(":visible")).toBe(true);
      expect($(".layout-popup #componentInput").is(":visible")).toBe(false);
    });

    it("should show error notification", function(done) {
      cdfdd.combinedSaveAs();

      //Values not properly defined
      $(".layout-popup #fileInput").val("");
      $(".layout-popup #titleInput").val("");
      $(".layout-popup #descriptionInput").val("");

      cdfdd.combinedSaveAsSubmit(true, "");

      expect(SaveRequests.saveAsDashboard).not.toHaveBeenCalled();
      expect(CDFDDUtils.promptNotification).toHaveBeenCalled();
      expect(CDFDDUtils.promptNotification.calls.mostRecent().args[0]).toEqual("Error");

      done();
    });

    it("Should call endpoint to save the dashboard", function() {
      var folder = "dash_folder/";
      var filename = "dash_filename.wcdf";

      var saveAsParams = {
        operation: "saveas",
        file: folder + filename,
        title: "dash_title",
        description: "dash_description",
        cdfstructure: "{}"
      };

      spyOn(cdfdd, "strip");
      spyOn(JSON, "stringify").and.returnValue(saveAsParams.cdfstructure);

      cdfdd.combinedSaveAs();

      $(".layout-popup #fileInput").val(filename);
      $(".layout-popup #titleInput").val(saveAsParams.title);
      $(".layout-popup #descriptionInput").val(saveAsParams.description);

      cdfdd.combinedSaveAsSubmit(true, folder);

      expect(CDFDDUtils.promptNotification).not.toHaveBeenCalled();
      expect(SaveRequests.saveAsDashboard).toHaveBeenCalled();

      var mostRecentCalls = SaveRequests.saveAsDashboard.calls.mostRecent().args;

      expect(mostRecentCalls[0]).toEqual(saveAsParams);
      expect(mostRecentCalls[1]).toEqual(folder);
      expect(mostRecentCalls[2]).toEqual(filename);
      expect(mostRecentCalls[3]).toEqual(cdfdd);
    });
  });
});
