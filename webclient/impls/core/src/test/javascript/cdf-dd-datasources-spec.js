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
