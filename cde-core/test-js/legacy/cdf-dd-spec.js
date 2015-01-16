/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
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

  it("Save Settings # Change Table Component Style Property", function() {

    var rendererType = undefined;
    cdfdd.components.initTables = function() {};
    var table = {
      id: "table",
      type: "ComponentsTable",
      typeDesc: "table Component",
      parent: "OTHERCOMPONENTS",
      properties: [{
        name: "tableStyle",
        value: "bootstrap"
      }]
    };

    spyOn(cdfdd, 'getDashboardData').and.callFake(function() { return { components: {rows: [table]} }; });
    spyOn(cdfdd, 'getDashboardWcdf').and.callFake(function() { return { rendererType: rendererType }; });
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(SaveRequests, 'saveSettings');

    /*change from bootstrap to blueprint*/
    rendererType = 'bootstrap';
    cdfdd.saveSettingsRequest({rendererType:'blueprint'});
    expect(table.properties[0].value).toBe("themeroller");
    expect(SaveRequests.saveSettings).toHaveBeenCalled();

    /*change from blueprint to bootstrap*/
    rendererType = 'blueprint';
    cdfdd.saveSettingsRequest({rendererType:'bootstrap'});
    expect(table.properties[0].value).toBe("bootstrap");
    expect(SaveRequests.saveSettings).toHaveBeenCalled();
  });
});
