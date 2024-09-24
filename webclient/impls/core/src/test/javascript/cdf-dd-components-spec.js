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
