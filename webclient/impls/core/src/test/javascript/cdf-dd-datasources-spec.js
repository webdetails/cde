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

describe("CDF-DD-DATASOURCES tests", function () {
  var panel;

  beforeEach(function() {
    panel = new DatasourcesPanel();
    panel.datasourcesPallete = new PalleteManager(DatasourcesPanel.PALLETE);
    panel.datasourcesTable = new TableManager(DatasourcesPanel.DATASOURCES);
    panel.propertiesTable = new TableManager(DatasourcesPanel.PROPERTIES);
  });

  it("Template Load Test - DataSources", function() {
    spyOn(panel, "init");
    panel.initTables();
    expect(panel.initPallete).toBe(false);
  });

  describe("Datasources Table Tests", function() {
    var panelSourcesRows = {
      oldLabel: {
        type: "Label",
        typeDesc: "Label Desc"
      },
      newLabel: {
        type: "Label",
        typeDesc: "Label Desc",
        rowName: "Label Name"
      },
      oldSource: {
        type: "DataSource",
        typeDesc: "Source Desc"
      },
      newSource: {
        type: "DataSource",
        typeDesc: "Source Desc",
        rowName: "Source Name"
      }
    };

    beforeEach(function() {
      spyOn(panel.datasourcesPallete, "setLinkedTableManager");
      spyOn(panel.datasourcesTable, "init");
      spyOn(cdfdd, "getDashboardData").and.returnValue({datasources: {rows: []}});

      panel.initDataSourceTable();
    });

    it("Get DataSource Type Description Test", function() {
      var columnExpressions = panel.datasourcesTable.getTableModel().getColumnGetExpressions();
      var getDisplay = columnExpressions[0];

      expect(getDisplay(panelSourcesRows.oldLabel)).toEqual("Label Desc");
      expect(getDisplay(panelSourcesRows.newLabel)).toEqual("Label Desc");
      expect(getDisplay(panelSourcesRows.oldSource)).toEqual("Source Desc");
      expect(getDisplay(panelSourcesRows.newSource)).toEqual("Source Name");
    });
  });

});
