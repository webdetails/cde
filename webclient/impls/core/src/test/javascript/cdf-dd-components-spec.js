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


describe("CDF-DD-COMPONENTS tests", function() {
  var panel;

  beforeEach(function() {
    panel = new ComponentsPanel();
    panel.componentsPallete = new PalleteManager(ComponentsPanel.PALLETE);
    panel.componentsTable = new TableManager(ComponentsPanel.COMPONENTS);
    panel.propertiesTable = new TableManager(ComponentsPanel.PROPERTIES);
  });

  it("Template Load Test", function() {
    spyOn(panel, "init");
    panel.initTables();
    expect(panel.initPallete).toBe(false);
  });

  describe("Components Table Tests", function() {
    var panelCompRows = {
      oldLabel: {
        type: "Label",
        typeDesc: "Label Desc"
      },
      newLabel: {
        type: "Label",
        typeDesc: "Label Desc",
        rowName: "Label Name"
      },
      oldComp: {
        type: "Component",
        typeDesc: "Comp Desc"
      },
      newComp: {
        type: "Component",
        typeDesc: "Comp Desc",
        rowName: "Comp Name"
      }
    };

    beforeEach(function() {
      spyOn(panel.componentsPallete, "setLinkedTableManager");
      spyOn(panel.componentsTable, "init");
      spyOn(cdfdd, "getDashboardData").and.returnValue({components: {rows: []}});

      panel.initComponentsTable();
    });

    it("Get Component Type Description Test", function() {
      var columnExpressions = panel.componentsTable.getTableModel().getColumnGetExpressions();
      var getDisplay = columnExpressions[0];

      expect(getDisplay(panelCompRows.oldLabel)).toEqual("Label Desc");
      expect(getDisplay(panelCompRows.newLabel)).toEqual("Label Desc");
      expect(getDisplay(panelCompRows.oldComp)).toEqual("Comp Desc");
      expect(getDisplay(panelCompRows.newComp)).toEqual("Comp Name");
    });
  });
});
