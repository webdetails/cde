/*!
 * Copyright 2002 - 2015 Webdetails, a Pentaho company. All rights reserved.
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

// Global datasources array where all the new datasources are registered.
var CDFDDDatasourcesArray = [];

var DatasourcesPanel = Panel.extend({

  name: "Datasources Panel",
  initPallete: true,
  datasourcesPallete: {},
  datasourcesTable: {},
  datasourcesTableModel: {},
  propertiesTable: {},
  propertiesTableModel: {},
  datasourcesArray: [],

  constructor: function(id) {
    this.base(id);
    this.logger = new Logger("Datasources");
    Panel.register(this);
  },

  initTables: function() {
    this.initPallete = false;
    this.init();
  },

  init: function() {
    var panelOperations = [
      new DatasourcesMoveUpOperation(),
      new DatasourcesMoveDownOperation(),
      new DatasourcesDuplicateOperation(),
      new DatasourcesDeleteOperation()
    ];

    this.base();
    this.logger.debug("Specific init");

    // Pallete
    if(this.initPallete) {
      this.datasourcesPallete = new PalleteManager(DatasourcesPanel.PALLETE);
      this.addPalleteEntries();
    }
    this.datasourcesPallete.init();

    // Datasources
    this.datasourcesTable = new TableManager(DatasourcesPanel.DATASOURCES);
    this.datasourcesTable.setTitle("Datasources");
    this.datasourcesPallete.setLinkedTableManager(this.datasourcesTable);
    this.datasourcesTable.setOperations(panelOperations);

    var datasourcesTableModel = new TableModel('datasourcesTreeTableModel');
    datasourcesTableModel.setColumnNames(['Type', 'Name']);
    var typeDescription = function(row) { return row.typeDesc; };
    var rowProperties = function(row) { return CDFDDUtils.ev(row.properties[0].value); };
    datasourcesTableModel.setColumnGetExpressions([typeDescription, rowProperties]);
    datasourcesTableModel.setColumnTypes(['String', 'String']);
    var rowId = function(row) { return row.id; };
    datasourcesTableModel.setRowId(rowId);
    var rowType = function(row) { return row.type; };
    datasourcesTableModel.setRowType(rowType);
    var rowParent = function(row) { return row.parent; };
    datasourcesTableModel.setParentId(rowParent);
    var dataSources = cdfdd.getDashboardData().datasources.rows;
    datasourcesTableModel.setData(dataSources);
    this.datasourcesTable.setTableModel(datasourcesTableModel);
    this.datasourcesTable.init();
    $('#' + DatasourcesPanel.DATASOURCES).addClass('selectedTable');

    // Properties
    this.propertiesTable = new TableManager(DatasourcesPanel.PROPERTIES);
    this.propertiesTable.setTitle("Properties");
    var propertiesTableModel = new PropertiesTableModel('datasourcesPropertiesTableModel');
    propertiesTableModel.setColumnGetExpressions([
      function(row) { return row.description; },
      function(row) { return CDFDDUtils.ev(row.value); }
    ]);

    // If we set the name, we need to change the name in the datasourcesTable
    propertiesTableModel.setColumnSetExpressions([undefined,
      function(row, value) {
        row.value = value;
        if(row.name == 'name') {
          var _tableManager = TableManager.getTableManager("table-" + DatasourcesPanel.DATASOURCES);
          this.logger.debug("Changing the name - applying to previous row in " + _tableManager + " in row " + _tableManager.getSelectedCell()[0]);
          var _cell = _tableManager.getSelectedCell();
          $("#" + _tableManager.getTableId() + " > tbody > tr:eq(" + _cell[0] + ") > td:eq(1)").text(value);
        }
      }
    ]);

    this.propertiesTable.setTableModel(propertiesTableModel);
    this.propertiesTable.init();

    this.datasourcesTable.setLinkedTableManager(this.propertiesTable);
    this.datasourcesTable.setLinkedTableManagerOperation(function(row) {
      var arr = [];
      for(p in row.properties) {
        if(row.properties.hasOwnProperty(p)) {
          arr.push(row.properties[p]);
        }
      }
      return arr;
    });

    $('#' + DatasourcesPanel.DATASOURCES).click(function(e) {
      $('#' + DatasourcesPanel.PROPERTIES).removeClass('selectedTable').addClass('unselectedTable');
      $('#' + DatasourcesPanel.DATASOURCES).addClass('selectedTable').removeClass('unselectedTable');
    });

    $('#' + DatasourcesPanel.PROPERTIES).click(function(e) {
      $('#' + DatasourcesPanel.DATASOURCES).addClass('unselectedTable').removeClass('selectedTable');
      $('#' + DatasourcesPanel.PROPERTIES).addClass('selectedTable').removeClass('unselectedTable');
    });
  },

  getContent: function() {
    return '\n' +
        '<div id="' + DatasourcesPanel.PALLETE + '" class="span-6 accordion"></div>\n' +
        '<div id="' + DatasourcesPanel.DATASOURCES + '" class="span-8 panel-scroll-element">Datasources</div>\n' +
        '<div id="' + DatasourcesPanel.PROPERTIES + '" class="span-10 panel-scroll-element last">Properties</div>\n';
  },

  addPalleteEntries: function() {
    $.each(CDFDDDatasourcesArray, function(i, datasource) {
      Panel.getPanel(DatasourcesPanel.MAIN_PANEL).getDatasourcesPallete().addEntry(datasource);
    });
  },

  // Get Datasources
  getDatasources: function() {
    var data = this.datasourcesTable.getTableModel().getData();
    var output = [];
    var myself = this;
    $.each(data, function(i, row) {
      var nameProperty = row.properties && row.properties[0];
      if(row.type !== "Label" && nameProperty && nameProperty.value !== "") {
        output.push(row);
      }
    });
    return output;
  },

  // Get Previous jndi - iterate through all but the last one, the newly added
  getPreviousProperty: function(prop) {

    var data = this.datasourcesTable.getTableModel().getData();
    var output = "";
    var myself = this;
    $.each(data, function(i, row) {
      if(i == data.length - 1) {
        return false;
      }
      if(row.type == DatasourcesMdxModel.MODEL || row.type == DatasourcesSqlModel) {
        output = Panel.getRowPropertyValue(row, prop);
        return false;
      }
    });

    return output;
  },

  getDuplicateOperation: function() {
    return DatasourcesDuplicateOperation;
  },

  getSelectedTable: function() {
    var selectedTableId = $('#panel-' + this.id + ' .selectedTable').attr('id');

    return TableManager.getTableManager("table-" + selectedTableId);
  },

  selectNextTable: function() {
    var selectedTableId = $('#panel-' + this.id + ' .selectedTable').attr('id');

    if(selectedTableId == DatasourcesPanel.DATASOURCES && this.datasourcesTable.isSelectedCell) {
      $('#' + DatasourcesPanel.PROPERTIES).click();
      return this.propertiesTable;
    } else if(selectedTableId == DatasourcesPanel.PROPERTIES) {
      $('#' + DatasourcesPanel.DATASOURCES).click();
      return this.datasourcesTable;
    }
  },

  getPreviousJndi: function() {
    return this.getPreviousProperty('jndi');
  },
  getPreviousCatalog: function() {
    return this.getPreviousProperty('catalog');
  },
  getPreviousCube: function() {
    return this.getPreviousProperty('cube');
  },

  setDatasourcesPallete: function(datasourcesPallete) {
    this.datasourcesPallete = datasourcesPallete;
  },
  getDatasourcesPallete: function() {
    return this.datasourcesPallete;
  },
  setDatasourcesArray: function(datasourcesArray) {
    this.datasourcesArray = datasourcesArray;
  },
  getDatasourcesArray: function() {
    return this.datasourcesArray;
  }
}, {

  MAIN_PANEL: "datasourceens_panel",
  PALLETE: "cdfdd-datasources-pallete",
  PALLETE_ID: "cdfdd-datasources-palletePallete",
  DATASOURCES: "cdfdd-datasources-datasources",
  PROPERTIES: "cdfdd-datasources-properties"
});

/*
  Datasources Panel Operations
 */
var DatasourcesDuplicateOperation = DuplicateOperation.extend({

  id: "DATASOURCES_DUPLICATE",
  types: ["Datasources"],
  name: "Duplicate datasource",
  description: "Insert a clone of this datasource",

  constructor: function() {
    this.logger = new Logger("DatasourcesDuplicateOperation");
  },

  canExecute: function( tableManager ) {
    return tableManager.isSelectedCell && !tableManager.isSelectedGroupCell;
  }
});
CellOperations.registerOperation(new DatasourcesDuplicateOperation);

var DatasourcesMoveUpOperation = MoveUpOperation.extend({

  id: "DATASOURCES_MOVE_UP",
  types: ["Datasources"],

  constructor: function() {
    this.logger = new Logger("DatasourcesMoveUpOperation");
  }
});
CellOperations.registerOperation(new DatasourcesMoveUpOperation);

var DatasourcesMoveDownOperation = MoveDownOperation.extend({

  id: "DATASOURCES_MOVE_DOWN",
  types: ["Datasources"],

  constructor: function() {
    this.logger = new Logger("DatasourcesMoveDownOperation");
  }
});
CellOperations.registerOperation(new DatasourcesMoveDownOperation);

var DatasourcesDeleteOperation = DeleteOperation.extend({

  id: "DATASOURCES_DELETE",
  types: ["Datasources"],

  constructor: function() {
    this.logger = new Logger("DatasourcesDeleteOperation");
  }
});
CellOperations.registerOperation(new DatasourcesDeleteOperation);
